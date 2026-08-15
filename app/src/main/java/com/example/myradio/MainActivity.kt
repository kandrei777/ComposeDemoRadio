package com.example.myradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myradio.ui.main.MainActions
import com.example.myradio.ui.main.MainScreen
import com.example.myradio.ui.main.MainViewModel
import com.example.myradio.ui.theme.MyRadioTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyRadioTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityScreen()
                }
            }
        }
    }
}

@Composable
fun ActivityScreen(vm: MainViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    MainScreen(
        uiState = state,
        actions = vm as MainActions,
        modifier = Modifier.navigationBarsPadding()
    )
}

