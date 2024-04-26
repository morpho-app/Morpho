package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
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
import com.morpho.butterfly.model.RecordUnion
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: MainScreenModel, I: MorphoDataItem, S: ContentCardState<I>> TabbedSkylineFragment(
    sm: T,
    state: StateFlow<S>?,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    refresh: (AtCursor) -> Unit = {  },
) {
    val navigator = if (LocalNavigator.current?.instanceOf(TabNavigator::class) == true) {
        LocalNavigator.currentOrThrow.parent!!
    } else LocalNavigator.currentOrThrow
    var repostClicked by remember { mutableStateOf(false) }
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false) }
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
    if(content?.value != null) {
        SkylineFragment(
            content = state,
            isProfileFeed = true,
            onProfileClicked = { actor -> navigator.push(ProfileTab(actor)) },
            onItemClicked = { uri -> navigator.push(ThreadTab(uri)) },
            refresh = { cursor -> refresh(cursor)},
            onUnClicked = { type, rkey -> sm.deleteRecord(type, rkey) },
            onRepostClicked = { onRepostClicked(it) },
            onReplyClicked = { onReplyClicked(it) },
            onLikeClicked = { uri -> sm.createRecord(RecordUnion.Like(uri)) },
            onPostButtonClicked = { onPostButtonClicked() },
            contentPadding = paddingValues,
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
                onSend = {
                    sm.api.createRecord(RecordUnion.MakePost(it))
                    showComposer = false
                },
                onUpdate = { draft = it }
            )

        }
    } else {
        LoadingCircle()
    }
}

