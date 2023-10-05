package radiant.nimbus.model

import app.bsky.feed.FeedViewPost
import radiant.nimbus.util.mapImmutable


data class Skyline(
    val posts: List<SkylineItem>,
    val cursor: String?,
) {
    companion object {
        fun from(
            posts: List<FeedViewPost>,
            cursor: String?,
        ): Skyline {
            return Skyline(
                posts = posts.mapImmutable { SkylineItem(it.toPost()) },
                cursor = cursor,
            )
        }
    }
}
