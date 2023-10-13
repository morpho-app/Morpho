package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.bsky.feed.Post
import app.bsky.feed.PostEmbedUnion
import app.bsky.feed.PostReplyRef
import com.atproto.repo.StrongRef
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import radiant.nimbus.api.Language
import radiant.nimbus.model.BskyPost
import radiant.nimbus.ui.post.ComposerPostFragment
import radiant.nimbus.ui.post.testPost
import radiant.nimbus.ui.theme.NimbusTheme


enum class ComposerRole {
    StandalonePost,
    Reply,
    QuotePost,
    ThreadBuilder,
}



@Composable
fun PostComposer(
    modifier: Modifier = Modifier,
    initialContent: BskyPost? = null,
    role: ComposerRole = ComposerRole.StandalonePost,
    onSend: (Post) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var postText by rememberSaveable { mutableStateOf("") }
    val replyRef = remember { if(initialContent != null && role == ComposerRole.Reply) {
        val root = if (initialContent.reply?.root != null) {
            StrongRef(initialContent.reply.root.uri,initialContent.reply.root.cid)
        } else if (initialContent.reply?.parent != null) {
            StrongRef(initialContent.reply.parent.uri,initialContent.reply.parent.cid)
        } else {
            StrongRef(initialContent.uri,initialContent.cid)
        }
        val parent = StrongRef(initialContent.uri,initialContent.cid)
        PostReplyRef(root, parent)
    } else null }
    var quoteRef = remember { if(initialContent != null && role == ComposerRole.QuotePost) {
        StrongRef(initialContent.uri,initialContent.cid)
    } else null}
    val submitText = rememberSaveable {
        when(role) {
            ComposerRole.StandalonePost -> "Post"
            ComposerRole.Reply -> "Reply"
            ComposerRole.QuotePost -> "Quote Post"
            ComposerRole.ThreadBuilder -> "Post"
        }

    }
    val textLeft = remember { 300 - postText.codePoints().count() }
    val textFractionUsed: Float = remember { postText.codePoints().count() / 300.0f }
    Column(
        Modifier
            .imePadding(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(0.1f))
            Button(
                onClick = {
                    onSend(
                        Post(
                            text = postText,
                            reply = replyRef,
                            embed = if(quoteRef != null) {
                                PostEmbedUnion.Record(value = app.bsky.embed.Record(quoteRef))
                            } else null,
                            createdAt = Clock.System.now(),
                            // Placeholder until can pull the system language or check user prefs
                            langs = persistentListOf(Language("en")),
                            //Deal with facets, etc. later

                        )
                    )
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(submitText)
            }
        }
        Surface(
            tonalElevation = 2.dp
        ) {
            Column {
                if (replyRef != null && initialContent != null) {
                    ComposerPostFragment(
                        post = initialContent,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .padding(horizontal = 4.dp)
                    )
                }

                OutlinedTextField(
                    modifier = Modifier
                        .imePadding()
                        .fillMaxWidth()
                        .padding(4.dp),

                    value = postText,
                    placeholder = { Text(text = "Write something...") },
                    onValueChange = { postText = it},
                    supportingText = if (quoteRef != null && initialContent != null) {
                        { ComposerPostFragment(post = initialContent) } } else null,
                    keyboardOptions = KeyboardOptions(autoCorrect = true),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = "Select Image")
            }
            Spacer(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(0.1f))

            Text(text = textLeft.toString(), style = MaterialTheme.typography.labelLarge)
            CircularProgressIndicator(
                progress = textFractionUsed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(45.dp)
                    .padding(8.dp),

            )
        }


    }

}


@Preview
@Composable
fun PreviewQuotePost() {
    NimbusTheme(darkTheme = false) {
        Surface {
            PostComposer(initialContent = testPost, role = ComposerRole.QuotePost)
        }
    }
}

@Preview
@Composable
fun PreviewReply() {
    NimbusTheme(darkTheme = false) {
        Surface {
            PostComposer(initialContent = testPost, role = ComposerRole.Reply)
        }
    }
}