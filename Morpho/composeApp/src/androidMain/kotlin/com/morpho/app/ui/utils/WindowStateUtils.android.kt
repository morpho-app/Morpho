package com.morpho.app.ui.utils

import android.content.Context
import android.graphics.Rect
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowMetricsCalculator
import com.morpho.app.AndroidMainViewModel
import com.morpho.app.MainActivity
import org.koin.androidx.viewmodel.ext.android.getLazyViewModelForClass

import org.koin.compose.koinInject
import org.koin.core.context.loadKoinModules
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Information about the posture of the device
 */
sealed interface DevicePosture {
    object NormalPosture : DevicePosture

    data class BookPosture(
        val hingePosition: Rect
    ) : DevicePosture

    data class Separating(
        val hingePosition: Rect,
        var orientation: FoldingFeature.Orientation
    ) : DevicePosture
}

@OptIn(ExperimentalContracts::class)
fun isBookPosture(foldFeature: FoldingFeature?): Boolean {
    contract { returns(true) implies (foldFeature != null) }
    return foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
            foldFeature.orientation == FoldingFeature.Orientation.VERTICAL
}

@OptIn(ExperimentalContracts::class)
fun isSeparating(foldFeature: FoldingFeature?): Boolean {
    contract { returns(true) implies (foldFeature != null) }
    return foldFeature?.state == FoldingFeature.State.FLAT && foldFeature.isSeparating
}


actual fun getWindowSizeClass(): WindowSize {
    TODO()
}