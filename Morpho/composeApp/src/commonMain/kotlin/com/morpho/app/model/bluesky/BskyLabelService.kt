package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.ProfileAssociated
import app.bsky.labeler.LabelerView
import app.bsky.labeler.LabelerViewDetailed
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
@Immutable
open class BskyLabelService(
    val uri: AtUri,
    val cid: Cid,
    val creator: Profile?,
    val likeCount: Long?,
    val liked: Boolean,
    val likeUri: AtUri?,
    override val indexedAt: Moment,
    val policies: List<BskyLabelDefinition>,
    override val labels: List<BskyLabel>,
): Profile {
    override val did: Did
        get() = creator?.did ?: Did("did:blank:did")
    override val handle: Handle
        get() = creator?.handle ?: Handle("blank.handle")
    override val displayName: String?
        get() = creator?.displayName
    override val avatar: String?
        get() = creator?.avatar
    override val mutedByMe: Boolean
        get() = creator?.mutedByMe ?: false
    override val mutedByList: UserListBasic?
        get() = null
    override val block: BlockRecord?
        get() = null
    override val blockedBy: Boolean
        get() = false
    override val blockingByList: UserListBasic?
        get() = null
    override val following: FollowRecord?
        get() = null
    override val followedBy: FollowRecord?
        get() = null
    override val numKnownFollowers: Long
        get() = 0
    override val knownFollowers: List<Profile>
        get() = listOf()
    override val associated: ProfileAssociated?
        get() = null
    override val createdAt: Moment?
        get() = null
    override val followingMe: Boolean
        get() = false
    override val followedByMe: Boolean
        get() = false
}

public data object BlueskyHardcodedLabeler: BskyLabelService(
    uri = AtUri("at://morpho/builtin-labeler"),
    cid = Cid("builtin-labeler"),
    creator = null,
    likeCount = 0,
    liked = false,
    likeUri = null,
    indexedAt = Moment(Clock.System.now()),
    policies = persistentListOf(),
    labels = persistentListOf(),
)

fun LabelerViewDetailed.toLabelService(): BskyLabelService {
    return BskyLabelService(
        uri = this.uri,
        cid = this.cid,
        creator = this.creator.toProfile(),
        likeCount = this.likeCount,
        liked = (this.viewer?.like != null),
        likeUri = this.viewer?.like,
        indexedAt = Moment(this.indexedAt),
        policies = this.policies.labelValueDefinitions.mapImmutable { it.toModLabelDef() },
        labels = this.labels.mapImmutable { it.toLabel() },
    )
}

fun LabelerView.toLabelService(): BskyLabelService {
    return BskyLabelService(
        uri = this.uri,
        cid = this.cid,
        creator = this.creator.toProfile(),
        likeCount = this.likeCount,
        liked = (this.viewer?.like != null),
        likeUri = this.viewer?.like,
        indexedAt = Moment(this.indexedAt),
        policies = persistentListOf(),
        labels = this.labels.mapImmutable { it.toLabel() },
    )
}