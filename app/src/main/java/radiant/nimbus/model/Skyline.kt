package radiant.nimbus.model

import app.bsky.feed.FeedViewPost
import kotlinx.collections.immutable.ImmutableList
import radiant.nimbus.util.mapImmutable


data class Skyline(
    val posts: ImmutableList<BskyPost>,
    val cursor: String?,
) {
    companion object {
        fun from(
            posts: List<FeedViewPost>,
            cursor: String?,
        ): Skyline {
            return Skyline(
                posts = posts.mapImmutable { it.toPost() },
                cursor = cursor,
            )
        }
    }
}
