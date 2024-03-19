package com.morpho.app.ui.common

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.bsky.feed.Post
import app.bsky.feed.PostEmbedUnion
import app.bsky.feed.PostReplyRef
import com.atproto.repo.StrongRef
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import com.morpho.butterfly.Language
import com.morpho.app.model.BskyPost
import com.morpho.app.model.DraftPost
import com.morpho.app.ui.post.ComposerPostFragment
import com.morpho.app.ui.post.testPost
import com.morpho.app.ui.theme.MorphoTheme


enum class ComposerRole {
    StandalonePost,
    Reply,
    QuotePost,
    ThreadBuilder,
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun BottomSheetPostComposer(
    modifier: Modifier = Modifier,
    crossinline onDismissRequest: ()-> Unit = {},
    initialContent: BskyPost? = null,
    role: ComposerRole = ComposerRole.StandalonePost,
    draft: DraftPost = DraftPost(),
    crossinline onSend: (Post) -> Unit = {},
    crossinline onCancel: () -> Unit = {},
    crossinline onUpdate: (DraftPost) -> Unit = {},
    sheetState:SheetState = rememberModalBottomSheetState(),
) {
    val scope = rememberCoroutineScope()
    BackHandler {
        scope.launch { sheetState.hide() }
            .invokeOnCompletion {
                if (!sheetState.isVisible) {onDismissRequest() } }
    }
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
            onSend = {
                onSend(it)
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {onDismissRequest() } } },
            onUpdate = { onUpdate(it) },
            onDismissRequest = {
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {onDismissRequest() } } },
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
    onDismissRequest: () -> Unit = {},
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
    val textLeft = 300 - postText.length
    val textFractionUsed: Float = postText.length.toFloat() / 300.0f
    LaunchedEffect(postText) {
        onUpdate(
            DraftPost(
            text = postText,
            reply = if (replyRef != null && role == ComposerRole.Reply) initialContent else null,
            quote = if (quoteRef != null && role == ComposerRole.QuotePost) initialContent else null,
        )
        )
    }
    Column(
        modifier = modifier
            .imePadding(),
        verticalArrangement = Arrangement.Top
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
                            text = postText.lines().reduce { acc, s ->  acc.trimEnd() + s.trimEnd()},
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
            Column(
                modifier = modifier
                    .imePadding(),
                verticalArrangement = Arrangement.Top
            ) {
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
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = true,
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                        ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSend(
                                Post(
                                    text = postText.lines().reduce { acc, s ->  acc.trimEnd() + s.trimEnd()},
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
                        onPrevious = {
                            focusManager.clearFocus()
                            onDismissRequest()
                        },
                        onSend = {
                            focusManager.clearFocus()
                            onSend(
                                Post(
                                    text = postText.lines().reduce { acc, s ->  acc.trimEnd() + s.trimEnd()},
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
                    ),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.imePadding()
        ) {
            IconButton(
                onClick = { },
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
    MorphoTheme(darkTheme = false) {
        Surface {
            PostComposer(initialContent = testPost, role = ComposerRole.QuotePost)
        }
    }
}

@Preview
@Composable
fun PreviewReply() {
    MorphoTheme(darkTheme = false) {
        Surface {
            PostComposer(initialContent = testPost, role = ComposerRole.Reply)
        }
    }
}