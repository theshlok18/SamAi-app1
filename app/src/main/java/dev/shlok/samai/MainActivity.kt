package dev.shlok.samai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.shlok.samai.ui.SamApp
import dev.shlok.samai.ui.theme.SamTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash until first composition is ready
        var splashVisible = true
        splashScreen.setKeepOnScreenCondition { splashVisible }

        setContent {
            SamTheme {
                SamApp(
                    onSplashReady = { splashVisible = false }
                )
            }
        }
    }
}
