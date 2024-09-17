package com.morpho.app.screens.thread

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.atproto.repo.StrongRef
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostThread
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.uidata.ThreadUpdate
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.screens.base.tabbed.ThreadTab
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.app.ui.common.*
import com.morpho.app.ui.elements.doMenuOperation
import com.morpho.app.ui.thread.ThreadFragment
import com.morpho.app.util.ClipboardManager
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
       ExperimentalVoyagerApi::class
)
@Composable
fun TabScreen.ThreadViewContent(
    cardState: ContentCardState.PostThread,
    navigator:Navigator = LocalNavigator.currentOrThrow,

) {
    val sm = navigator.rememberNavigatorScreenModel { MainScreenModel() }
    val threadState by cardState.updates.filterIsInstance<ThreadUpdate>().collectAsState(ThreadUpdate.Empty)

    TabbedScreenScaffold(
        navBar = { navBar(navigator) },
        topContent = {
            ThreadTopBar(navigator = navigator)
        },
        modifier = Modifier,
        state = threadState,
        content = { insets, state ->
            when(state) {
                is ThreadUpdate.Empty -> {
                    LoadingCircle()
                }

                is ThreadUpdate.Error -> {
                    Text("Error: ${state.error}")
                }

                is ThreadUpdate.Thread -> {
                    ThreadView(
                        thread = state.results,
                        insets = insets,
                        navigator = navigator,
                        createRecord = { sm.screenModelScope.launch { sm.agent.createRecord(it) } },
                        deleteRecord = { type, uri -> sm.screenModelScope.launch {
                            sm.agent.deleteRecord(type, uri)
                        } },
                        resolveHandle = { handle -> sm.agent.resolveHandle(handle).getOrNull() }
                    )
                }
                else -> {
                    Text("Unknown state: $state")
                }
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
    resolveHandle: suspend (AtIdentifier) -> Did?,
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
    val uriHandler = LocalUriHandler.current
    ThreadFragment(thread = thread,
                   contentPadding = insets,
                   onItemClicked = { navigator.push(ThreadTab(it)) },
                   onProfileClicked = {
                       scope.launch {
                           val did = resolveHandle(it)
                           if(did != null) navigator.push(ProfileTab(did))
                       }
                   },
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
                   onMenuClicked = { option, post ->
                       doMenuOperation(option, post,
                                       clipboardManager = clipboard,
                                       uriHandler = uriHandler
                       ) },
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
                initialContent?.let { post ->
                    RecordUnion.Repost(
                        StrongRef(post.uri,post.cid)
                    )
                }?.let { createRecord(it) }
            },
            onQuotePost = {
                composerRole = ComposerRole.QuotePost
                showComposer = true
                repostClicked = false
            }
        )
    }
    if(showComposer) {
        val agent = getKoin().get<MorphoAgent>()
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
                    val post = finishedDraft.createPost(agent)
                    createRecord(RecordUnion.MakePost(post))
                }
                showComposer = false
            },
            onUpdate = { draft = it }
        )

    }
}