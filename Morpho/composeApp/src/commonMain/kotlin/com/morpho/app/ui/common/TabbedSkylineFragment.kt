package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uidata.UIUpdate
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.ThreadTab
import com.morpho.app.ui.elements.doMenuOperation
import com.morpho.app.util.ClipboardManager
import com.morpho.butterfly.ButterflyAgent
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedSkylineFragment(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    isProfileFeed: Boolean = false,
    uiUpdate: StateFlow<UIUpdate>,
    eventCallback: (Event) -> Unit = {},
) {
    val agent = getKoin().get<ButterflyAgent>()
    val uiState = uiUpdate.collectAsState(initial = UIUpdate.Empty)
    val navigator = if (LocalNavigator.current?.parent?.instanceOf(TabNavigator::class) == true) {
        LocalNavigator.currentOrThrow
    } else LocalNavigator.currentOrThrow.parent!!
    val scope = rememberCoroutineScope()
    var repostClicked by remember { mutableStateOf(false) }
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false) }
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current
    // Probably pull this farther up,
    //      but this means if you don't explicitly cancel you don't lose the post
    var draft by remember { mutableStateOf(DraftPost()) }
    val onReplyClicked:(post: BskyPost) -> Unit = remember {
        return@remember {
            initialContent = it
            composerRole = ComposerRole.Reply
            showComposer = true
        }
    }

    val onRepostClicked:(post: BskyPost) -> Unit = remember {
        return@remember {
            initialContent = it
            repostClicked = true
        }
    }

    val onPostButtonClicked:() -> Unit = remember {
        return@remember {
            composerRole = ComposerRole.StandalonePost
            showComposer = true
        }
    }
    val clipboard = getKoin().get<ClipboardManager>()
    if(uiState.value !is UIUpdate.Empty) {
        SkylineFragment(
            onProfileClicked = { actor -> navigator.push(ProfileTab(actor)) },
            onItemClicked = { uri -> navigator.push(ThreadTab(uri)) },
            onUnClicked = { type, rkey -> agent.deleteRecord(type, rkey) },
            onRepostClicked = { onRepostClicked(it) },
            onMenuClicked = { option, post ->
                doMenuOperation(option, post,
                                clipboardManager = clipboard,
                                uriHandler = uriHandler
                ) },
            onReplyClicked = { onReplyClicked(it) },
            onLikeClicked = { ref -> agent.like(ref) },
            onPostButtonClicked = { onPostButtonClicked() },
            getContentHandling = { post -> listOf() },
            contentPadding = paddingValues,
            isProfileFeed = isProfileFeed,
            feedUpdate = uiUpdate.filterIsInstance(),
        )
        if(repostClicked) {
            RepostQueryDialog(
                onDismissRequest = {
                    showComposer = false
                    repostClicked = false
                },
                onRepost = {
                    repostClicked = false
                    initialContent?.let { agent.repost(StrongRef(it.uri, it.cid)) }
                },
                onQuotePost = {
                    composerRole = ComposerRole.QuotePost
                    showComposer = true
                    repostClicked = false
                }
            )
        }
        if(showComposer) {
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
                    scope.launch(Dispatchers.IO) { agent.post(finishedDraft.createPost(agent)) }
                    showComposer = false
                },
                onUpdate = { draft = it }
            )

        }
    } else LoadingCircle()
}

