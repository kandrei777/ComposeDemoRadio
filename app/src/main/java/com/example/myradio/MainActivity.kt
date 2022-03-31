package com.example.myradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.myradio.model.*
import com.example.myradio.ui.theme.MyRadioTheme

class MainActivity : ComponentActivity() {
    private val playerModel: PlayerModel by lazy {
        ViewModelProvider(this).get(PlayerModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyRadioTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ActivityScreen(playerModel)
                }
            }
        }
    }
}

@Composable
fun ActivityScreen(playerModel: PlayerModel) {
    Screen(
        stations = playerModel.stations,
        currentPlayingStation = playerModel.currentPlayingStation,
        currentSelectStation = playerModel.currentSelectStation,
        onStationClicked = playerModel::selectStation,
        favAction = playerModel.playerAction,
        onFavClicked = playerModel::onFavAction,
        lastUserEvent = playerModel.lastUserEvent,
        onStreamClicked = playerModel::playStream
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val list = listOf<StationItem>(
        StationItem(
            Station(
                "Discover Trance",
                "Featuring the latest and greatest uplifting trance music available.",
                "talksport.webp",
                "https://www.discovertrance.com/",
                listOf(
                    Stream(
                        "audio/x-mpegurl",
                        "http://uk01.discovertrance.com:9216/listen.pls?sid=1"
                    ),
                    Stream("audio/mpeg", "http://uk01.discovertrance.com:9216/stream/1/")
                )
            ),
        ),
        StationItem(
            Station(
                "Discover Trance",
                "Featuring the latest and greatest uplifting trance music available.",
                "discover-trance-uk.webp",
                "https://www.discovertrance.com/",
                listOf(
                    Stream(
                        "audio/x-mpegurl",
                        "http://uk01.discovertrance.com:9216/listen.pls?sid=1"
                    ),
                    Stream("audio/mpeg", "http://uk01.discovertrance.com:9216/stream/1/")
                )
            ),
        ),
    )
    Screen(
        stations = list,
        currentPlayingStation = null,
        currentSelectStation = null,
        onStationClicked = {},
        favAction = PlayerAction.Cancel,
        onFavClicked = { },
        lastUserEvent = null,
        onStreamClicked = {}
    )
}
