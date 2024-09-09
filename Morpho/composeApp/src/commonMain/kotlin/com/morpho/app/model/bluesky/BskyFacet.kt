package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.richtext.*
import com.atproto.label.SelfLabels
import com.morpho.app.util.didCidToImageLink
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.Uri
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class BskyFacet(
    val start: Int,
    val end: Int,
    val facetType: List<FacetType>,
)

@Immutable
@Serializable
sealed interface FacetType {
    @Immutable
    @Serializable
    data class UserHandleMention(
        val handle: Handle,
    ) : FacetType

    @Immutable
    @Serializable
    data class UserDidMention(
        val did: Did,
    ) : FacetType

    @Immutable
    @Serializable
    data class ExternalLink(
        val uri: Uri,
    ) : FacetType

    @Immutable
    @Serializable
    data class Tag(
        val tag: String,
    ) : FacetType

    @Immutable
    @Serializable
    data class PollBlueOption(
        val number: Int,
    ) : FacetType

    @Immutable
    @Serializable
    data object PollBlueQuestion : FacetType

    @Immutable
    @Serializable
    data class Format(
        val format: RichTextFormat
    ) : FacetType

    @Immutable
    @Serializable
    data class BlueMoji(
        val did: Did,
        val image: BlueMojiImageLink,
        val name: String,
        val alt: String? = null,
        val adultOnly: Boolean? = false,
        val labels: List<BskyLabel>? = null,
    ) : FacetType

    @Immutable
    @Serializable
    data class UnknownFacet(
        val value: String,
    ) : FacetType

}

@Immutable
@Serializable
sealed interface BlueMojiImageLink {
    val url: String
    val apng: Boolean
    val lottie: Boolean

    @Immutable
    @Serializable
    data class Png(
        override val url: String,
        override val apng: Boolean = false,
        override val lottie: Boolean = false
    ) : BlueMojiImageLink

    @Immutable
    @Serializable
    data class Webp(
        override val url: String,
        override val apng: Boolean = false,
        override val lottie: Boolean = false
    ) : BlueMojiImageLink

    @Immutable
    @Serializable
    data class Gif(
        override val url: String,
        override val apng: Boolean = false,
        override val lottie: Boolean = false
    ) : BlueMojiImageLink

}

@Immutable
@Serializable
enum class RichTextFormat {
    BOLD,
    ITALIC,
    STRIKETHROUGH,
    UNDERLINE,
}


fun BlueMojiFormatUnion.toLink(did: Did): BlueMojiImageLink {
    return when (this) {
        is BlueMojiFormatUnion.BlueMojiFormatV0 -> {
            val type = if (apng128 == true) "image/png" else "jpg"
            if (lottie == true) {
                /// Currently we don't support lottie
                throw IllegalArgumentException("Lottie is not supported")
            } else {
                when {
                    png128 != null -> BlueMojiImageLink.Png(
                        didCidToImageLink(did, png128!!, false, type),
                        apng128 ?: false,
                        lottie ?: false
                    )
                    webp128 != null -> BlueMojiImageLink.Webp(
                        didCidToImageLink(did, webp128!!, false, type),
                        apng128 ?: false,
                        lottie ?: false
                    )
                    gif128 != null -> BlueMojiImageLink.Gif(
                        didCidToImageLink(did, gif128!!, false, type),
                        apng128 ?: false,
                        lottie ?: false
                    )
                    else -> throw IllegalArgumentException("No image link found")
                }
            }
        }
    }
}


fun BlueMojiImageLink.toFormat(): BlueMojiFormatUnion {
    return when (this) {
        is BlueMojiImageLink.Png -> BlueMojiFormatUnion.BlueMojiFormatV0(
            png128 = Cid(url.substringAfterLast("/")),
            apng128 = apng,
            lottie = lottie
        )
        is BlueMojiImageLink.Webp -> BlueMojiFormatUnion.BlueMojiFormatV0(
            webp128 = Cid(url.substringAfterLast("/")),
            apng128 = apng,
            lottie = lottie
        )
        is BlueMojiImageLink.Gif -> BlueMojiFormatUnion.BlueMojiFormatV0(
            gif128 = Cid(url.substringAfterLast("/")),
            apng128 = apng,
            lottie = lottie
        )
    }
}

fun Facet.toBskyFacet(): BskyFacet {
    return BskyFacet(
        start = index.byteStart.toInt(),
        end = index.byteEnd.toInt(),
        facetType = features.map { facetType ->
            when (facetType) {
                is FacetFeatureUnion.Tag -> FacetType.Tag(facetType.value.tag)
                is FacetFeatureUnion.Mention -> FacetType.UserDidMention(facetType.value.did)
                is FacetFeatureUnion.Link -> FacetType.ExternalLink(facetType.value.uri)
                is FacetFeatureUnion.PollBlueOption -> FacetType.PollBlueOption(facetType.value.number)
                is FacetFeatureUnion.PollBlueQuestion -> FacetType.PollBlueQuestion
                is FacetFeatureUnion.BlueMojiFacet -> FacetType.BlueMoji(
                    facetType.value.did, facetType.value.formats.toLink(facetType.value.did),
                    facetType.value.name, facetType.value.alt,
                    facetType.value.adultOnly,
                    facetType.value.labels?.values?.map { it.toLabel(facetType.value.did) }
                )
                else -> FacetType.UnknownFacet(facetType.toString())
            }
        }
    )
}

@Suppress("UNCHECKED_CAST")
fun BskyFacet.toFacet(): Facet {
    return Facet(
        index = FacetByteSlice(start.toLong(), end.toLong()),
        features = facetType.mapNotNull {
            when (it) {
                is FacetType.Tag -> FacetFeatureUnion.Tag(FacetTag(it.tag))
                is FacetType.UserDidMention -> FacetFeatureUnion.Mention(FacetMention(it.did))
                is FacetType.ExternalLink -> FacetFeatureUnion.Link(FacetLink(it.uri))
                is FacetType.PollBlueOption -> FacetFeatureUnion.PollBlueOption(
                    PollBlueOptionFacet(
                        it.number
                    )
                )
                is FacetType.PollBlueQuestion -> FacetFeatureUnion.PollBlueQuestion
                is FacetType.BlueMoji -> {
                    val selfLabels = it.labels?.map { label -> label.toSelfLabel() }?.toImmutableList()
                    FacetFeatureUnion.BlueMojiFacet(
                        BlueMoji(it.did, it.image.toFormat(), it.name, it.alt, it.adultOnly,
                                 selfLabels?.let { labels -> SelfLabels(labels) }
                        ))
                }
                is FacetType.UnknownFacet -> null
                else -> throw IllegalArgumentException("Unknown facet type: $it")
            }
        }.toImmutableList() as ReadOnlyList<FacetFeatureUnion>
    )
}