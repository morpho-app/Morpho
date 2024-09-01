package com.morpho.app.model.bluesky

import app.bsky.feed.Post
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.Language
import kotlinx.serialization.Serializable

@Serializable
data class LitePost(
    val text: String,
    val facets: List<BskyFacet>,
    val feature: BskyPostFeature?,
    val langs: List<Language>,
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


