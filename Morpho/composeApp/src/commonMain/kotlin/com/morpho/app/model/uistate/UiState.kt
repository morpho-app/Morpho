package com.morpho.app.model.uistate


sealed interface UiLoadingState {
    data object Loading : UiLoadingState
    data object Idle : UiLoadingState
    data class Error(val errorMessage: String) : UiLoadingState
}