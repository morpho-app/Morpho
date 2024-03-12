package com.morpho.app.util

import android.net.Uri
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.atproto.identity.ResolveHandleQueryParams
import com.google.common.base.Utf8
import com.google.common.collect.ImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.ApiProvider
import morpho.app.api.Handle
import morpho.app.api.response.AtpResponse
import morpho.app.model.BskyFacet
import morpho.app.model.FacetType
import morpho.app.model.RichTextFormat
import morpho.app.util.safeUrlParse
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import kotlin.math.max
import kotlin.math.min


data class BlueskyText(
    val text: String,
    val facets: ImmutableList<BskyFacet>
)

fun getGraphemeLength(value: String): Long {
    return value.codePoints().count()
}

sealed interface Segment {
    val text: String
    @Serializable
    data class Text(
        val raw: String,
        override val text: String,
    ) : Segment

    @Serializable
    data class Escape(
        val raw: String,
        override val text: String,
    ) : Segment

    @Serializable
    data class Link(
        val raw: String,
        override val text: String,
        val url: String,
    ) : Segment

    @Serializable
    data class MarkdownLink(
        val raw: List<String>,
        override val text: String,
        val url: String,
        val valid: Boolean,
    ) : Segment
    @Serializable
    data class Tag(
        val raw: String,
        override val text: String,
        val tag: String,
    ) : Segment

    @Serializable
    data class Mention(
        val raw: String,
        override val text: String,
        val handle: String,
    ) : Segment


    @Serializable
    data class Format(
        val raw: List<String>,
        override val text: String,
        val format: RichTextFormat,
        val valid: Boolean,
    ) : Segment
}

data class SegmentedText(
    val segments: List<Segment>,
    val links: List<Uri>,
)

/**
 * Segments text according to mentions, links (bare and Markdown), tags, and potentially formatting characters
 * Intent is to run this repeatedly in the post composer to highlight stuff, ideally with some sort of caching.
 * As a post is sent, then it will resolve it all to actual app.bsky.richtext facets and so on
 */
fun segmentText(text: String): SegmentedText {
    val segments: MutableList<Segment> = mutableListOf()
    var links: MutableList<Uri> = mutableListOf()
    var index = 0
    while(index <= text.lastIndex) {
        val cursor = text[index]
        var parsed = false
        while(!parsed) {
            when(cursor) {
                '@' -> { // Mentions
                    val match = mentionRegex.find(text, min(max(index-2, 0), text.lastIndex))
                    if (match != null) {
                        val handle = match.groups[3]?.value
                        if (handle != null) {
                            segments += Segment.Mention(raw = "@${handle}", text = "@${handle}", handle)
                            parsed = true
                            index += (1 + handle.length)
                        }
                        continue
                    }
                }
                '#' -> { // Hashtags
                    var end = index + 1
                    whitespace@ for (i in end..<text.length) {
                        if (text[i] == ' ' || text[i] == '\n') {
                            end = i
                            break@whitespace
                        }
                    }
                    if (end == index + 1) continue
                    val tag = text.slice((index + 1)..end)
                    segments += Segment.Tag(raw = "#${tag}", text = "#${tag}", tag)
                    index = end
                    parsed = true
                    continue
                }
                '[' -> { // Markdown links
                    val textStart = index + 1
                    var textEnd = textStart
                    var text = ""
                    var textRaw = ""
                    run {
                        var flushed = textStart
                        endBracket@ while(textEnd <= text.lastIndex) {
                           val char = text[textEnd]
                           if(char == ']') {
                               break@endBracket
                           } else if (char == '\\') {
                               val next = text[textEnd + 1]
                               if (next == ']' || next == '\\') {
                                   textRaw += text.slice(flushed..(textEnd + 1))
                                   text += text.slice(flushed..textEnd)
                                   flushed = textEnd + 1
                                   textEnd = flushed
                                   continue@endBracket
                               }
                           }
                           textEnd++
                        }
                        if(text[textEnd] != '[' || text[textEnd + 1] != '(') {
                            return@run
                        }
                        textRaw = text.slice(flushed..textEnd)
                        text = text.slice(flushed..textEnd)
                    }
                    var urlStart = textEnd + 2
                    var urlEnd = urlStart
                    var url = ""
                    var urlRaw = ""
                    run {
                        var flushed = urlStart
                        endParen@ while(urlEnd <= text.lastIndex) {
                            val char = text[urlEnd]
                            if (char == ')') {
                                break@endParen
                            } else if (char == '\\') {
                                val next = text[urlEnd + 1]

                                if(next == ')' || next == '\\') {
                                    urlRaw += text.slice(flushed..(urlEnd + 1))
                                    url += text.slice(flushed..urlEnd)
                                    flushed = urlEnd + 1
                                    urlEnd = flushed
                                    continue@endParen
                                }
                            }
                        }
                        if(text[urlEnd] != ')') {
                            return@run
                        }
                        urlRaw += text.slice(flushed..urlEnd)
                        url += text.slice(flushed..urlEnd)
                    }
                    val urlParsed = safeUrlParse(url)
                    index = urlEnd + 1
                    segments += Segment.MarkdownLink(raw = listOf("[", textRaw, "](", urlRaw, ")"), text = text, url = url, valid = urlParsed != null )
                    if(urlParsed != null) links += urlParsed
                    parsed = true
                    continue
                }
                '\\' -> { // Escaping
                    val next = text[index + 1]
                    if ( next == '@' || next == '#' || next == '[' || next == '\\' ) {
                        val ch = text[index + 1]
                        segments += Segment.Escape("$ch","$ch")
                        index += 2
                        parsed = true
                        continue
                    }
                }
                else -> {
                    if (!parsed) {
                        var end = index + 1
                        parse@while(end <= text.lastIndex) {
                            val char  = text[end]
                            if (char == '\\' || char == '[') break@parse
                            if (char == '@' || char == '#') {
                                val prev = text[end - 1]
                                if (prev != ' ' && prev != '\n') continue@parse
                                break@parse
                            }

                            val match = urlRegex.find(text, min(max(end-2, 0), text.lastIndex))
                            if (match != null && match.groups[3] != null ) {
                                segments += Segment.Link(raw = match.value, text = match.value, url =  match.groups[3]!!.value)
                                val url = match.groups[3]?.value?.let { safeUrlParse(it) }
                                if (url != null) {
                                    links += url
                                }
                                end = match.range.last
                                break@parse
                            } else {
                                if (end-5 > index) {
                                    val raw = text.slice(index..end-5)
                                    val txt = raw.replace(whitespageRegex, "")
                                    segments += Segment.Text(raw = raw, text = txt)
                                }
                                if (index == text.length) break@parse
                            }
                            end++
                        }

                        val raw = text.slice(index..(end+1))
                        val txt = raw.replace(whitespageEofRegex, "")
                        segments += Segment.Text(raw = raw, text = txt)
                        index = end
                        continue
                    }
                }
            }
        }

    }
    return SegmentedText(segments, links)
}

