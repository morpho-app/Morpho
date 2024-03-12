package morpho.app.model

import app.bsky.richtext.Facet
import app.bsky.richtext.FacetFeatureUnion
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle
import morpho.app.api.Uri

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