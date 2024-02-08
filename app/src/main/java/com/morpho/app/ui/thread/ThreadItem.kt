package morpho.app.ui.thread

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atproto.repo.StrongRef
import morpho.app.api.AtIdentifier
import morpho.app.api.AtUri
import morpho.app.api.model.RecordType
import morpho.app.model.BskyPost
import morpho.app.model.BskyPostReason
import morpho.app.model.ThreadPost
import morpho.app.ui.common.OnPostClicked
import morpho.app.ui.elements.MenuOptions
import morpho.app.ui.post.BlockedPostFragment
import morpho.app.ui.post.FullPostFragment
import morpho.app.ui.post.NotFoundPostFragment
import morpho.app.ui.post.PostFragment
import morpho.app.ui.post.PostFragmentRole

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