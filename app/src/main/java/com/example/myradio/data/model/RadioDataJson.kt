package com.example.myradio.data.model

import com.google.gson.annotations.SerializedName

data class RadioDataJson(
    @SerializedName("genres") val genres: List<GenreJson>,
    @SerializedName("stations") val stations: List<StationJson>,
    @SerializedName("version") val version: String
)

data class GenreJson(
    @SerializedName("tag") val tag: String,
    @SerializedName("title") val title: String,
    @SerializedName("iconUrl") val iconUrl: String?,
    @SerializedName("url") val url: String?
)

data class StationJson(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val iconUrl: String?,
    @SerializedName("url") val websiteUrl: String?,
    @SerializedName("language") val language: String,
    @SerializedName("genres") val genres: List<String>,
    @SerializedName("streams") val streams: List<StreamJson>
) {
    val webKey: String
        get() {
            val sanitizedTitle = title.lowercase()
                .replace(" ", "-")
                .filter { it.isLetterOrDigit() || it == '-' }
            return "$id-$sanitizedTitle"
        }
}

data class StreamJson(
    @SerializedName("mediaType") val mediaType: String,
    @SerializedName("mime") val mime: String,
    @SerializedName("url") val streamUrl: String
)