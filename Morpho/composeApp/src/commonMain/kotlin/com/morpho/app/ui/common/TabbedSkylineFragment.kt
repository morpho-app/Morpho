package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.ThreadTab
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.app.ui.elements.doMenuOperation
import com.morpho.app.util.ClipboardManager
import com.morpho.butterfly.model.RecordUnion
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: MainScreenModel, I: MorphoDataItem, S: ContentCardState<I>> TabbedSkylineFragment(
    sm: T,
    state: StateFlow<S>?,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    refresh: (AtCursor) -> Unit = {},
    isProfileFeed: Boolean = false,
    listState: LazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = state?.value?.feed?.cursor?.scroll ?: 0
    ),
) {
    val navigator = if (LocalNavigator.current?.parent?.instanceOf(TabNavigator::class) == true) {
        LocalNavigator.currentOrThrow
    } else LocalNavigator.currentOrThrow.parent!!
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
    val content = state?.collectAsState()
    val clipboard = getKoin().get<ClipboardManager>()
    if(content?.value != null) {

        SkylineFragment(
            content = state,
            onProfileClicked = { actor -> navigator.push(ProfileTab(actor)) },
            onItemClicked = { uri -> navigator.push(ThreadTab(uri)) },
            refresh = { cursor -> refresh(cursor)},
            onUnClicked = { type, rkey -> sm.deleteRecord(type, rkey) },
            onRepostClicked = { onRepostClicked(it) },
            onMenuClicked = { option, post ->
                doMenuOperation(option, post,
                                clipboardManager = clipboard,
                                uriHandler = uriHandler
                ) },
            onReplyClicked = { onReplyClicked(it) },
            onLikeClicked = { uri -> sm.createRecord(RecordUnion.Like(uri)) },
            onPostButtonClicked = { onPostButtonClicked() },
            getContentHandling = { post -> sm.labelService.getContentHandlingForPost(post)},
            contentPadding = paddingValues,
            isProfileFeed = isProfileFeed,
            listState = listState,
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
                            StrongRef(post.uri, post.cid)
                        )
                    }?.let { sm.api.createRecord(it) }
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
                    sm.screenModelScope.launch(Dispatchers.IO) {
                        val post = finishedDraft.createPost(sm.api)
                        sm.api.createRecord(RecordUnion.MakePost(post))
                    }
                    showComposer = false
                },
                onUpdate = { draft = it }
            )

        }
    } else {
        LoadingCircle()
    }
}

