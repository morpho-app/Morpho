package com.morpho.app.ui.thread

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostReason
import com.morpho.app.model.bluesky.ThreadPost
import com.morpho.app.model.uidata.ContentHandling
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.*
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
inline fun ThreadItem(
    item: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchStart,
    elevate: Boolean = false,
    reason: BskyPostReason? = null,
    crossinline onItemClicked: OnPostClicked = {},
    crossinline onProfileClicked: (AtIdentifier) -> Unit = {},
    crossinline onReplyClicked: (BskyPost) -> Unit = { },
    crossinline onRepostClicked: (BskyPost) -> Unit = { },
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    noinline onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    crossinline getContentHandling: (BskyPost) -> ImmutableList<ContentHandling> = { persistentListOf() }
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            if (role == PostFragmentRole.PrimaryThreadRoot) {
                FullPostFragment(
                    post = item.post.copy(reason = reason),
                    onItemClicked = {onItemClicked(it) },
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
                    post = item.post.copy(reason = reason),
                    role = role,
                    indentLevel = indentLevel,
                    elevate = elevate,
                    onItemClicked = {onItemClicked(it) },
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