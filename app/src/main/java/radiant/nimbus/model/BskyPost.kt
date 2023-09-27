package radiant.nimbus.model

import androidx.room.Entity
import androidx.room.Fts4
import app.bsky.feed.FeedViewPost
import app.bsky.feed.Post
import app.bsky.feed.PostView
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.util.deserialize
import radiant.nimbus.util.mapImmutable
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import javax.annotation.concurrent.Immutable

enum class PostType {
    BlockedThread,
    NotFoundThread,
    VisibleThread,
    BskyPost,
}

@Fts4
@Entity(tableName = "post_cache")
data class CachePost(
    val uri: String,
    val cid: String,
    val type: PostType,
    val authorDid: String,
    val timestamp: Long,
    val cacheEntry: String,
)


@Serializable
@Immutable
data class BskyPost (
    val uri: AtUri,
    val cid: Cid,
    val author: Profile,
    val text: String,
    val textLinks: ImmutableList<BskyPostLink>,
    val createdAt: Moment,
    val feature: BskyPostFeature?,
    val replyCount: Long,
    val repostCount: Long,
    val likeCount: Long,
    val indexedAt: Moment,
    val reposted: Boolean,
    val liked: Boolean,
    val labels: ImmutableList<BskyLabel>,
    val reply: BskyPostReply?,
    val reason: BskyPostReason?,
)


fun FeedViewPost.toPost(): BskyPost {
    return post.toPost(
        reply = reply?.toReply(),
        reason = reason?.toReason(),
    )
}

fun PostView.toPost(): BskyPost {
    return toPost(
        reply = null,
        reason = null
    )
}

fun PostView.toPost(
    reply: BskyPostReply?,
    reason: BskyPostReason?,
): BskyPost {
    // TODO verify via recordType before blindly deserialized.
    val postRecord = Post.serializer().deserialize(record)

    return BskyPost(
        uri = uri,
        cid = cid,
        author = author.toProfile(),
        text = postRecord.text,
        textLinks = postRecord.facets.mapImmutable { it.toLink() },
        createdAt = Moment(postRecord.createdAt),
        feature = embed?.toFeature(),
        replyCount = replyCount ?: 0,
        repostCount = repostCount ?: 0,
        likeCount = likeCount ?: 0,
        indexedAt = Moment(indexedAt),
        reposted = viewer?.repost != null,
        liked = viewer?.like != null,
        labels = labels.mapImmutable { it.toLabel() },
        reply = reply,
        reason = reason,
    )
}