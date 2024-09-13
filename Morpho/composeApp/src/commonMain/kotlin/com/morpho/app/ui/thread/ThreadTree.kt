package com.morpho.app.ui.thread

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyScopeMarker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.ThreadPost
import com.morpho.app.model.uidata.ContentHandling
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.ui.post.PostFragmentRole
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import morpho.app.ui.utils.indentLevel

@LazyScopeMarker
@Composable
fun ThreadTree(
    reply: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 1,
    comparator: Comparator<ThreadPost> = compareBy {
        if (it is ThreadPost.ViewablePost) {
            it.post.createdAt.instant.epochSeconds
        } else {
            it.hashCode().toLong()
        }
    },
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() },
    end: Boolean = false,
) {
    val lineColour = MaterialTheme.colorScheme.onTertiaryContainer//.copy(alpha = 0.8f)
    val lineColour2 = MaterialTheme.colorScheme.outline//.copy(alpha = 0.8f)
    val bgColour = MaterialTheme.colorScheme.background

    if(reply is ThreadPost.ViewablePost) {
        if (reply.replies.isEmpty()) {
            ThreadReply(
                item = reply, role = PostFragmentRole.ThreadBranchEnd, indentLevel = indentLevel,
                modifier = if(indentLevel > 1) Modifier
                    .drawBehind {
                        drawLine(
                            color = lineColour,
                            cap = StrokeCap.Butt,
                            start = Offset(9.dp.toPx(), 22.dp.toPx()),
                            end = Offset(100.dp.toPx(), 22.dp.toPx()),
                            strokeWidth = Stroke.HairlineWidth
                        )
                        if(end) {
                            drawRect(
                                color = bgColour,
                                topLeft = Offset(4.dp.toPx(), 23.dp.toPx()),
                                size = Size(100.dp.toPx(), size.height - 23.dp.toPx()),
                            )
                        }
                    }.padding(top = 2.dp, start = 6.dp,)
                else Modifier.padding(top = 2.dp, start = 6.dp, bottom = 2.dp),
                onItemClicked = onItemClicked,
                onProfileClicked = onProfileClicked,
                onUnClicked = onUnClicked,
                onRepostClicked = onRepostClicked,
                onReplyClicked = onReplyClicked,
                onMenuClicked = onMenuClicked,
                onLikeClicked = onLikeClicked,
                getContentHandling = getContentHandling
            )
        } else {
            val replies = remember { reply.replies.sortedWith(comparator) }
            WrappedColumn(
                modifier = if(indentLevel == 1) modifier.fillMaxWidth().padding(start = 0.dp, end = 4.dp)
                    else modifier.fillMaxWidth().padding(start = 1.dp, bottom = 2.dp)
            ) {

                Surface(
                    //shadowElevation = if (indentLevel % 2 > 0) 2.dp else 0.dp,
                    tonalElevation = if(replies.size > 1) (indentLevel*2).dp else 0.dp,
                    //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
                    color = Color.Transparent,
                    //color = if(replies.size > 1) MaterialTheme.colorScheme.surfaceColorAtElevation((indentLevel*2).dp)
                    //else Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(indentLevel(indentLevel / 2.0f))
                        .align(Alignment.End)


                ) {
                    WrappedColumn(
                        horizontalAlignment = Alignment.End,
                        modifier = if(replies.size > 1) Modifier.fillMaxWidth()
                            .drawBehind {
                                if(!end)
                                    drawLine(
                                        color = lineColour,
                                        cap = StrokeCap.Butt,
                                        start = Offset(8.dp.toPx(), 10.dp.toPx()),
                                        end = Offset(8.dp.toPx(), size.height - 22.dp.toPx()),
                                        strokeWidth = Stroke.HairlineWidth
                                    )
                            } else Modifier.fillMaxWidth()
                                .drawBehind {
                                    if(replies.size == 1)
                                        drawLine(
                                            color = lineColour2,
                                            cap = StrokeCap.Butt,
                                            start = Offset(12.dp.toPx(), 6.dp.toPx()),
                                            end = Offset(12.dp.toPx(), size.height - 22.dp.toPx()),
                                            strokeWidth = 2.dp.toPx(),
                                        )
                                }


                    ) {
                        ThreadReply(
                            item = reply,
                            role = PostFragmentRole.ThreadBranchStart,
                            indentLevel = 1,
                            modifier = if(replies.size > 1) Modifier.padding(start = 2.dp, top = 2.dp)
                                else if(replies.size == 1) Modifier.padding(start = 1.dp, top = 1.dp)
                                else Modifier,
                            onItemClicked = {onItemClicked(it) },
                            onProfileClicked = { onProfileClicked(it) },
                            onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                            onRepostClicked = { onRepostClicked(it) },
                            onReplyClicked = { onReplyClicked(it) },
                            onMenuClicked = { menu, post -> onMenuClicked(menu, post) },
                            onLikeClicked = { onLikeClicked(it) },
                            getContentHandling = { getContentHandling(it) }
                        )
                        if(replies.size > 1) {
                            Surface(
                                color = Color.Transparent,
                                //shadowElevation = if (indentLevel > 0) 2.dp else 0.dp,
                                //tonalElevation = (indentLevel*2).dp,
                                //border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiaryContainer),
                                //color = MaterialTheme.colorScheme.surfaceColorAtElevation((indentLevel*2).dp),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.padding(top = 2.dp, start = 0.dp)
                                    .fillMaxWidth()
                            ) {
                                WrappedColumn(

                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    replies.fastForEachIndexed { index,reply ->
                                        ThreadTree(
                                            reply = reply,
                                            modifier = Modifier.drawBehind {
                                                drawLine(
                                                    color = lineColour,
                                                    cap = StrokeCap.Butt,
                                                    start = Offset(9.dp.toPx(), 20.dp.toPx()),
                                                    end = Offset(100.dp.toPx(), 20.dp.toPx()),
                                                    strokeWidth = Stroke.HairlineWidth
                                                )
                                                if(index == replies.lastIndex) {
                                                    drawRect(
                                                        color = bgColour,
                                                        topLeft = Offset(4.dp.toPx(), 21.dp.toPx()),
                                                        size = Size(100.dp.toPx(), size.height - 21.dp.toPx()),
                                                    )
                                                }
                                            }.padding(start = 3.dp),
                                            indentLevel = indentLevel + 1,
                                            onItemClicked = { onItemClicked(it) },
                                            onProfileClicked = { onProfileClicked(it) },
                                            onUnClicked = { type, uri -> onUnClicked(type, uri) },
                                            onRepostClicked = { onRepostClicked(it) },
                                            onReplyClicked = { onReplyClicked(it) },
                                            onMenuClicked = { option, p -> onMenuClicked(option, p) },
                                            onLikeClicked = { onLikeClicked(it) },
                                            getContentHandling = { getContentHandling(it) },
                                            end = index == replies.lastIndex
                                        )
                                    }
                                }
                            }
                        } else if(replies.size == 1) {
                            ThreadReply(
                                item = replies.first(),
                                role = PostFragmentRole.ThreadBranchEnd,
                                indentLevel = indentLevel,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                                onItemClicked = { onItemClicked(it) },
                                onProfileClicked = { onProfileClicked(it) },
                                onUnClicked = { type, uri -> onUnClicked(type, uri) },
                                onRepostClicked = { onRepostClicked(it) },
                                onReplyClicked = { onReplyClicked(it) },
                                onMenuClicked = { option, p -> onMenuClicked(option, p) },
                                onLikeClicked = { onLikeClicked(it) },
                            )

                        }
                    }
                }
            }

        }
    }
}