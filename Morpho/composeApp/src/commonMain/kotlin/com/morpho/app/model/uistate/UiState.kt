package com.morpho.app.model.uistate



interface UiState {
    val loadingState: UiLoadingState

    val isLoading: Boolean
        get() = loadingState == UiLoadingState.Loading
}