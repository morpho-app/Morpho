package com.morpho.app.model.bluesky

import app.bsky.feed.FeedViewPostReasonUnion
import app.bsky.feed.SkeletonFeedPostReasonUnion
import com.morpho.app.model.uidata.Moment
import com.morpho.butterfly.AtUri

sealed interface BskyPostReason {
    data class BskyPostRepost(
        val repostAuthor: Profile,
        val indexedAt: Moment,
    ) : BskyPostReason

    data class BskyPostFeedPost(
        val repost: AtUri
    ) : BskyPostReason

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