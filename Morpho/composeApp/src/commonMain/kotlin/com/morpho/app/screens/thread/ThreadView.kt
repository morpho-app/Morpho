package com.morpho.app.screens.thread

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostThread
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.screens.base.tabbed.ThreadTab
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.app.ui.common.*
import com.morpho.app.ui.elements.doMenuOperation
import com.morpho.app.ui.thread.ThreadFragment
import com.morpho.app.util.ClipboardManager
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabScreen.ThreadViewContent(
    threadState: StateFlow<ContentCardState.PostThread>,
    navigator:Navigator = LocalNavigator.currentOrThrow,
    sm:MainScreenModel = navigator.getNavigatorScreenModel<MainScreenModel>()
) {
    val thread by threadState.value.thread.collectAsState()
    TabbedScreenScaffold(
        navBar = { navBar(navigator) },
        topContent = {
            ThreadTopBar(navigator = navigator)
        },
        content = { insets ->
            if(thread != null) {
                ThreadView(
                    thread!!,
                    insets = insets,
                    navigator = navigator,
                    createRecord = { sm.createRecord(it) },
                    deleteRecord = { type, uri -> sm.deleteRecord(type, uri) }
                )
            } else {
                LoadingCircle()
            }

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadTopBar(navigator: Navigator = LocalNavigator.currentOrThrow) {
    CenterAlignedTopAppBar(
        title = { Text("Post") },
        navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        }
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThreadView(
    thread: BskyPostThread,
    insets: PaddingValues = PaddingValues(0.dp),
    navigator: Navigator = LocalNavigator.currentOrThrow,
    createRecord: (RecordUnion) -> Unit = {  },
    deleteRecord: (RecordType, AtUri) -> Unit = { _, _ ->  },
) {
    var repostClicked by remember { mutableStateOf(false)}
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false)}
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Probably pull this farther up,
    //      but this means if you don't explicitly cancel you don't lose the post
    var draft by remember{ mutableStateOf(DraftPost()) }
    val clipboard = getKoin().get<ClipboardManager>()
    val scope = rememberCoroutineScope()
    ThreadFragment(thread = thread,
                   contentPadding = insets,
                   onItemClicked = { navigator.push(ThreadTab(it)) },
                   onProfileClicked = { navigator.push(ProfileTab(it)) },
                   onUnClicked = {type, uri ->  deleteRecord(type, uri)},
                   onRepostClicked = {
                       initialContent = it
                       repostClicked = true
                   },
                   onReplyClicked = {
                       initialContent = it
                       composerRole = ComposerRole.Reply
                       showComposer = true
                   },
                   onMenuClicked = { option, post -> doMenuOperation(option, post, clipboardManager = clipboard) },
                   onLikeClicked = { createRecord(RecordUnion.Like(it)) },
    )
    if(repostClicked) {
        RepostQueryDialog(
            onDismissRequest = {
                showComposer = false
                repostClicked = false
            },
            onRepost = {
                repostClicked = false
                composerRole = ComposerRole.QuotePost
                initialContent?.let { post ->
                    RecordUnion.Repost(
                        StrongRef(post.uri,post.cid)
                    )
                }?.let { createRecord(it) }
            },
            onQuotePost = {
                showComposer = true
                repostClicked = false
            }
        )
    }
    if(showComposer) {
        val api = getKoin().get<Butterfly>()
        BottomSheetPostComposer(
            onDismissRequest = { showComposer = false },
            sheetState = sheetState,
            role = composerRole,
            //modifier = Modifier.padding(insets),
            initialContent = initialContent,
            draft = draft,
            onCancel = {
                showComposer = false
                draft = DraftPost()
            },
            onSend = { finishedDraft ->
                scope.launch(Dispatchers.IO) {
                    val post = finishedDraft.createPost(api)
                    createRecord(RecordUnion.MakePost(post))
                }
                showComposer = false
            },
            onUpdate = { draft = it }
        )

    }
}