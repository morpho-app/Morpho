package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.FeedType
import app.bsky.feed.GeneratorView
import app.bsky.feed.GetFeedGeneratorQuery
import app.bsky.graph.GetListQuery
import app.bsky.graph.ListView
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.ButterflyAgent
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.Nsid
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Serializable
@Immutable
enum class AuthorFilter {
    PostsWithReplies,
    PostsNoReplies,
    PostsAuthorThreads,
    PostsWithMedia,
}

@Serializable
@Immutable
@Parcelize
sealed interface FeedDescriptor: Parcelable {
    @Serializable
    @Immutable
    data object Home: FeedDescriptor
    @Serializable
    @Immutable
    data class Author(
        val did: Did,
        val filter: AuthorFilter = AuthorFilter.PostsWithReplies
    ): FeedDescriptor
    @Serializable
    @Immutable
    data class FeedGen(val uri: AtUri): FeedDescriptor
    @Serializable
    @Immutable
    data class Likes(val did: Did): FeedDescriptor
    @Serializable
    @Immutable
    data class List(val uri: AtUri): FeedDescriptor
}

@Serializable
@Immutable
@Parcelize
sealed interface FeedSourceInfo: Parcelable {
    val uri: AtUri
    val cid: Cid
    val avatar: String?
    val displayName: String?
    val description: String?
    val creatorDid: Did
    val creatorHandle: Handle
    val feedDescriptor: FeedDescriptor
    val type: Nsid
    val pinned: Boolean?

    @Serializable
    @Immutable
    data class ListInfo(
        override val uri: AtUri,
        override val cid: Cid,
        override val avatar: String?,
        override val displayName: String?,
        override val description: String?,
        override val creatorDid: Did,
        override val creatorHandle: Handle,
        override val feedDescriptor: FeedDescriptor,
        override val pinned: Boolean? = null,
    ): FeedSourceInfo {
        override val type: Nsid = Nsid("app.bsky.feed.generator")
    }

    @Serializable
    @Immutable
    data class FeedInfo(
        override val uri: AtUri,
        override val cid: Cid,
        override val avatar: String?,
        override val displayName: String?,
        override val description: String?,
        override val creatorDid: Did,
        override val creatorHandle: Handle,
        override val feedDescriptor: FeedDescriptor,
        val likeCount: Long? = null,
        val likeUri: AtUri? = null,
        override val pinned: Boolean? = null,
    ): FeedSourceInfo {
        override val type: Nsid = Nsid("app.bsky.graph.list")
    }

    @Serializable
    @Immutable
    data object Home: FeedSourceInfo {
        override val uri: AtUri = AtUri.HOME_URI
        override val cid: Cid = Cid("home")
        override val avatar: String? = null
        override val displayName: String = "Home"
        override val description: String = "Your home feed, currently same as Following"
        override val creatorDid: Did = Did("did:web:morpho.app")
        override val creatorHandle: Handle = Handle("${displayName.lowercase()}.morpho.app")
        override val feedDescriptor: FeedDescriptor = FeedDescriptor.Home
        override val type: Nsid = Nsid("app.morpho.feed.home")
        override val pinned: Boolean = true
    }

    @Serializable
    @Immutable
    data object Following: FeedSourceInfo {
        override val creatorDid: Did = Did("did:web:morpho.app")
        override val uri: AtUri = AtUri.HOME_URI
        override val cid: Cid = Cid("following")
        override val avatar: String? = null
        override val displayName: String = "Following"
        override val description: String = "Your feed of people you follow"
        override val creatorHandle: Handle = Handle("${displayName.lowercase()}.morpho.app")
        override val feedDescriptor: FeedDescriptor = FeedDescriptor.Home
        override val type: Nsid = Nsid("app.morpho.feed.following")
        override val pinned: Boolean = true
    }
}

fun FeedSourceInfo.toContentCardMapEntry(): ContentCardMapEntry {
    return when(this) {
        is FeedSourceInfo.FeedInfo -> ContentCardMapEntry.Feed(this.uri, this.displayName?: "", this.avatar)
        FeedSourceInfo.Following -> ContentCardMapEntry.Home
        FeedSourceInfo.Home -> ContentCardMapEntry.Home
        is FeedSourceInfo.ListInfo -> ContentCardMapEntry.ListFeed(this.uri, this.displayName?: "", this.avatar)
    }
}

fun GeneratorView.hydrateFeedGenerator(): FeedSourceInfo.FeedInfo {
    return FeedSourceInfo.FeedInfo(
        uri = this.uri,
        cid = this.cid,
        avatar = this.avatar,
        displayName = this.displayName,
        description = this.description,
        creatorDid = this.creator.did,
        creatorHandle = this.creator.handle,
        feedDescriptor = FeedDescriptor.FeedGen(this.uri),
        likeCount = this.likeCount,
        likeUri = this.viewer?.like,
    )
}

fun ListView.hydrateList(): FeedSourceInfo.ListInfo {
    return FeedSourceInfo.ListInfo(
        uri = this.uri,
        cid = this.cid,
        avatar = this.avatar,
        displayName = this.name,
        description = this.description,
        creatorDid = this.creator.did,
        creatorHandle = this.creator.handle,
        feedDescriptor = FeedDescriptor.List(this.uri),
    )
}

suspend fun app.bsky.actor.SavedFeed.toFeedSourceInfo(agent: ButterflyAgent): Result<FeedSourceInfo> {
    return when(this.type) {
        FeedType.FEED -> {
            agent.api.getFeedGenerator(GetFeedGeneratorQuery(AtUri(this.value)))
                .map { feed ->
                    feed.view.hydrateFeedGenerator().copy(
                        pinned = this.pinned
                    )
                }
        }
        FeedType.LIST -> {
            agent.api.getList(GetListQuery(AtUri(this.value), 1))
                .map { list ->
                    list.list.hydrateList().copy(
                        pinned = this.pinned
                    )
                }
        }
        FeedType.TIMELINE -> Result.success(FeedSourceInfo.Following)
    }
}