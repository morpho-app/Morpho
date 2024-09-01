package com.morpho.app.ui.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.morpho.app.model.bluesky.BskyFacet
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.model.bluesky.RichTextFormat.*
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.min

@Composable
fun RichTextElement(
    text: String,
    modifier: Modifier = Modifier,
    facets: List<BskyFacet> = persistentListOf(),
    onClick: (List<FacetType>) -> Unit = {},
    maxLines: Int = 20,

    ) {
    val formattedText = buildAnnotatedString {
        pushStyle(SpanStyle(MaterialTheme.colorScheme.onSurface))
        append(text)
        facets.fastForEach { facet ->
            facet.facetType.fastForEach { facetType ->
                when(facetType) {
                    is FacetType.ExternalLink -> {
                        addStringAnnotation(tag = "Link", facetType.uri.uri, min(facet.start, text.length-1), min(facet.end, text.length-1))
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = min(facet.start, text.length-1),
                            end = min(facet.end, text.length-1)
                        )
                    }
                    is FacetType.PollBlueOption -> {
                        addStringAnnotation(tag = "PollBlue", facetType.number.toString(), min(facet.start, text.length-1), min(facet.end, text.length-1))
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = min(facet.start, text.length-1),
                            end = min(facet.end, text.length-1)
                        )
                    }

                    is FacetType.Tag -> {
                        addStringAnnotation(tag = "Tag", facetType.tag, min(facet.start, text.length-1), min(facet.end, text.length-1))
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = min(facet.start, text.length-1),
                            end = min(facet.end, text.length-1)
                        )
                    }
                    is FacetType.UserDidMention -> {
                        addStringAnnotation(tag = "Mention", facetType.did.did, min(facet.start, text.length-1), min(facet.end, text.length-1))
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = min(facet.start, text.length-1),
                            end = min(facet.end, text.length-1)
                        )
                    }
                    is FacetType.UserHandleMention -> {
                        addStringAnnotation(tag = "Mention", facetType.handle.handle, min(facet.start, text.length-1), min(facet.end, text.length-1))
                        addStyle(
                            style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                            start = min(facet.start, text.length-1),
                            end = min(facet.end, text.length-1)
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
                            start = min(facet.start, text.length-1),
                            end = min(facet.end, text.length-1)
                        )
                    }
                    is FacetType.BlueMoji -> {
                        // TODO: Add BlueMoji support
                    }

                    else -> {}
                }
            }


        }
        toAnnotatedString()
    }
    SelectionContainer(
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
    ) {
        ClickableText(
            text = formattedText,
            onClick = { offset ->
                facets.forEach {
                    if (it.start <= offset && offset <= it.end) {
                        return@ClickableText onClick(it.facetType)
                    }
                }
                onClick(listOf())
            },
            maxLines = maxLines, // Sorry @retr0.id, no more 200 line posts.
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }

}