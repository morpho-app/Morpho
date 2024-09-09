package com.morpho.app.model.uistate

import com.morpho.app.util.JavaSerializable


interface PostThreadContentState: JavaSerializable {
    val hasNewPosts: Boolean
    val loadingState: ContentLoadingState
    val isLoading: Boolean
        get() = loadingState == ContentLoadingState.Loading
}