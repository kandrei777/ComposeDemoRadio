package com.example.radiofetcher

import java.io.File

data class SourceUrl(val url: String, val genre: String)
object FetcherConfig {

    val resultDirectory = File("<DIR>")
    val cacheDirectory = File("<DIR>")
    val sources: List<SourceUrl> = listOf(
        SourceUrl("<URL>", "rock"),
    )
    fun getStationUrl(slim: String): String = "<URL>$slim"
}
