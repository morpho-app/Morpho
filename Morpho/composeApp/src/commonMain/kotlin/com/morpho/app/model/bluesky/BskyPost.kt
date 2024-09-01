package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.feed.*
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.deserialize
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Language
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
enum class PostType {
    BlockedThread,
    NotFoundThread,
    VisibleThread,
    BskyPost,
}

@Serializable
@Immutable
data class BskyPost (
    val uri: AtUri,
    val cid: Cid,
    val author: Profile,
    val text: String,
    @Serializable
    val facets: List<BskyFacet> = listOf(),
    @Serializable
    val tags: List<String> = listOf(),
    val createdAt: Moment,
    @Serializable
    val feature: BskyPostFeature? = null,
    val replyCount: Long,
    val repostCount: Long,
    val likeCount: Long,
    val indexedAt: Moment,
    val reposted: Boolean,
    val repostUri: AtUri? = null,
    val liked: Boolean,
    val likeUri: AtUri? = null,
    @Serializable
    val labels: List<BskyLabel>,
    val reply: BskyPostReply? = null,
    val reason: BskyPostReason? = null,
    @Serializable
    val langs: List<Language> = listOf(),
) {
    override operator fun equals(other: Any?) : Boolean {
        return when(other) {
            null -> false
            is Cid -> other == cid
            is AtUri -> other == uri
            // decide what exactly we care about here
            is BskyPost -> {
                other.uri == uri &&
                other.cid == cid &&
                other.feature == feature &&
                other.replyCount == replyCount &&
                other.repostCount == repostCount &&
                other.likeCount == likeCount &&
                other.indexedAt == indexedAt &&
                other.labels == labels &&
                other.reason == reason
                other.text == text &&
                other.author == author &&
                other.facets == facets &&
                other.tags == tags
            }
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

fun ThreadViewPost.toPost() : BskyPost {
    val replyRef = when (parent) {
        is ThreadViewPostParentUnion.BlockedPost -> null
        is ThreadViewPostParentUnion.NotFoundPost -> null
        is ThreadViewPostParentUnion.ThreadViewPost -> {
            val parentPost = (parent as ThreadViewPostParentUnion.ThreadViewPost).value.toPost()
            val rootPost = findRootPost()?.toPost() ?: parentPost
            BskyPostReply(root = rootPost, parent = parentPost, grandparentAuthor = parentPost.reply?.parent?.author)
        }
        null -> null
    }
    return post.toPost(reply = replyRef, reason = null)
}

fun ThreadViewPost.findRootPost(): ThreadViewPost? {
    return generateSequence(this) { currentPost ->
        when (val parentUnion = currentPost.parent) {
            is ThreadViewPostParentUnion.ThreadViewPost -> parentUnion.value
            else -> null
        }
    }.lastOrNull()
}

fun ThreadViewPost.findParentChain(): List<ThreadViewPost> {
    return generateSequence(this) { currentPost ->
        when (val parentUnion = currentPost.parent) {
            is ThreadViewPostParentUnion.ThreadViewPost -> parentUnion.value
            else -> null
        }
    }.toList()
}

fun PostView.toPost(
    reply: BskyPostReply?,
    reason: BskyPostReason?,
): BskyPost {
    // TODO verify via recordType before blindly deserialized.
    val postRecord = try {
        Post.serializer().deserialize(record)
    } catch (e: Exception) {
        Post(
            text = "Error deserializing post: $e\n" +
                    "Record: $record",
            facets = persistentListOf(),
            tags = persistentListOf(),
            createdAt = Clock.System.now(),
            langs = persistentListOf(),
        )
    }

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
