package radiant.nimbus.model

import app.bsky.feed.FeedViewPostReasonUnion

sealed interface BskyPostReason {
    data class BskyPostRepost(
        val repostAuthor: Profile,
        val indexedAt: Moment,
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