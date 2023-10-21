package radiant.nimbus.model

import app.bsky.richtext.Facet
import app.bsky.richtext.FacetFeatureUnion
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.Handle
import radiant.nimbus.api.Uri

@Serializable
data class BskyFacet(
    val start: Int,
    val end: Int,
    val target: Target,
)

sealed interface Target {
    @Serializable
    data class UserHandleMention(
        val handle: Handle,
    ) : Target

    @Serializable
    data class UserDidMention(
        val did: Did,
    ) : Target

    @Serializable
    data class ExternalLink(
        val uri: Uri,
    ) : Target

    @Serializable
    data class Tag(
        val tag: String,
    ) : Target

    @Serializable
    data class PollBlueOption(
        val number: Int,
    ) : Target

    @Serializable
    data class Format(
        val format: RichTextFormat
    ) : Target
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
        target = when (val feature = features.first()) {
            is FacetFeatureUnion.Link -> Target.ExternalLink(feature.value.uri)
            is FacetFeatureUnion.Mention -> Target.UserDidMention(feature.value.did)
            is FacetFeatureUnion.Tag -> Target.Tag(feature.value.tag)
            is FacetFeatureUnion.PollBlueOption -> Target.PollBlueOption(feature.value.number)
        },
    )
}