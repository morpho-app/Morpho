package com.morpho.app.ui.thread

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostReason
import com.morpho.app.model.bluesky.ThreadPost
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.*
import com.morpho.app.ui.utils.ItemClicked
import com.morpho.app.ui.utils.OnItemClicked
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.ContentHandling
import com.morpho.butterfly.model.RecordType

@Composable
inline fun ThreadItem(
    item: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchStart,
    elevate: Boolean = false,
    reason: BskyPostReason? = null,
    onItemClicked: OnItemClicked = ItemClicked(
        uriHandler = LocalUriHandler.current,
        navigator = LocalNavigator.currentOrThrow,
    ),
    crossinline onProfileClicked: (AtIdentifier) -> Unit = {},
    crossinline onReplyClicked: (BskyPost) -> Unit = { },
    crossinline onRepostClicked: (BskyPost) -> Unit = { },
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    noinline onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    crossinline getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() }
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            if (role == PostFragmentRole.PrimaryThreadRoot) {
                FullPostFragment(
                    post = item.post.copy(reason = reason, reply = item.post.reply?.copy(parentPost = null)),
                    modifier = modifier,
                    onItemClicked = onItemClicked,
                    onProfileClicked = { onProfileClicked(it) },
                    onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                    onRepostClicked = { onRepostClicked(it) },
                    onReplyClicked = { onReplyClicked(it) },
                    onMenuClicked = onMenuClicked,
                    onLikeClicked = { onLikeClicked(it) },
                    getContentHandling = { getContentHandling(it) }
                )
            } else {
                PostFragment(
                    post = item.post.copy(reason = reason, reply = item.post.reply?.copy(parentPost = null)),
                    role = role,
                    modifier = modifier,
                    indentLevel = indentLevel,
                    elevate = elevate,
                    onItemClicked = onItemClicked,
                    onProfileClicked = { onProfileClicked(it) },
                    onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                    onRepostClicked = { onRepostClicked(it) },
                    onReplyClicked = { onReplyClicked(it) },
                    onMenuClicked = onMenuClicked,
                    onLikeClicked = { onLikeClicked(it) },
                    getContentHandling = { getContentHandling(it) }
                )
            }
        }
        is ThreadPost.BlockedPost -> {
            BlockedPostFragment(
                modifier = modifier,
                post = item.uri,
                role = role,
                indentLevel = indentLevel,
            )
        }
        is ThreadPost.NotFoundPost -> {
            NotFoundPostFragment(
                modifier = modifier,
                post = item.uri,
                role = role,
                indentLevel = indentLevel,
            )
        }
    }
}