package radiant.nimbus.model

import androidx.compose.runtime.Immutable

import app.bsky.feed.FeedViewPost
import app.bsky.feed.Post
import app.bsky.feed.PostView

import kotlinx.collections.immutable.ImmutableList
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import radiant.nimbus.util.deserialize
import radiant.nimbus.util.mapImmutable

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