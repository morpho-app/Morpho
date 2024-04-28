package com.morpho.app.model.uistate

sealed interface UiLoadingState {
    data object Loading : UiLoadingState
    data object Idle : UiLoadingState
    data class Error(val errorMessage: String) : UiLoadingState
}

sealed interface ContentLoadingState {
    data object Loading : ContentLoadingState
    data object Idle : ContentLoadingState
    data class Error(val errorMessage: String) : ContentLoadingState
}