package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.screenModelScope
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.screens.profile.TabbedProfileViewModel
import com.morpho.butterfly.model.RecordUnion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: MainScreenModel> TabbedSkylineFragment(
    screenModel: T,
    state: ContentCardState.ProfileTimeline<MorphoDataItem>?,
    paddingValues: PaddingValues = PaddingValues(0.dp),
) {
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
    if(state != null) {
        var timeline by remember { mutableStateOf(state) }
        SkylineFragment(
            content = timeline,
            isProfileFeed = true,
            onProfileClicked = { actor -> screenModel.onProfileClicked(actor, true) },
            onItemClicked = { uri -> screenModel.onItemClicked(uri, true) },
            refresh = { _ ->
                // Also want to test without explicitly remembering and setting timeline locally
                screenModel.screenModelScope.launch {
                    when (screenModel) {
                        is TabbedProfileViewModel -> {
                            screenModel.loadContent(state).await().map {
                                timeline = it as ContentCardState.ProfileTimeline<MorphoDataItem>
                            }
                        }

                        is TabbedMainScreenModel -> {
                            screenModel.loadContent(state).await().map {
                                timeline = it as ContentCardState.ProfileTimeline<MorphoDataItem>
                            }
                        }

                        else -> { /* Shouldn't happen */
                        }
                    }
                }
            },
            onUnClicked = { type, rkey -> screenModel.deleteRecord(type, rkey) },
            onRepostClicked = { onRepostClicked(it) },
            onReplyClicked = { onReplyClicked(it) },
            onLikeClicked = { uri -> screenModel.createRecord(RecordUnion.Like(uri)) },
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
                    }?.let { screenModel.api.createRecord(it) }
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
                    screenModel.api.createRecord(RecordUnion.MakePost(it))
                    showComposer = false
                },
                onUpdate = { draft = it }
            )

        }
    }
}