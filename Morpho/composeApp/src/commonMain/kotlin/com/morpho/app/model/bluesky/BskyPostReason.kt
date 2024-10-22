package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.feed.FeedViewPostReasonUnion
import app.bsky.feed.SkeletonFeedPostReasonUnion
import com.morpho.app.model.uidata.Moment
import com.morpho.app.model.uidata.MomentParceler
import com.morpho.butterfly.AtUri
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.serialization.Serializable

@Parcelize
@Immutable
@Serializable
sealed interface BskyPostReason: Parcelable {
    @Immutable
    @Serializable
    data class BskyPostRepost(
        val repostAuthor: Profile,
        @TypeParceler<Moment, MomentParceler>()
        val indexedAt: Moment,
    ) : BskyPostReason

    @Immutable
    @Serializable
    data class BskyPostFeedPost(
        val repost: AtUri
    ) : BskyPostReason

    @Immutable
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