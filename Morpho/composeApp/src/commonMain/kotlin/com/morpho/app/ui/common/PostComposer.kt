package com.morpho.app.ui.common


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.bsky.feed.PostReplyRef
import com.atproto.repo.StrongRef
import com.morpho.app.data.toSharedImage
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DraftImage
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.bluesky.toProfileViewBasic
import com.morpho.app.model.uidata.getReplyRefs
import com.morpho.app.ui.post.ComposerPostFragment
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch


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
    crossinline onSend: (DraftPost) -> Unit = {},
    crossinline onCancel: () -> Unit = {},
    crossinline onUpdate: (DraftPost) -> Unit = {},
    sheetState:SheetState = rememberModalBottomSheetState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
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
            scope = scope
        )
    }
}


@Composable
fun PostComposer(
    modifier: Modifier = Modifier,
    initialContent: BskyPost? = null,
    role: ComposerRole = ComposerRole.StandalonePost,
    draft: DraftPost = DraftPost(),
    onSend: (DraftPost) -> Unit = {},
    onCancel: () -> Unit = {},
    onUpdate: (DraftPost) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val focusManager = LocalFocusManager.current
    var postText by rememberSaveable { mutableStateOf(draft.text) }
    val localReplyRef = remember {
        if(role == ComposerRole.Reply) {
            if (initialContent != null) {
                val root: StrongRef = if (initialContent.reply?.root != null) {
                    StrongRef(initialContent.reply.root.uri,initialContent.reply.root.cid)
                } else if (initialContent.reply?.parent != null) {
                    StrongRef(initialContent.reply.parent.uri, initialContent.reply.parent.cid)
                } else {
                    StrongRef(initialContent.uri,initialContent.cid)
                }
                val parent: StrongRef = StrongRef(initialContent.uri, initialContent.cid)
                val grandParentAuthor = (if (initialContent.reply?.parent != null) {
                    initialContent.reply.grandparentAuthor
                } else {
                    initialContent.author
                })?.toProfileViewBasic()
                PostReplyRef(root, parent, grandParentAuthor)
            } else if (draft.reply != null) {
                StrongRef(draft.reply.uri, draft.reply.cid)
            } else null
        } else null
    }
    var replyRef by remember { mutableStateOf(localReplyRef) }
    // TODO: Probably put this somewhere saner, but for now this works
    LaunchedEffect(localReplyRef) {
        val uri = initialContent?.uri ?: draft.reply?.uri
        if (role == ComposerRole.Reply && localReplyRef == null && uri != null) {
            getReplyRefs(uri).singleOrNull()?.getOrNull()?.let {
                replyRef = it
            }
        }
    }
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
    var postImages by rememberSaveable { mutableStateOf(draft.images) }
    val imagePicker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(),
        title = "Pick a media",
        initialDirectory = null
    ) { files ->
        scope.launch(Dispatchers.IO) {
            // Handle the picked files
            val images = files?.map {
                val image = it.toSharedImage()
                DraftImage(
                    image = image,
                    aspectRatio = image.getAspectRatio()
                )
            }
            if (images != null) {
                postImages = postImages.toMutableList().apply {
                    addAll(images)
                }
            }
        }
    }

    LaunchedEffect(postText, postImages) {
        onUpdate(
            DraftPost(
                text = postText,
                reply = if (replyRef != null && role == ComposerRole.Reply) initialContent else null,
                quote = if (role == ComposerRole.QuotePost) initialContent else null,
                images = postImages
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
                    onSend(draft)
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
                    supportingText = if (role == ComposerRole.QuotePost && initialContent != null) {
                        { ComposerPostFragment(post = initialContent) } } else null,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = true,
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                        ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSend(draft)
                        },
                        onPrevious = {
                            focusManager.clearFocus()
                            onDismissRequest()
                        },
                        onSend = {
                            focusManager.clearFocus()
                            onSend(draft)
                        },
                    ),
                )
                Row {
                    postImages.forEach { image ->
                        ComposerThumbnail(image, Modifier.padding(4.dp),
                        removeCallback = {
                            postImages = postImages.toMutableList().apply {
                                remove(image)
                            }
                        })
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.imePadding()
        ) {
            IconButton(
                onClick = {imagePicker.launch() },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = "Select Image")
            }
            Spacer(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(0.1f))

            Text(text = textLeft.toString(), style = MaterialTheme.typography.labelLarge)
            CircularProgressIndicator(
                progress = { textFractionUsed },
                modifier = Modifier
                    .size(45.dp)
                    .padding(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }


    }

}


