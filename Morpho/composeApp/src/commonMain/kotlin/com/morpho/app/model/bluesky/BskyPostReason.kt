package com.morpho.app.model.bluesky

import app.bsky.feed.FeedViewPostReasonUnion
import app.bsky.feed.SkeletonFeedPostReasonUnion
import com.morpho.app.model.uidata.Moment
import com.morpho.butterfly.AtUri
import kotlinx.serialization.Serializable

@Serializable
sealed interface BskyPostReason {
    @Serializable
    data class BskyPostRepost(
        val repostAuthor: Profile,
        val indexedAt: Moment,
    ) : BskyPostReason

    @Serializable
    data class BskyPostFeedPost(
        val repost: AtUri
    ) : BskyPostReason

    @Serializable
    data class SourceFeed(
        val feed: FeedGenerator
    ) : BskyPostReason
}

fun FeedViewPostReasonUnion.toReason(): BskyPostReason {
    return when (this) {
        is FeedViewPostReasonUnion.ReasonRepost -> {
            BskyPostReason.BskyPostRepost(
                repostAuthor = value.by.toProfile(),
                indexedAt = Moment(value.indexedAt),
            )
        }
    }
}

fun SkeletonFeedPostReasonUnion.toReason() : BskyPostReason {
    return when (this) {
        is SkeletonFeedPostReasonUnion.SkeletonReasonRepost -> {
            BskyPostReason.BskyPostFeedPost(
                repost = value.repost
            )
        }
    }
}