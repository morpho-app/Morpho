package radiant.nimbus.model

import app.bsky.feed.Post
import kotlinx.collections.immutable.ImmutableList
import radiant.nimbus.util.mapImmutable

data class LitePost(
    val text: String,
    val links: ImmutableList<BskyPostLink>,
    val createdAt: Moment,
)

fun Post.toLitePost(): LitePost {
    return LitePost(
        text = text,
        links = facets.mapImmutable { it.toLink() },
        createdAt = Moment(createdAt),
    )
}