package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import com.morpho.app.model.uistate.FeedType
import com.morpho.app.util.JavaSerializable
import com.morpho.app.util.MutableSharedFlowSerializer
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface ContentCardMapEntry: JavaSerializable {
    val uri: AtUri
    val title: String
    @Serializable(with = MutableSharedFlowSerializer::class)
    val cursorFlow: MutableSharedFlow<AtCursor>
    val avatar: String?

    @Immutable
    @Serializable
    data object Home: ContentCardMapEntry, Skyline {
        override val uri: AtUri = AtUri.HOME_URI
        override val title: String = "Home"
        @Serializable(with = MutableSharedFlowSerializer::class)
        override val cursorFlow: MutableSharedFlow<AtCursor> = initAtCursor()
        override val avatar: String? = null
    }

    @Immutable
    @Serializable
    sealed interface Skyline

    @Immutable
    @Serializable
    data class Feed(
        override val uri: AtUri,
        override val title: String = uri.atUri,
        @Serializable(with = MutableSharedFlowSerializer::class)
        override val cursorFlow: MutableSharedFlow<AtCursor> = initAtCursor(),
        override val avatar: String? = null,
    ) : ContentCardMapEntry, Skyline

    @Immutable
    @Serializable
    data class PostThread(
        override val uri: AtUri,
        override val title: String = uri.atUri,
        @Serializable(with = MutableSharedFlowSerializer::class)
        override val cursorFlow: MutableSharedFlow<AtCursor> = initAtCursor(),
        override val avatar: String? = null,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class UserList(
        override val uri: AtUri,
        override val title: String = uri.atUri,
        @Serializable(with = MutableSharedFlowSerializer::class)
        override val cursorFlow: MutableSharedFlow<AtCursor> = initAtCursor(),
        override val avatar: String? = null,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class FeedList(
        override val uri: AtUri,
        override val title: String = uri.atUri,
        @Serializable(with = MutableSharedFlowSerializer::class)
        override val cursorFlow: MutableSharedFlow<AtCursor> = initAtCursor(),
        override val avatar: String? = null,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class ServiceList(
        override val uri: AtUri,
        override val title: String = uri.atUri,
        @Serializable(with = MutableSharedFlowSerializer::class)
        override val cursorFlow: MutableSharedFlow<AtCursor> = initAtCursor(),
        override val avatar: String? = null,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class Profile(
        val id: AtIdentifier,
        override val uri: AtUri = AtUri.profileUri(id),
        override val title: String = uri.atUri,
        @Serializable(with = MutableSharedFlowSerializer::class)
        override val cursorFlow: MutableSharedFlow<AtCursor> = initAtCursor(),
        override val avatar: String? = null,
    ) : ContentCardMapEntry

    val isHome: Boolean
        get() = uri == AtUri.HOME_URI
    val feedType: FeedType
        get() = when {
            isHome -> FeedType.HOME
            uri.atUri.matches(AtUri.ProfilePostsUriRegex) -> FeedType.PROFILE_POSTS
            uri.atUri.matches(AtUri.ProfileRepliesUriRegex) -> FeedType.PROFILE_REPLIES
            uri.atUri.matches(AtUri.ProfileMediaUriRegex) -> FeedType.PROFILE_MEDIA
            uri.atUri.matches(AtUri.ProfileLikesUriRegex) -> FeedType.PROFILE_LIKES
            uri.atUri.matches(AtUri.ProfileUserListsUriRegex) -> FeedType.PROFILE_USER_LISTS
            uri.atUri.matches(AtUri.ProfileModServiceUriRegex) -> FeedType.PROFILE_MOD_SERVICE
            uri.atUri.matches(AtUri.ProfileFeedsListUriRegex) -> FeedType.PROFILE_FEEDS_LIST
            else -> FeedType.OTHER
        }
}