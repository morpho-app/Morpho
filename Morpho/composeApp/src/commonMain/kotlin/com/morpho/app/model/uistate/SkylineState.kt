package com.morpho.app.model.uistate

import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.MorphoData
import kotlinx.serialization.Serializable

interface SkylineContentState<T : MorphoDataItem> {
    val hasNewPosts: Boolean
    val feed: MorphoData<T>
    val loadingState: ContentLoadingState
    val isLoading: Boolean
        get() = loadingState == ContentLoadingState.Loading
}

@Serializable
data class SkylineState<T: MorphoDataItem>(
    override val feed: MorphoData<T>,
    override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
    override val hasNewPosts: Boolean = false,
): SkylineContentState<T>

enum class FeedType {
    HOME,
    PROFILE_POSTS,
    PROFILE_REPLIES,
    PROFILE_MEDIA,
    PROFILE_LIKES,
    OTHER
}
