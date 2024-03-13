package com.morpho.app.util

import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import com.atproto.identity.ResolveHandleQueryParams
import com.google.common.collect.ImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.ApiProvider
import morpho.app.api.Handle
import morpho.app.api.response.AtpResponse
import morpho.app.model.BskyFacet
import morpho.app.model.FacetType
import morpho.app.model.RichTextFormat
import okio.ByteString.Companion.encodeUtf8


data class BlueskyText(
    val text: String,
    val facets: ImmutableList<BskyFacet>
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


fun makeBlueskyText(text: String): BlueskyText {
    val segments: MutableList<Pair<String, BskyFacet?>> = mutableListOf()
    val mentionMatches = mentionRegex.findAll(text)
    val markdownLinkMatches = markdownLinkRegex.findAll(text)
    val bareLinkMatches = urlRegex.findAll(text).filterNot { matchResult -> markdownLinkMatches.contains(matchResult) }
    val unmatchedText = text.splitToSequence(combinedRegex)

    mentionMatches.forEach { match ->
        val group = match.groups[3]
        if (group != null) {
            val handle = group.value
            match.groups[2]?.range?.let { segments += Pair("@$handle", BskyFacet(it.first, match.range.last, FacetType.UserHandleMention(Handle(handle)))) }
        }
    }
    markdownLinkMatches.forEach {match ->
        val labelMatch = match.groups[1]
        val linkMatch = match.groups[2]
        if (labelMatch != null && linkMatch != null) {
            segments += Pair(labelMatch.value, BskyFacet(labelMatch.range.first, labelMatch.range.last, FacetType.ExternalLink(
                morpho.app.api.Uri(linkMatch.value)
            )))
        }
    }
    bareLinkMatches.forEach { match ->
        segments += Pair(match.value, BskyFacet(match.range.first, match.range.last, FacetType.ExternalLink(morpho.app.api.Uri(match.value))))
    }
    val outString = StringBuilder(text.length)
    if (segments.first().second?.start == 0) {
        unmatchedText.forEachIndexed { index, s ->
            outString.append(s)
            outString.append(segments[index].first)
            segments.add(index + 1, Pair(s, null))
        }
    } else {
        unmatchedText.forEachIndexed { index, s ->
            outString.append(segments[index].first)
            outString.append(s)
            segments.add(index, Pair(s, null))
        }
    }
    val bytes = outString.toString().encodeUtf8()
    var byteIndex = 0
    val facets = segments.fastMap {
        val byteString = it.first.encodeUtf8()
        byteIndex += byteString.size
        if (it.second != null) {
            val facet = it.second
            val start = bytes.indexOf(byteString)
            val end = start + byteString.size
            return@fastMap BskyFacet(start, end, facet!!.facetType)
        } else return@fastMap null

    }.filterNotNull()

    return BlueskyText( outString.toString(), facets as ImmutableList<BskyFacet>)
}

suspend fun resolveBlueskyText(text: BlueskyText, apiProvider: ApiProvider): BlueskyText {
    val facets = text.facets.fastMap { facet ->
        if (facet.facetType is FacetType.UserHandleMention) {
            // Resolve handles
            when(val response = apiProvider.api.resolveHandle(ResolveHandleQueryParams(facet.facetType.handle))) {
                is AtpResponse.Failure -> facet
                is AtpResponse.Success -> {
                    BskyFacet(facet.start, facet.end, FacetType.UserDidMention(response.response.did))
                }
            }
        } else {
            facet
        }
    }.fastFilter { facet -> facet.facetType !is FacetType.UserHandleMention } // delete facets for any that couldn't be resolved
    return BlueskyText(text.text, facets as ImmutableList<BskyFacet>)
}