package morpho.app.ui.elements

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
import kotlinx.collections.immutable.persistentListOf
import morpho.app.model.BskyFacet
import morpho.app.model.RichTextFormat.BOLD
import morpho.app.model.RichTextFormat.ITALIC
import morpho.app.model.RichTextFormat.STRIKETHROUGH
import morpho.app.model.RichTextFormat.UNDERLINE
import morpho.app.model.FacetType
import kotlin.math.min

@Composable
fun RichTextElement(
    text: String,
    modifier: Modifier = Modifier,
    facets: List<BskyFacet> = persistentListOf(),
    onClick: (FacetType?) -> Unit = {},
    maxLines: Int = 20,

    ) {
    val formattedText = buildAnnotatedString {
        pushStyle(SpanStyle(MaterialTheme.colorScheme.onSurface))
        append(text)
        facets.forEach {
            when(it.facetType) {
                is FacetType.ExternalLink -> {
                    addStringAnnotation(tag = "Link", it.facetType.uri.uri, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is FacetType.PollBlueOption -> {
                    addStringAnnotation(tag = "PollBlue", it.facetType.number.toString(), min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }

                is FacetType.Tag -> {
                    addStringAnnotation(tag = "Tag", it.facetType.tag, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is FacetType.UserDidMention -> {
                    addStringAnnotation(tag = "Mention", it.facetType.did.did, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is FacetType.UserHandleMention -> {
                    addStringAnnotation(tag = "Mention", it.facetType.handle.handle, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is FacetType.Format -> {
                    val style = when(it.facetType.format) {
                        BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                        ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                        STRIKETHROUGH ->SpanStyle(textDecoration = TextDecoration.LineThrough)
                        UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                    }
                    addStyle(
                        style = style,
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }

                else -> {}
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
                onClick(null)
            },
            maxLines = maxLines, // Sorry @retr0.id, no more 200 line posts.
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }

}