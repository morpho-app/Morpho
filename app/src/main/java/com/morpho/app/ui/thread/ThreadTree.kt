package com.morpho.app.ui.thread

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyScopeMarker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.atproto.repo.StrongRef
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import com.morpho.app.model.BskyPost
import com.morpho.app.model.ThreadPost
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.PostFragmentRole
import morpho.app.ui.utils.indentLevel

@LazyScopeMarker
@Composable
fun ThreadTree(
    reply: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 1,
    comparator: Comparator<ThreadPost> = compareBy {
        if (it is ThreadPost.ViewablePost) {
            it.post.indexedAt.instant.epochSeconds
        } else {
            it.hashCode().toLong()
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
                item = reply, role = PostFragmentRole.Solo, indentLevel = indentLevel,
                modifier = Modifier.padding(top = 2.dp),
                onItemClicked = onItemClicked,
                onProfileClicked = onProfileClicked,
                onUnClicked = onUnClicked,
                onRepostClicked = onRepostClicked,
                onReplyClicked = onReplyClicked,
                onMenuClicked = onMenuClicked,
                onLikeClicked = onLikeClicked,

            )
        } else {
            val replies = remember { reply.replies.sortedWith(comparator) }
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)

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
                            indentLevel = indentLevel,
                            modifier = Modifier.padding(top = 2.dp),
                            onItemClicked = {onItemClicked(it) },
                            onProfileClicked = { onProfileClicked(it) },
                            onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                            onRepostClicked = { onRepostClicked(it) },
                            onReplyClicked = { onReplyClicked(it) },
                            onMenuClicked = { onMenuClicked(it) },   
                            onLikeClicked = { onLikeClicked(it) },
                        )

                        replies.fastForEach { reply ->
                            ThreadTree(
                                reply = reply, modifier = modifier, indentLevel = indentLevel,
                                onItemClicked = { onItemClicked(it) },
                                onProfileClicked = { onProfileClicked(it) },
                                onUnClicked = { type, uri -> onUnClicked(type, uri) },
                                onRepostClicked = { onRepostClicked(it) },
                                onReplyClicked = { onReplyClicked(it) },
                                onMenuClicked = { onMenuClicked(it) },
                                onLikeClicked = { onLikeClicked(it) },
                            )
                        }
                    }
                }
            }

        }
    }
}