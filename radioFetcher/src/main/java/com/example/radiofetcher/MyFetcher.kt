package com.example.radiofetcher

import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val regexWebConfig = """webradioConfig\s*=\s*(\{.*?});""".toRegex()


fun main() {
    start()
}

fun start() {
    val pool = Executors.newFixedThreadPool(8)
    val client = MyHttpClient(FetcherConfig.cacheDirectory)
    val fetcher = MyFetcher(client)
    fetcher.fetchGenres()
    fetcher.genres.values.forEach { genre ->
        pool.submit {
            fetcher.processGenreUrl(genre.url, genre.tag)
        }
    }
    pool.shutdown()
    pool.awaitTermination(10, TimeUnit.MINUTES)
    File(FetcherConfig.resultDirectory, "stations_v2.json").outputStream().bufferedWriter().use {
        val result = Stations(
            version = getTimestampLabel(), // YYYYMMDDHH24MISS
            genres = fetcher.genres.values.toList(),
            stations = fetcher.stations.values.toList(),
        )
        it.write(Gson().toJson(result))
    }
}

fun getTimestampLabel(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    return current.format(formatter)
}

class MyFetcher(
    private val client: MyHttpClient
) {
    val stations = ConcurrentHashMap<Int, Station>()
    val genres = mutableMapOf<String, Genre>()

    fun fetchGenres() {
        val doc = Jsoup.parse(client.getCached(FetcherConfig.allGenres, "all_genres.htm")!!)
        doc.select("#radios a").forEach { aTag ->
            val href = aTag.attr("href")
            val iconUrl = aTag.selectFirst("img")?.attr("src").orEmpty()
            val title = aTag.selectFirst("i.text")?.text().orEmpty()
            val tag = href.removeSuffix("/").substringAfterLast('/')
            val genre = Genre(
                tag = tag,
                url = href,
                iconUrl = iconUrl,
                title = title
            )
            genres[tag] = genre
        }
    }

    /**
     * Tread safe
     */
    fun processGenreUrl(url: String, genreTag: String) {
        require(url.isNotBlank())
        println("# Process $url")
        val counter = AtomicInteger(0)
        val doc = Jsoup.parse(client.getCached(url, "$genreTag.html")!!)
        val config = processWebConfig(doc)
        doc.select("li > span > a").forEach { station ->
            val href = station.attr("href")
            val icon = station.child(0).attr("src")
            val base = href.substringAfter("#")

            getWebData(config, base)?.apply { // WebData
                val streams = result.streams
                    .filter { !it.isContainer && it.mime.startsWith("audio/") }
                    .map { it.toStream() }

                if (streams.isNotEmpty()) {
                    val station = result.station.toStation(
                        streams,
                        icon,
                        getDescription(
                            result.station.url,
                            "dsc_${result.station.id}.html"
                        ),
                        genre = genreTag,
                        language = config.languageCode,
                    )
                    stations.compute(station.id) { _, stored ->
                        if (stored != null) {
                            stored.copy(
                                title = station.title.ifBlank { stored.title },
                                description = station.description.ifBlank { stored.description },
                                icon = station.icon.ifBlank { stored.icon },
                                url = station.url.ifBlank { stored.url },
                                language = station.language.ifBlank { stored.language },
                                genres = (stored.genres + station.genres).distinct(),
                                streams = (stored.streams + station.streams).distinctBy { it.url }
                            )
                        } else {
                            counter.incrementAndGet()
                            station
                        }
                    }
                }
            }
        }
        println("** Added $counter stations as $genreTag")
    }

    fun processWebConfig(doc: Document): WebradioConfig {
        val scripts = doc.select("script:containsData(webradioConfig)")

        for (s in scripts) {
            val inner = s.html()
            val matchResult = regexWebConfig.find(inner)
            val jsonString = matchResult?.groups?.get(1)?.value ?: continue
            val config = runCatching {
                Gson().fromJson(jsonString, WebradioConfig::class.java)
            }.getOrNull()

            if (config != null) return config
        }
        error("Cannot find config")
    }

    private fun getWebData(config: WebradioConfig, slim: String): WebData? {
        val url = config.urlApi + "data/streams/" + config.domainID + '/' + slim
        val data = client.getCached(url, "$slim.json")!!
        return Gson().fromJson(data, WebData::class.java)
    }

    fun getDescription(url: String, cacheFile: String): String = try {
        if (url.length > 7) { // check at least http://
            val doc = Jsoup.parse(client.getCached(url, cacheFile)!!)
            //     <meta property="og:description"
            //          content="All the top adult contemporary radio stations. An easy page to listen to music, news and other fun!">
            doc.selectFirst("meta[property=og:description]")?.attr("content") ?: doc.selectFirst("meta[name=description]")?.attr("content")
            ?: ""
        } else {
            ""
        }
    } catch (_: Exception) {
        println("Cannot get description for: $url")
        ""
    }
}

private fun WebStation.toStation(
    streams: List<Stream>,
    icon: String,
    description: String,
    genre: String,
    language: String,
) =
    Station(
        id, title, description, icon, url,
        genres = listOf(genre),
        language = language,
        streams = streams,
    )

private fun WebStream.toStream() = Stream(
    mediaType, mime, url
)
