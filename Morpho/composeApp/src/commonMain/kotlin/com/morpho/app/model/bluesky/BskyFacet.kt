package com.morpho.app.model.bluesky

import app.bsky.richtext.*
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.Uri
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.serialization.Serializable


@Serializable
data class BskyFacet(
    val start: Int,
    val end: Int,
    val facetType: List<FacetType>,
)

@Serializable
sealed interface FacetType {
    @Serializable
    data class UserHandleMention(
        val handle: Handle,
    ) : FacetType

    @Serializable
    data class UserDidMention(
        val did: Did,
    ) : FacetType

    @Serializable
    data class ExternalLink(
        val uri: Uri,
    ) : FacetType


    @Serializable
    data class Tag(
        val tag: String,
    ) : FacetType

    @Serializable
    data class PollBlueOption(
        val number: Int,
    ) : FacetType

    @Serializable
    data object PollBlueQuestion : FacetType

    @Serializable
    data class Format(
        val format: RichTextFormat
    ) : FacetType

    @Serializable
    data class BlueMoji(
        val did: Did,
        val formats: BlueMojiFormatUnion,
        val name: String,
    ) : FacetType

    @Serializable
    data class UnknownFacet(
        val value: String,
    ) : FacetType

}

@Serializable
enum class RichTextFormat {
    BOLD,
    ITALIC,
    STRIKETHROUGH,
    UNDERLINE,
}


fun Facet.toBskyFacet(): BskyFacet {
    return BskyFacet(
        start = index.byteStart.toInt(),
        end = index.byteEnd.toInt(),
        facetType = features.map {
            when (it) {
                is FacetFeatureUnion.Tag -> FacetType.Tag(it.value.tag)
                is FacetFeatureUnion.Mention -> FacetType.UserDidMention(it.value.did)
                is FacetFeatureUnion.Link -> FacetType.ExternalLink(it.value.uri)
                is FacetFeatureUnion.PollBlueOption -> FacetType.PollBlueOption(it.value.number)
                is FacetFeatureUnion.PollBlueQuestion -> FacetType.PollBlueQuestion
                is FacetFeatureUnion.BlueMojiFacet -> FacetType.BlueMoji(it.value.did, it.value.formats, it.value.name)
                else -> FacetType.UnknownFacet(it.toString())
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
                is FacetType.BlueMoji -> FacetFeatureUnion.BlueMojiFacet(
                    BlueMoji(it.did, it.formats, it.name)
                )
                is FacetType.UnknownFacet -> null
                else -> throw IllegalArgumentException("Unknown facet type: $it")
            }
        } as ReadOnlyList<FacetFeatureUnion>
    )
}