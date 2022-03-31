package com.example.myradio.model

import android.content.Context
import com.google.gson.Gson
import java.io.IOException
import java.net.URL

object Store {
    fun loadData(context: Context): Array<Station> {
        try {
            val data = context.assets.open("stations.json").bufferedReader().use { it.readText() }
            return Gson().fromJson(data, Array<Station>::class.java)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return emptyArray()
        }
    }
}