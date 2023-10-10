package radiant.nimbus.ui.thread

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyScopeMarker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atproto.repo.StrongRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.common.OnPostClicked
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.post.PostFragmentRole
import radiant.nimbus.ui.utils.indentLevel

@LazyScopeMarker
@Composable
fun ThreadTree(
    reply: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 1,
    comparator: Comparator<ThreadPost> = compareBy {
        if (it is ThreadPost.ViewablePost) {
            it.post.indexedAt
        } else {
            it.hashCode()
        }},
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (StrongRef) -> Unit = { },
    onRepostClicked: (StrongRef) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, rkey: String) -> Unit = { _, _ -> },
    lKeys: Flow<MutableMap<AtUri, String?>>,
    rpKeys: Flow<MutableMap<AtUri, String?>>,
) {
    if(reply is ThreadPost.ViewablePost) {
        val likeKeys by lKeys.collectAsStateWithLifecycle(initialValue = mutableMapOf())
        val repostKeys by lKeys.collectAsStateWithLifecycle(initialValue = mutableMapOf())

        if (reply.replies.isEmpty()) {
            var lkeyFlow: Flow<String?> = flowOf(null)
            var rpkeyFlow: Flow<String?> = flowOf(null)
            LaunchedEffect(likeKeys) {
                lkeyFlow = snapshotFlow { likeKeys[reply.post.uri] }
                    .distinctUntilChanged()
            }
            LaunchedEffect(repostKeys) {
                rpkeyFlow = snapshotFlow { likeKeys[reply.post.uri] }
                    .distinctUntilChanged()
            }
            ThreadReply(
                item = reply, role = PostFragmentRole.ThreadBranchEnd, indentLevel = indentLevel,
                modifier = Modifier.padding(vertical = 2.dp),
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
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)

            ) {
                val lineColour = if (indentLevel % 4 == 0) {
                    MaterialTheme.colorScheme.tertiary.copy(0.7f)
                } else if (indentLevel % 2 == 0) {
                    MaterialTheme.colorScheme.secondary.copy(0.7f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(0.7f)
                }
                Surface(
                    //shadowElevation = if (indentLevel > 0) 1.dp else 0.dp,
                    border = BorderStroke(
                        1.dp, Brush.sweepGradient(
                            0.0f to Color.Transparent, 0.2f to Color.Transparent,
                            0.4f to lineColour, 0.7f to lineColour,
                            0.9f to Color.Transparent,
                            center = Offset(100f, 500f)
                        )
                    ),
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth(indentLevel(indentLevel / 2.0f))
                        .align(Alignment.End)


                ) {
                    Column(
                    ) {
                        var lkeyFlow: Flow<String?> = flowOf(null)
                        var rpkeyFlow: Flow<String?> = flowOf(null)
                        LaunchedEffect(likeKeys) {
                            lkeyFlow = snapshotFlow { likeKeys[reply.post.uri] }
                                .distinctUntilChanged()
                        }
                        LaunchedEffect(repostKeys) {
                            rpkeyFlow = snapshotFlow { likeKeys[reply.post.uri] }
                                .distinctUntilChanged()
                        }
                        ThreadReply(
                            item = reply,
                            role = PostFragmentRole.ThreadBranchStart,
                            indentLevel = indentLevel + 1,
                            modifier = Modifier.padding(vertical = 2.dp),
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

                        val nextIndent = indentLevel + 1
                        reply.replies.sortedWith(comparator).forEach {
                            ThreadTree(
                                reply = it, indentLevel = nextIndent, modifier = modifier,
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onUnClicked = onUnClicked,
                                onRepostClicked = onRepostClicked,
                                onReplyClicked = onReplyClicked,
                                onMenuClicked = onMenuClicked,
                                onLikeClicked = onLikeClicked,
                                lKeys = lKeys,
                                rpKeys = rpKeys,
                            )
                        }
                    }
                }
            }

        }
    }
}