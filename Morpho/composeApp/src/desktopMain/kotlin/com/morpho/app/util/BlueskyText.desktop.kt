package com.morpho.app.util

import androidx.compose.ui.util.fastMap
import com.morpho.app.model.bluesky.BskyFacet
import com.morpho.app.model.bluesky.FacetType
import com.morpho.butterfly.Handle
import com.morpho.butterfly.Uri
import kotlinx.collections.immutable.toImmutableList
import okio.ByteString.Companion.encodeUtf8

actual fun makeBlueskyText(text: String): BlueskyText {
    val segments: MutableList<Pair<String, BskyFacet?>> = mutableListOf()
    val mentionMatches = mentionRegex.findAll(text)
    val markdownLinkMatches = markdownLinkRegex.findAll(text)
    val bareLinkMatches = urlRegex.findAll(text).filterNot { matchResult -> markdownLinkMatches.contains(matchResult) }
    val unmatchedText = text.splitToSequence(combinedRegex)

    mentionMatches.forEach { match: MatchResult ->
        val group = match.groups[3]
        if (group != null) {
            val handle = group.value
            match.groups[2]?.range?.let { segments += Pair("@$handle", BskyFacet(it.first, match.range.last, FacetType.UserHandleMention(
                Handle(handle)
            ))
            ) }
        }
    }
    markdownLinkMatches.forEach {match ->
        val labelMatch = match.groups[1]
        val linkMatch = match.groups[2]
        if (labelMatch != null && linkMatch != null) {
            segments += Pair(labelMatch.value, BskyFacet(labelMatch.range.first, labelMatch.range.last, FacetType.ExternalLink(
                Uri(linkMatch.value)
            ))
            )
        }
    }
    bareLinkMatches.forEach { match ->
        segments += Pair(match.value, BskyFacet(match.range.first, match.range.last, FacetType.ExternalLink(
            Uri(match.value)
        ))
        )
    }
    val outString = StringBuilder(text.length)
    if (segments.isEmpty()) {
        outString.append(text)
    } else if (segments.first().second?.start == 0) {
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

    return BlueskyText( outString.toString(), facets.toImmutableList())
}