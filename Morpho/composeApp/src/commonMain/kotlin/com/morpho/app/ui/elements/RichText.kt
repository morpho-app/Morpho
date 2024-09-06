package com.morpho.app.ui.elements

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.morpho.app.model.bluesky.BskyFacet
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.model.bluesky.RichTextFormat.*
import com.morpho.app.util.utf16FacetIndex
import kotlinx.collections.immutable.persistentListOf
import okio.ByteString.Companion.encodeUtf8


@Composable
fun RichTextElement(
    text: String,
    modifier: Modifier = Modifier,
    facets: List<BskyFacet> = persistentListOf(),
    onClick: (List<FacetType>) -> Unit = {},
    maxLines: Int = 20,

    ) {
    val utf8Text = text.encodeUtf8()
    val splitText = text.split("◌").listIterator() // special BlueMoji character
    val formattedText = buildAnnotatedString {
        pushStyle(SpanStyle(MaterialTheme.colorScheme.onSurface))
        append(splitText.next())
        facets.fastForEach { facet ->
            val bounds = text.utf16FacetIndex(utf8Text, facet.start, facet.end)
            val start = bounds.first
            val end = bounds.second
            facet.facetType.fastForEach { facetType ->
                when(facetType) {
                    is FacetType.ExternalLink -> {
                        addStringAnnotation(tag = "Link", facetType.uri.uri, start, end)
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = start,
                            end = end
                        )
                    }
                    is FacetType.PollBlueOption -> {
                        addStringAnnotation(tag = "PollBlue", facetType.number.toString(), start, end)
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = start,
                            end = end
                        )
                    }

                    is FacetType.Tag -> {
                        addStringAnnotation(tag = "Tag", facetType.tag, start, end)
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = start,
                            end = end
                        )
                    }
                    is FacetType.UserDidMention -> {
                        addStringAnnotation(tag = "Mention", facetType.did.did, start, end)
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = start,
                            end = end
                        )
                    }
                    is FacetType.UserHandleMention -> {
                        addStringAnnotation(tag = "Mention", facetType.handle.handle, start, end)
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = start,
                            end = end
                        )
                    }
                    is FacetType.Format -> {
                        val style = when(facetType.format) {
                            BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                            ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                            STRIKETHROUGH ->SpanStyle(textDecoration = TextDecoration.LineThrough)
                            UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                        }
                        addStyle(
                            style = style,
                            start = start,
                            end = end
                        )
                    }
                    is FacetType.BlueMoji -> {
                        appendInlineContent(facetType.image.url, facetType.name)
                        append(splitText.next())
                    }

                    else -> {}
                }
            }


        }
        // Add the rest of the text in case the BlueMoji character was a false alarm
        while(splitText.hasNext()) {
            // Put the placeholder back in
            append("◌")
            append(splitText.next())
        }
        toAnnotatedString()
    }
    val inlineContentMap = remember {
        facets.fastMap { facet ->
            facet.facetType.fastMap { facetType ->
                when(facetType) {
                    is FacetType.BlueMoji -> {
                        Pair(
                            facetType.image.url,
                            InlineTextContent(
                                Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(
                                        LocalPlatformContext.current)
                                        .data(facetType.image.url).size(128, 128)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = facetType.name,
                                    modifier = Modifier
                                )
                            }
                        )
                    }

                    else -> {
                        Pair("", InlineTextContent(
                            Placeholder(1.sp, 1.sp, PlaceholderVerticalAlign.TextCenter)
                        ){})
                    }
                }

            }
        }.flatten().filter { it.first.isNotEmpty() }.toMap()

    }
    SelectionContainer(
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
    ) {
        val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
        val pressIndicator = Modifier.pointerInput(onClick) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    val offset = layoutResult.getOffsetForPosition(pos)
                    facets.forEach {
                        if (it.start <= offset && offset <= it.end) {
                            return@detectTapGestures onClick(it.facetType)
                        }
                    }
                    onClick(listOf())
                }
            }
        }
        BasicText(
            text = formattedText,
            inlineContent = inlineContentMap,
            maxLines = maxLines, // Sorry @retr0.id, no more 200 line posts.
            overflow = TextOverflow.Ellipsis,
            modifier = modifier.then(pressIndicator),
        )
    }

}