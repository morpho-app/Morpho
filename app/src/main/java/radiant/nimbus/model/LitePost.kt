package radiant.nimbus.model

import app.bsky.feed.Post
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Language
import radiant.nimbus.util.mapImmutable

@Serializable
data class LitePost(
    val text: String,
    val facets: ImmutableList<BskyFacet>,
    val feature: BskyPostFeature?,
    val langs: ImmutableList<Language>,
    val createdAt: Moment,
)

fun Post.toLitePost(): LitePost {
    return LitePost(
        text = text,
        facets = facets.mapImmutable { it.toBskyFacet() },
        createdAt = Moment(createdAt),
        feature = embed?.toFeature(),
        langs = langs,
    )
}


