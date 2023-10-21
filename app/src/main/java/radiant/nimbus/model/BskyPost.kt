package radiant.nimbus.model

import androidx.room.Entity
import androidx.room.Fts4
import app.bsky.feed.FeedViewPost
import app.bsky.feed.Post
import app.bsky.feed.PostView
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Language
import radiant.nimbus.util.deserialize
import radiant.nimbus.util.mapImmutable
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
    val facets: ImmutableList<BskyFacet>,
    val tags: ImmutableList<String>,
    val createdAt: Moment,
    val feature: BskyPostFeature?,
    val replyCount: Long,
    val repostCount: Long,
    val likeCount: Long,
    val indexedAt: Moment,
    val reposted: Boolean,
    val repostUri: AtUri? = null,
    val liked: Boolean,
    val likeUri: AtUri? = null,
    val labels: ImmutableList<BskyLabel>,
    val reply: BskyPostReply?,
    val reason: BskyPostReason?,
    val langs: ImmutableList<Language> = persistentListOf(),
) {
    override operator fun equals(other: Any?) : Boolean {
        return when(other) {
            null -> false
            is Cid -> other == cid
            is AtUri -> other == uri
            else -> other.hashCode() == this.hashCode()
        }
    }

    operator fun contains(other: Any?) : Boolean {
        return when(other) {
            null -> false
            is Cid -> other == cid
            is AtUri -> other == uri
            is BskyPost -> other.cid == cid
            else -> reply?.parent?.contains(other) == true
        }
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + cid.hashCode()
        result = 31 * result + (feature?.hashCode() ?: 0)
        result = 31 * result + replyCount.hashCode()
        result = 31 * result + repostCount.hashCode()
        result = 31 * result + likeCount.hashCode()
        result = 31 * result + indexedAt.hashCode()
        result = 31 * result + repostUri.hashCode()
        result = 31 * result + likeUri.hashCode()
        result = 31 * result + labels.hashCode()
        result = 31 * result + (reason?.hashCode() ?: 0)
        return result
    }
}


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
    //Log.v("PostRecord", record.toString())
    val postRecord = Post.serializer().deserialize(record)

    return BskyPost(
        uri = uri,
        cid = cid,
        author = author.toProfile(),
        text = postRecord.text,
        facets = postRecord.facets.mapImmutable { it.toBskyFacet() },
        tags = postRecord.tags.mapImmutable{it},
        createdAt = Moment(postRecord.createdAt),
        feature = embed?.toFeature(),
        replyCount = replyCount ?: 0,
        repostCount = repostCount ?: 0,
        likeCount = likeCount ?: 0,
        indexedAt = Moment(indexedAt),
        reposted = viewer?.repost != null,
        repostUri = viewer?.repost,
        liked = viewer?.like != null,
        likeUri = viewer?.like,
        labels = labels.mapImmutable { it.toLabel() },
        langs = postRecord.langs.mapImmutable { it },
        reply = reply,
        reason = reason,
    )
}