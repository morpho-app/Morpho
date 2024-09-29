package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.feed.Post
import com.morpho.app.model.uidata.Moment
import com.morpho.app.model.uidata.MomentParceler
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.Language
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.serialization.Serializable

@Parcelize
@Immutable
@Serializable
data class LitePost(
    val text: String,
    val facets: List<BskyFacet>,
    val feature: BskyPostFeature?,
    val langs: List<Language>,
    @TypeParceler<Moment, MomentParceler>()
    val createdAt: Moment,
): Parcelable

fun Post.toLitePost(): LitePost {
    return LitePost(
        text = text,
        facets = facets.mapImmutable { it.toBskyFacet() },
        createdAt = Moment(createdAt),
        feature = embed?.toFeature(),
        langs = langs,
    )
}


