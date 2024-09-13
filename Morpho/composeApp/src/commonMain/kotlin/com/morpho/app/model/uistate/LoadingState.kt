package com.morpho.app.model.uistate

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface UiLoadingState {
    data object Loading : UiLoadingState
    data object Idle : UiLoadingState
    data class Error(val errorMessage: String) : UiLoadingState
}


@Immutable
@Serializable
sealed interface ContentLoadingState {
    data object Loading : ContentLoadingState
    data object Idle : ContentLoadingState
    data class Error(val errorMessage: String) : ContentLoadingState
}