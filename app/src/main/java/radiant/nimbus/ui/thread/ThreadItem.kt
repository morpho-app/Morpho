package radiant.nimbus.ui.thread

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atproto.repo.StrongRef
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostReason
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.common.OnPostClicked
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.post.BlockedPostFragment
import radiant.nimbus.ui.post.FullPostFragment
import radiant.nimbus.ui.post.NotFoundPostFragment
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.ui.post.PostFragmentRole

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
    crossinline onMenuClicked: (MenuOptions) -> Unit = { },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            if (role == PostFragmentRole.PrimaryThreadRoot) {
                FullPostFragment(
                    post = item.post,
                    onItemClicked = {onItemClicked(it) },
                    onProfileClicked = { onProfileClicked(it) },
                    onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                    onRepostClicked = { onRepostClicked(it) },
                    onReplyClicked = { onReplyClicked(it) },
                    onMenuClicked = { onMenuClicked(it) },
                    onLikeClicked = { onLikeClicked(it) },
                )
            } else {
                PostFragment(
                    post = item.post,
                    role = role,
                    indentLevel = indentLevel,
                    elevate = elevate,
                    onItemClicked = {onItemClicked(it) },
                    onProfileClicked = { onProfileClicked(it) },
                    onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                    onRepostClicked = { onRepostClicked(it) },
                    onReplyClicked = { onReplyClicked(it) },
                    onMenuClicked = { onMenuClicked(it) },
                    onLikeClicked = { onLikeClicked(it) },
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