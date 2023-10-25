package radiant.nimbus.model

import androidx.compose.runtime.Immutable
import app.bsky.feed.ReplyRef
import app.bsky.feed.ReplyRefParentUnion
import app.bsky.feed.ReplyRefRootUnion
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class BskyPostReply(
    val root: BskyPost?,
    val parent: BskyPost?,
)

fun ReplyRef.toReply(): BskyPostReply {
    return BskyPostReply(
        root = when (val root = root) {
            is ReplyRefRootUnion.BlockedPost -> null
            is ReplyRefRootUnion.NotFoundPost -> null
            is ReplyRefRootUnion.PostView -> root.value.toPost()
        },
        parent = when (val parent = parent) {
            is ReplyRefParentUnion.BlockedPost -> null
            is ReplyRefParentUnion.NotFoundPost -> null
            is ReplyRefParentUnion.PostView -> parent.value.toPost()
        }
    )
}