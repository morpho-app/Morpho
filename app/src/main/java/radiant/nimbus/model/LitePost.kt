package radiant.nimbus.model

import app.bsky.feed.Post
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.util.mapImmutable

@Serializable
data class LitePost(
    val text: String,
    val links: ImmutableList<BskyPostLink>,
    val feature: BskyPostFeature?,
    val langs: ImmutableList<sh.christian.ozone.api.Language>,
    val createdAt: Moment,
)

fun Post.toLitePost(): LitePost {
    return LitePost(
        text = text,
        links = facets.mapImmutable { it.toLink() },
        createdAt = Moment(createdAt),
        feature = embed?.toFeature(),
        langs = langs,
    )
}


