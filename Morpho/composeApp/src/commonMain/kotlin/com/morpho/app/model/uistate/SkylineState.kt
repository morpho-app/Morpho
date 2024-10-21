package com.morpho.app.model.uistate

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Immutable
@Serializable
enum class FeedType {
    HOME,
    PROFILE_POSTS,
    PROFILE_REPLIES,
    PROFILE_MEDIA,
    PROFILE_LIKES,
    PROFILE_USER_LISTS,
    PROFILE_MOD_SERVICE,
    PROFILE_FEEDS_LIST,
    LIST_FOLLOWING,
    OTHER,
}
