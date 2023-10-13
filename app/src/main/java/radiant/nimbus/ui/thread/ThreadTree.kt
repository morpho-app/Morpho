package radiant.nimbus.ui.thread

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyScopeMarker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.atproto.repo.StrongRef
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.BskyPost
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
        }
    },
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
) {
    if(reply is ThreadPost.ViewablePost) {

        if (reply.replies.isEmpty()) {
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
                        )

                        val nextIndent = indentLevel + 1
                        reply.replies.sortedWith(comparator).forEach {
                            ThreadTree(
                                reply = it, modifier = modifier, indentLevel = nextIndent,
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onReplyClicked = onReplyClicked,
                                onRepostClicked = onRepostClicked,
                                onLikeClicked = onLikeClicked,
                                onMenuClicked = onMenuClicked,
                                onUnClicked = onUnClicked,
                            )
                        }
                    }
                }
            }

        }
    }
}