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
import morpho.app.model.Target
import kotlin.math.min

@Composable
fun RichTextElement(
    text: String,
    modifier: Modifier = Modifier,
    facets: List<BskyFacet> = persistentListOf(),
    onClick: (Target?) -> Unit = {},
    maxLines: Int = 20,

) {
    val formattedText = buildAnnotatedString {
        pushStyle(SpanStyle(MaterialTheme.colorScheme.onSurface))
        append(text)
        facets.forEach {
            when(it.target) {
                is Target.ExternalLink -> {
                    addStringAnnotation(tag = "Link", it.target.uri.uri, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is Target.PollBlueOption -> {
                    addStringAnnotation(tag = "PollBlue", it.target.number.toString(), min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }

                is Target.Tag -> {
                    addStringAnnotation(tag = "Tag", it.target.tag, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is Target.UserDidMention -> {
                    addStringAnnotation(tag = "Mention", it.target.did.did, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is Target.UserHandleMention -> {
                    addStringAnnotation(tag = "Mention", it.target.handle.handle, min(it.start, text.length-1), min(it.end, text.length-1))
                    addStyle(
                        style = SpanStyle(MaterialTheme.colorScheme.tertiary),
                        start = min(it.start, text.length-1),
                        end = min(it.end, text.length-1)
                    )
                }
                is Target.Format -> {
                    val style = when(it.target.format) {
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
                        return@ClickableText onClick(it.target)
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