package com.morpho.app.util

import androidx.compose.ui.util.fastFilterNotNull
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastMap
import com.atproto.identity.ResolveHandleQuery
import com.morpho.app.model.bluesky.BskyFacet
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.model.bluesky.RichTextFormat
import com.morpho.butterfly.Butterfly
import kotlinx.serialization.Serializable


data class BlueskyText(
    val text: String,
    val facets: List<BskyFacet>
)

@Serializable
sealed interface Segment {
    val text: String
    val start: Int
    val end: Int
    @Serializable
    data class Text(
        val raw: String,
        override val text: String, override val start: Int = 0, override val end: Int = 0,
    ) : Segment

    @Serializable
    data class Escape(
        val raw: String,
        override val text: String, override val start: Int = 0, override val end: Int = 0,
    ) : Segment

    @Serializable
    data class Link(
        val raw: String,
        override val text: String,
        val url: String, override val start: Int = 0, override val end: Int = 0,
    ) : Segment

    @Serializable
    data class MarkdownLink(
        val raw: List<String>,
        override val text: String,
        val url: String,
        val valid: Boolean, override val start: Int = 0, override val end: Int = 0,
    ) : Segment
    @Serializable
    data class Tag(
        val raw: String,
        override val text: String, override val start: Int = 0, override val end: Int = 0,
        val tag: String,
    ) : Segment

    @Serializable
    data class Mention(
        val raw: String,
        override val text: String, override val start: Int = 0, override val end: Int = 0,
        val handle: String,
    ) : Segment


    @Serializable
    data class Format(
        val raw: List<String>,
        override val text: String, override val start: Int = 0, override val end: Int = 0,
        val format: RichTextFormat,
        val valid: Boolean,
    ) : Segment
}


expect fun makeBlueskyText(text: String): BlueskyText

suspend fun resolveBlueskyText(text: BlueskyText, api: Butterfly): Result<BlueskyText> = runCatching {
    val facets:List<BskyFacet> = text.facets.fastFlatMap { facet: BskyFacet ->
        facet.facetType.fastMap {
            if (it is FacetType.UserHandleMention) {
                // Resolve handles
                val response = api.api.resolveHandle(ResolveHandleQuery(it.handle)).getOrNull()
                if (response != null) {
                    val index = facet.facetType.indexOf(it)
                    val facetTypes = facet.facetType.toMutableList()
                    facetTypes[index] = FacetType.UserDidMention(response.did)
                    facet.copy(facetType = facetTypes)
                } else null

            } else {
                facet
            }
        }

    }.fastFilterNotNull()
    return Result.success(BlueskyText(text.text, facets))
}