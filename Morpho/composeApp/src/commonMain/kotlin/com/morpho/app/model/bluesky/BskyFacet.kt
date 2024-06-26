package com.morpho.app.model.bluesky

import app.bsky.richtext.*
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.Uri
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable


@Serializable
data class BskyFacet(
    val start: Int,
    val end: Int,
    val facetType: FacetType,
)

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
}

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
        facetType = when (val feature = features.first()) {
            is FacetFeatureUnion.Link -> FacetType.ExternalLink(feature.value.uri)
            is FacetFeatureUnion.Mention -> FacetType.UserDidMention(feature.value.did)
            is FacetFeatureUnion.Tag -> FacetType.Tag(feature.value.tag)
            is FacetFeatureUnion.PollBlueOption -> FacetType.PollBlueOption(feature.value.number)
            is FacetFeatureUnion.PollBlueQuestion -> FacetType.PollBlueQuestion
        },
    )
}

@Suppress("UNCHECKED_CAST")
fun BskyFacet.toFacet(): Facet {
    return Facet(
        index = FacetByteSlice(start.toLong(), end.toLong()),
        features = when (facetType) {
            is FacetType.Tag -> persistentListOf(FacetFeatureUnion.Tag(FacetTag(facetType.tag)))
            is FacetType.UserDidMention -> persistentListOf(FacetFeatureUnion.Mention(FacetMention(facetType.did)))
            is FacetType.ExternalLink -> persistentListOf(FacetFeatureUnion.Link(FacetLink(facetType.uri)))
            else-> persistentListOf(FacetFeatureUnion)
        } as ReadOnlyList<FacetFeatureUnion>
    )
}