package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.feed.Like
import app.bsky.feed.Post
import app.bsky.feed.PostReplyRef
import app.bsky.feed.Repost
import app.bsky.graph.Follow
import app.bsky.notification.ListNotificationsNotification
import app.bsky.notification.ListNotificationsReason
import com.atproto.repo.StrongRef
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.deserialize
import com.morpho.app.util.mapImmutable
import com.morpho.app.util.recordType
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Immutable
@Serializable
sealed interface BskyNotification {
    val uri: AtUri
    val cid: Cid
    val author: Profile
    /**
     * Expected values are 'like', 'repost', 'follow', 'mention', 'reply', and 'quote'.
     */
    val reason: ListNotificationsReason
    val reasonSubject: AtUri?
    val isRead: Boolean
    val indexedAt: Moment
    val labels: List<BskyLabel>

    @Immutable
    @Serializable
    data class Like(
        override val uri: AtUri,
        override val cid: Cid,
        override val author: Profile,
        override val reason: ListNotificationsReason,
        override val reasonSubject: AtUri? = null,
        override val isRead: Boolean,
        override val indexedAt: Moment,
        override val labels: List<BskyLabel> = listOf(),
        val subject: StrongRef,
        val createdAt: Moment,
    ): BskyNotification

    @Immutable
    @Serializable
    data class Repost(
        override val uri: AtUri,
        override val cid: Cid,
        override val author: Profile,
        override val reason: ListNotificationsReason,
        override val reasonSubject: AtUri? = null,
        override val isRead: Boolean,
        override val indexedAt: Moment,
        override val labels: List<BskyLabel> = listOf(),
        val subject: StrongRef,
        val createdAt: Moment,
    ): BskyNotification

    @Immutable
    @Serializable
    data class Follow(
        override val uri: AtUri,
        override val cid: Cid,
        override val author: Profile,
        override val reason: ListNotificationsReason,
        override val reasonSubject: AtUri? = null,
        override val isRead: Boolean,
        override val indexedAt: Moment,
        override val labels: List<BskyLabel> = listOf(),
        val subject: Did,
        val createdAt: Moment,
    ): BskyNotification

    @Immutable
    @Serializable
    data class Post(
        override val uri: AtUri,
        override val cid: Cid,
        override val author: Profile,
        override val reason: ListNotificationsReason,
        override val reasonSubject: AtUri? = null,
        override val isRead: Boolean,
        override val indexedAt: Moment,
        override val labels: List<BskyLabel> = listOf(),
        val post: BskyPost,
    ): BskyNotification

    @Immutable
    @Serializable
    data class Unknown(
        override val uri: AtUri,
        override val cid: Cid,
        override val author: Profile,
        override val reason: ListNotificationsReason,
        override val reasonSubject: AtUri? = null,
        override val isRead: Boolean,
        override val indexedAt: Moment,
        override val labels: List<BskyLabel> = listOf(),
        val record: JsonElement,
    ): BskyNotification
}

fun ListNotificationsNotification.toBskyNotification() : BskyNotification {
    when(record.recordType) {
        "app.bsky.feed.like" -> {
            val like = Like.serializer().deserialize(this.record)
            return BskyNotification.Like(
                uri = this.uri,
                cid = this.cid,
                author = this.author.toProfile(),
                reason = this.reason,
                reasonSubject = this.reasonSubject,
                isRead = this.isRead,
                indexedAt = Moment(this.indexedAt),
                labels = this.labels.mapImmutable { it.toLabel() },
                subject = like.subject,
                createdAt = Moment(like.createdAt)
            )
        }

        "app.bsky.feed.post" -> {
            val postRecord = Post.serializer().deserialize(this.record)
            return BskyNotification.Post(
                uri = this.uri,
                cid = this.cid,
                author = this.author.toProfile(),
                reason = this.reason,
                reasonSubject = this.reasonSubject,
                isRead = this.isRead,
                indexedAt = Moment(this.indexedAt),
                labels = this.labels.mapImmutable { it.toLabel() },
                post = BskyPost(
                    uri = uri,
                    cid = cid,
                    author = author.toProfile(),
                    text = postRecord.text,
                    facets = postRecord.facets.mapImmutable { it.toBskyFacet() },
                    tags = postRecord.tags.mapImmutable { it },
                    createdAt = Moment(postRecord.createdAt),
                    feature = postRecord.embed?.toFeature(),
                    replyCount = 0,
                    repostCount = 0,
                    likeCount = 0,
                    indexedAt = Moment(indexedAt),
                    reposted = this.reason == ListNotificationsReason.QUOTE,
                    repostUri = null,
                    liked = false,
                    likeUri = null,
                    labels = labels.mapImmutable { it.toLabel() },
                    langs = postRecord.langs.mapImmutable { it },
                    reply = null,
                    reason = null,
                )
            )
        }
        "app.bsky.feed.repost" -> {
            val repost = Repost.serializer().deserialize(this.record)
            return BskyNotification.Repost(
                uri = this.uri,
                cid = this.cid,
                author = this.author.toProfile(),
                reason = this.reason,
                reasonSubject = this.reasonSubject,
                isRead = this.isRead,
                indexedAt = Moment(this.indexedAt),
                labels = this.labels.mapImmutable { it.toLabel() },
                subject = repost.subject,
                createdAt = Moment(repost.createdAt)
            )
        }
        "app.bsky.graph.follow" -> {
            val follow = Follow.serializer().deserialize(this.record)
            return BskyNotification.Follow(
                uri = this.uri,
                cid = this.cid,
                author = this.author.toProfile(),
                reason = this.reason,
                reasonSubject = this.reasonSubject,
                isRead = this.isRead,
                indexedAt = Moment(this.indexedAt),
                labels = this.labels.mapImmutable { it.toLabel() },
                subject = follow.subject,
                createdAt = Moment(follow.createdAt)
            )
        }
        else -> {
            return BskyNotification.Unknown(
                uri = this.uri,
                cid = this.cid,
                author = this.author.toProfile(),
                reason = this.reason,
                reasonSubject = this.reasonSubject,
                isRead = this.isRead,
                indexedAt = Moment(this.indexedAt),
                labels = this.labels.mapImmutable { it.toLabel() },
                record = this.record
            )
        }
    }
}

fun PostReplyRef.toReply() {

}