/**
 * Takes segmented text and parses it fully into BlueskyText, with facets
 */
suspend fun makeBlueskyText(segText: SegmentedText, apiProvider: ApiProvider) {
    val segments = segText.segments
    val facets: MutableList<BskyFacet> = mutableListOf()

    var byteLength = 0

    segments.fastForEach {seg ->
        val facetStart = byteLength
        val l = utf8Length(seg.text)
        if (l != null) byteLength += l else return@fastForEach // If the else clause here actually happens, add checks upstream
        val facetEnd = byteLength

        when(seg) {
            is Segment.Format -> return@fastForEach // Come back to this when doing fancy text formatting
            is Segment.Link -> facets += BskyFacet(facetStart, facetEnd, FacetType.ExternalLink(uri = morpho.app.api.Uri(seg.url)))
            is Segment.MarkdownLink -> facets += BskyFacet(facetStart, facetEnd, FacetType.ExternalLink(uri = morpho.app.api.Uri(seg.url)))
            is Segment.Mention -> {
                when(val response = apiProvider.api.resolveHandle(ResolveHandleQueryParams(Handle(seg.handle)))) {
                    is AtpResponse.Failure -> return@fastForEach
                    is AtpResponse.Success -> {
                        facets += BskyFacet(facetStart, facetEnd, FacetType.UserDidMention(response.response.did))
                    }
                }
            }
            is Segment.Tag -> facets += BskyFacet(facetStart, facetEnd, FacetType.Tag(seg.tag))
            else -> return@fastForEach // plaintext or escape characters
        }
    }
}

/**
 * Offline version of the above that just tags the handle without trying to resolve it
 * Potentially useful in the post composer loop
 */
fun makeBlueskyText(segText: SegmentedText) {
    val segments = segText.segments
    val facets: MutableList<BskyFacet> = mutableListOf()

    var byteLength = 0

    segments.fastForEach{ seg ->
        val facetStart = byteLength
        val l = utf8Length(seg.text)
        if (l != null) byteLength += l else return@fastForEach // If the else clause here actually happens, add checks upstream
        val facetEnd = byteLength

        when(seg) {
            is Segment.Format -> return@fastForEach // Come back to this when doing fancy text formatting
            is Segment.Link -> facets += BskyFacet(facetStart, facetEnd, FacetType.ExternalLink(uri = morpho.app.api.Uri(seg.url)))
            is Segment.MarkdownLink -> facets += BskyFacet(facetStart, facetEnd, FacetType.ExternalLink(uri = morpho.app.api.Uri(seg.url)))
            is Segment.Mention -> facets += BskyFacet(facetStart, facetEnd, FacetType.UserHandleMention(Handle(seg.handle)))
            is Segment.Tag -> facets += BskyFacet(facetStart, facetEnd, FacetType.Tag(seg.tag))
            else -> return@fastForEach // plaintext or escape characters
        }
    }
}
fun concatText(segText: SegmentedText): String {

    return segText.segments.fastMap { seg: Segment -> seg.text }.joinToString(separator = "")
}

