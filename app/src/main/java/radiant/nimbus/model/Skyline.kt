package radiant.nimbus.model

import app.bsky.feed.FeedViewPost
import radiant.nimbus.util.mapImmutable


data class Skyline(
    var posts: List<SkylineItem>,
    var cursor: String?,
) {
    companion object {
        fun from(
            posts: List<FeedViewPost>,
            cursor: String? = null,
        ): Skyline {
            return Skyline(
                posts = posts.mapImmutable { SkylineItem(it.toPost()) },
                cursor = cursor,
            )
        }

        fun concat(
            skyline: Skyline,
            posts: List<FeedViewPost>,
            cursor: String? = null,
        ): Skyline {
            return Skyline(
                posts = skyline.posts + posts.mapImmutable { SkylineItem(it.toPost()) },
                cursor = cursor,
            )
        }

        fun concat(
            first: Skyline,
            last: Skyline,
            cursor: String? = last.cursor
        ): Skyline {
            return Skyline(
                posts = first.posts + last.posts,
                cursor = cursor,
            )
        }
    }


}
