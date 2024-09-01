package com.morpho.app.ui.thread

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.ThreadPost
import com.morpho.app.model.uidata.ContentHandling
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.BlockedPostFragment
import com.morpho.app.ui.post.NotFoundPostFragment
import com.morpho.app.ui.post.PostFragment
import com.morpho.app.ui.post.PostFragmentRole
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType

@Composable
inline fun ThreadReply(
    item: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 1,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchMiddle,
    crossinline onItemClicked: OnPostClicked = {},
    crossinline onProfileClicked: (AtIdentifier) -> Unit = {},
    crossinline onReplyClicked: (BskyPost) -> Unit = { },
    crossinline onRepostClicked: (BskyPost) -> Unit = { },
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    crossinline onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    crossinline getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() }
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            val r = if (item.replies.isEmpty()) {
                PostFragmentRole.ThreadBranchEnd
            } else {
                PostFragmentRole.ThreadBranchMiddle
            }
            PostFragment(
                post = item.post,
                role = r,
                indentLevel = indentLevel,
                modifier = modifier,
                onItemClicked = {onItemClicked(it) },
                onProfileClicked = { onProfileClicked(it) },
                onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                onRepostClicked = { onRepostClicked(it) },
                onReplyClicked = { onReplyClicked(it) },
                onMenuClicked = { menu, post -> onMenuClicked(menu, post) },
                onLikeClicked = { onLikeClicked(it) },
                getContentHandling = { getContentHandling(it) }
            )
        }
        is ThreadPost.BlockedPost -> {
            BlockedPostFragment(
                post = item.uri,
                role = role,
                indentLevel = indentLevel,

                )
        }
        is ThreadPost.NotFoundPost -> {
            NotFoundPostFragment(
                post = item.uri,
                role = role,
                indentLevel = indentLevel,
            )
        }
    }
}