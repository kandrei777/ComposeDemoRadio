package com.example.radiofetcher

import com.google.gson.Gson
import org.jsoup.Jsoup
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration


fun main() {
    val fetcher = MyFetcher(FetcherConfig.cacheDirectory)
    FetcherConfig.sources.forEach {
        fetcher.processUrl(it.url, it.genre)
    }

    val resultDir = FetcherConfig.resultDirectory
    val icons = File(resultDir, "icons_urls.txt")
    icons.outputStream().bufferedWriter()
        .use { it.write(fetcher.icons.toList().joinToString("\n")) }
    File(resultDir, "stations.json").outputStream().bufferedWriter().use {
        it.write(Gson().toJson(fetcher.stations.toList().map { it.second }))
    }
    println("* Added ${fetcher.stations.size} stations total")
}

class MyFetcher(val cacheDir: File) {
    val stations = mutableMapOf<Int, Station>()
    val icons = mutableSetOf<String>()

    init {
        if (!cacheDir.exists()) {
            throw IllegalArgumentException("${cacheDir.absolutePath} does not exist")
        }
    }

    fun processUrl(url: String, slim: String) {
        require(url.isNotBlank())
        println("# Process $url")
        var counter = 0
        val doc = Jsoup.parse(fetch(url, "$slim.html"))
        // find <li class="item-6"><span><a
        doc.select("li > span > a").forEach {
            val href = it.attr("href")
            val icon = it.child(0).attr("src")
            val base = href.substringAfter("#")
            val json = processRadio(base)
            json?.apply {
                val streams = result.streams.filter { !it.isContainer }.map { it.toStream() }
                if (streams.isNotEmpty()) {
                    val station = result.station.toStation(
                        streams,
                        icon,
                        getDescription(
                            result.station.url,
                            "dsc_${result.station.id}.html"
                        )
                    )
                    icons.add(icon)
                    if (stations.contains(station.id)) {
                        println("# Duplicate:\n## $station\n## ${stations[station.id]} ")
                    } else {
                        stations[station.id] = station
                        counter++
                    }
                }
            }
        }
        println("** Added $counter stations as $slim")
    }

    private fun processRadio(slim: String) =
        parseStation(fetch(FetcherConfig.getStationUrl(slim), "$slim.json"))

    private fun parseStation(data: String): WebData? {
        return Gson().fromJson(data, WebData::class.java)
    }

    @Throws(Exception::class)
    fun fetch(url: String, fileName: String): String {
        val file = if (fileName.length > 1) File(cacheDir, fileName) else null
        return if (file != null && file.exists()) {
            file.inputStream().bufferedReader().use { it.readText() }
        } else {
            try {
                val client: HttpClient =
                    HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .build()

                val request = HttpRequest.newBuilder()
                    .GET().uri(URI(url))
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64; rv:97.0) Gecko/20100101 Firefox/97.0"
                    )
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept", "*/*")
                    .header("referrer", "https://internetradiouk.com/")
                    .build()


                val response = client.send(
                    request,
                    BodyHandlers.ofString()
                )
                val str = response.body()
                file?.apply {
                    outputStream().bufferedWriter().use { it.write(str) }
                }
                str
            } catch (exception: Exception) {
                println("E: Can't fetch $url: ${exception.message}")
                throw exception
            }
        }
    }

    fun getDescription(url: String, cacheFile: String): String = try {
        if (url.length > 7) { // check at least http://
            val doc = Jsoup.parse(fetch(url, cacheFile))
            //     <meta property="og:description"
            //          content="All the top adult contemporary radio stations. An easy page to listen to music, news and other fun!">
            doc.selectFirst("meta[property=og:description]")?.attr("content") ?:
            doc.selectFirst("meta[name=description]")?.attr("content") ?: ""
        } else {
            ""
        }
    } catch (exception: Exception) {
        ""
    }
}

private fun WebStation.toStation(streams: List<Stream>, icon: String, description: String) =
    Station(
        id, title, description, icon, url, streams
    )

private fun WebStream.toStream() = Stream(
    mediaType, mime, url
)
