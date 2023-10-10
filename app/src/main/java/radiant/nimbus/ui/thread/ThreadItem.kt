package radiant.nimbus.ui.thread

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atproto.repo.StrongRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.BskyPostReason
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.post.BlockedPostFragment
import radiant.nimbus.ui.post.FullPostFragment
import radiant.nimbus.ui.post.NotFoundPostFragment
import radiant.nimbus.ui.common.OnPostClicked
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.ui.post.PostFragmentRole
import radiant.nimbus.ui.elements.MenuOptions

@Composable
fun ThreadItem(
    item: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchStart,
    elevate: Boolean = false,
    reason: BskyPostReason? = null,
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (StrongRef) -> Unit = { },
    onRepostClicked: (StrongRef) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, rkey: String) -> Unit = { _, _ -> },
    lkeyFlow: Flow<String?> = flowOf(""),
    rpkeyFlow: Flow<String?> = flowOf("")
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            if (role == PostFragmentRole.PrimaryThreadRoot) {
                FullPostFragment(
                    post = item.post,
                    onItemClicked = onItemClicked,
                    onProfileClicked = onProfileClicked,
                    onUnClicked = onUnClicked,
                    onRepostClicked = onRepostClicked,
                    onReplyClicked = onReplyClicked,
                    onMenuClicked = onMenuClicked,
                    onLikeClicked = onLikeClicked,
                    lkeyFlow = lkeyFlow,
                    rpkeyFlow = rpkeyFlow,
                )
            } else {
                PostFragment(
                    post = item.post,
                    role = role,
                    indentLevel = indentLevel,
                    elevate = elevate,
                    onItemClicked = onItemClicked,
                    onProfileClicked = onProfileClicked,
                    onUnClicked = onUnClicked,
                    onRepostClicked = onRepostClicked,
                    onReplyClicked = onReplyClicked,
                    onMenuClicked = onMenuClicked,
                    onLikeClicked = onLikeClicked,
                    lkeyFlow = lkeyFlow,
                    rpkeyFlow = rpkeyFlow,
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