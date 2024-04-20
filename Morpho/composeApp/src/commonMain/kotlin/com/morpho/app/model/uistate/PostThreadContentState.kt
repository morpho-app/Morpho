package com.morpho.app.model.uistate

interface PostThreadContentState {
    val hasNewPosts: Boolean
    val loadingState: ContentLoadingState
    val isLoading: Boolean
        get() = loadingState == ContentLoadingState.Loading
}