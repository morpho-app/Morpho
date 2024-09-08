package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.FeedType
import app.bsky.feed.GetFeedGeneratorQuery
import app.bsky.graph.GetListQuery
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class UISavedFeed(
    public val avatar: String? = null,
    public val title: String,
    val description: String? = null,
    public val type: UIFeedType,
    public val pinned: Boolean,
    val feed: FeedGenerator? = null,
    val list: UserList? = null,
)

sealed interface  UIFeedType {
    val type: FeedType
    val value: String

    data class Feed(
        val uri: AtUri
    ): UIFeedType {
        override val type: FeedType = FeedType.FEED
        override val value: String = uri.atUri
    }

    data class List(
        val uri: AtUri
    ): UIFeedType {
        override val type: FeedType = FeedType.LIST
        override val value: String = uri.atUri
    }

    data object Timeline: UIFeedType {
        override val type: FeedType = FeedType.TIMELINE
        override val value: String = "following"
    }
}

suspend fun app.bsky.actor.SavedFeed.toUISavedFeed(api: Butterfly): UISavedFeed {
    return when(this.type) {
        FeedType.FEED -> {
            val feed = api.api.getFeedGenerator(GetFeedGeneratorQuery(AtUri(this.value)))
                .getOrNull()
            UISavedFeed(
                avatar = feed?.view?.avatar,
                description = feed?.view?.description,
                title = feed?.view?.displayName ?: this.value,
                type = UIFeedType.Feed(AtUri(this.value)),
                pinned = this.pinned,
                feed = feed?.view?.toFeedGenerator()
            )
        }
        FeedType.LIST -> {
            val list = api.api.getList(GetListQuery(AtUri(this.value))).getOrNull()?.list
            UISavedFeed(
                avatar = list?.avatar,
                title = list?.name ?: this.value,
                description = list?.description,
                type = UIFeedType.List(AtUri(this.value)),
                pinned = this.pinned,
                list = list?.toList()
            )
        }
        FeedType.TIMELINE -> UISavedFeed(
            avatar = null,
            title = "Home",
            type = UIFeedType.Timeline,
            pinned = this.pinned
        )
    }
}