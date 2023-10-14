package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import radiant.nimbus.api.Language
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.DraftPost
import radiant.nimbus.ui.post.ComposerPostFragment
import radiant.nimbus.ui.post.testPost
import radiant.nimbus.ui.theme.NimbusTheme


enum class ComposerRole {
    StandalonePost,
    Reply,
    QuotePost,
    ThreadBuilder,
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPostComposer(
    modifier: Modifier = Modifier,
    onDismissRequest: ()-> Unit = {},
    initialContent: BskyPost? = null,
    role: ComposerRole = ComposerRole.StandalonePost,
    draft: DraftPost = DraftPost(),
    onSend: (Post) -> Unit = {},
    onCancel: () -> Unit = {},
    onUpdate: (DraftPost) -> Unit = {},
    sheetState:SheetState = rememberModalBottomSheetState(),
) {
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = { scope.launch { sheetState.hide() }
            .invokeOnCompletion {
                if (!sheetState.isVisible) { onDismissRequest() } } },
        containerColor = MaterialTheme.colorScheme.background,
        sheetState = sheetState,
        windowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),

        ){
        PostComposer(
            role = role,
            modifier = modifier,
            initialContent = initialContent,
            draft = draft,
            onCancel = { scope.launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {onCancel() } } },
            onSend = { onSend(it) },
            onUpdate = { onUpdate(it) }
        )
    }
}


@Composable
fun PostComposer(
    modifier: Modifier = Modifier,
    initialContent: BskyPost? = null,
    role: ComposerRole = ComposerRole.StandalonePost,
    draft: DraftPost = DraftPost(),
    onSend: (Post) -> Unit = {},
    onCancel: () -> Unit = {},
    onUpdate: (DraftPost) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var postText by rememberSaveable { mutableStateOf(draft.text) }
    val replyRef = remember { if(role == ComposerRole.Reply) {
        val root: StrongRef? = if(initialContent != null) {
            if (initialContent.reply?.root != null) {
                StrongRef(initialContent.reply.root.uri,initialContent.reply.root.cid)
            } else if (initialContent.reply?.parent != null) {
                StrongRef(initialContent.reply.parent.uri, initialContent.reply.parent.cid)
            } else {
                StrongRef(initialContent.uri,initialContent.cid)
            }
        } else {
            if (draft.reply?.reply?.root != null) {
                StrongRef(draft.reply.reply.root.uri,draft.reply.reply.root.cid)
            } else if (draft.reply?.reply?.parent != null) {
                StrongRef(draft.reply.reply.parent.uri, draft.reply.reply.parent.cid)
            } else null
        }
        val parent: StrongRef? = if(initialContent != null) {
            StrongRef(initialContent.uri,initialContent.cid)
        } else if (draft.reply?.reply?.root != null) {
            StrongRef(draft.reply.reply.root.uri,draft.reply.reply.root.cid)
        } else if (draft.reply?.reply?.parent != null){
            StrongRef(draft.reply.reply.parent.uri, draft.reply.reply.parent.cid)
        } else null
        if(root != null && parent != null ) PostReplyRef(root, parent) else null
    } else null }
    val quoteRef = remember { if(role == ComposerRole.QuotePost) {
        if(initialContent != null) {
            StrongRef(initialContent.uri,initialContent.cid)
        } else if(draft.quote != null){
            StrongRef(draft.quote.uri,draft.quote.cid)
        } else null
    } else null}
    val submitText = rememberSaveable {
        when(role) {
            ComposerRole.StandalonePost -> "Post"
            ComposerRole.Reply -> "Reply"
            ComposerRole.QuotePost -> "Quote Post"
            ComposerRole.ThreadBuilder -> "Post"
        }

    }
    val textLeft = 300 - postText.codePoints().count()
    val textFractionUsed: Float = postText.codePoints().count() / 300.0f
    LaunchedEffect(postText) {
        onUpdate(
            DraftPost(
            text = postText,
            reply = if (replyRef != null && role == ComposerRole.Reply) initialContent else null,
            quote = if (quoteRef != null && role == ComposerRole.QuotePost) initialContent else null,
        ))
    }
    Column(
        modifier = modifier
            .imePadding(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    onCancel()
                    onUpdate(DraftPost())
                },
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
                    onUpdate(DraftPost())
                },
                modifier = Modifier.padding(horizontal = 8.dp)
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
                    minLines = 5,
                    value = postText,
                    placeholder = { Text(text = "Write something...") },
                    onValueChange = {
                        postText = it
                                    },
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