package com.morpho.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.window.layout.WindowMetrics
import androidx.window.layout.WindowMetricsCalculator


class MorphoAndroidActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App()
        }
    }

    fun getWindowMetrics(): WindowMetrics {
        return WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}