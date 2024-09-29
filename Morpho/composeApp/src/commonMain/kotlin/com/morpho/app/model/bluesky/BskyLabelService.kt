package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.labeler.LabelerView
import app.bsky.labeler.LabelerViewDetailed
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.uidata.Moment
import com.morpho.app.model.uidata.MomentParceler
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Immutable
open class BskyLabelService(
    val uri: AtUri,
    val cid: Cid,
    val creator: DetailedProfile?,
    val likeCount: Long?,
    val liked: Boolean,
    val likeUri: AtUri?,
    @TypeParceler<Moment, MomentParceler>()
    val indexedAt: Moment,
    val policies: List<BskyLabelDefinition>,
    val labels: List<BskyLabel>,
): Parcelable {
    val did: Did
        get() = creator?.did ?: Did("did:blank:did")
    val handle: Handle
        get() = creator?.handle ?: Handle("blank.handle")
    val displayName: String?
        get() = creator?.displayName
    val avatar: String?
        get() = creator?.avatar
}

suspend fun LabelerViewDetailed.toLabelService(
    agent: MorphoAgent,
): BskyLabelService {
    val fullProfile = agent.getProfile(this.creator.did).getOrNull()?.toProfile() ?: DetailedProfile(
        did = this.creator.did,
        handle = this.creator.handle,
        displayName = this.creator.displayName,
        description = this.creator.description,
        avatar = this.creator.avatar,
        banner = null,
        followersCount = 0,
        followsCount = 0,
        postsCount = 0,
        labels = this.creator.labels.map { it.toLabel() },
        indexedAt = this.creator.indexedAt?.let { Moment(it) },
        mutedByMe = false,
        following = this.creator.viewer?.following?.let { FollowRecord(it) },
        followedBy = this.creator.viewer?.followedBy?.let { FollowRecord(it) },
        numKnownFollowers = 0,
        knownFollowers = persistentListOf(),
        associated = this.creator.associated?.toBskyProfileAssociated(),
        createdAt = this.creator.createdAt?.let { Moment(it) },
        mutedByList = this.creator.viewer?.mutedByList?.toList(),
        block = this.creator.viewer?.blocking?.let { BlockRecord(it) },
        blockedBy = this.creator.viewer?.blockedBy == true,
        blockingByList = this.creator.viewer?.blockingByList?.toList(),
    )
    return BskyLabelService(
        uri = this.uri,
        cid = this.cid,
        creator = fullProfile,
        likeCount = this.likeCount,
        liked = (this.viewer?.like != null),
        likeUri = this.viewer?.like,
        indexedAt = Moment(this.indexedAt),
        policies = this.policies.labelValueDefinitions.mapImmutable { it.toModLabelDef() },
        labels = this.labels.mapImmutable { it.toLabel() },
    )
}

suspend fun LabelerView.toLabelService(
    agent: MorphoAgent? = null,
): BskyLabelService {
    val fullProfile = agent?.getProfile(this.creator.did)?.getOrNull()?.toProfile() ?: DetailedProfile(
        did = this.creator.did,
        handle = this.creator.handle,
        displayName = this.creator.displayName,
        description = this.creator.description,
        avatar = this.creator.avatar,
        banner = null,
        followersCount = 0,
        followsCount = 0,
        postsCount = 0,
        labels = this.creator.labels.map { it.toLabel() },
        indexedAt = this.creator.indexedAt?.let { Moment(it) },
        mutedByMe = false,
        following = this.creator.viewer?.following?.let { FollowRecord(it) },
        followedBy = this.creator.viewer?.followedBy?.let { FollowRecord(it) },
        numKnownFollowers = 0,
        knownFollowers = persistentListOf(),
        associated = this.creator.associated?.toBskyProfileAssociated(),
        createdAt = this.creator.createdAt?.let { Moment(it) },
        mutedByList = this.creator.viewer?.mutedByList?.toList(),
        block = this.creator.viewer?.blocking?.let { BlockRecord(it) },
        blockedBy = this.creator.viewer?.blockedBy == true,
        blockingByList = this.creator.viewer?.blockingByList?.toList(),
    )
    return BskyLabelService(
        uri = this.uri,
        cid = this.cid,
        creator = fullProfile,
        likeCount = this.likeCount,
        liked = (this.viewer?.like != null),
        likeUri = this.viewer?.like,
        indexedAt = Moment(this.indexedAt),
        policies = persistentListOf(),
        labels = this.labels.mapImmutable { it.toLabel() },
    )
}

fun LabelerViewDetailed.toLabelServiceLocal(): BskyLabelService {
    val fullProfile = DetailedProfile(
        did = this.creator.did,
        handle = this.creator.handle,
        displayName = this.creator.displayName,
        description = this.creator.description,
        avatar = this.creator.avatar,
        banner = null,
        followersCount = 0,
        followsCount = 0,
        postsCount = 0,
        labels = this.creator.labels.map { it.toLabel() },
        indexedAt = this.creator.indexedAt?.let { Moment(it) },
        mutedByMe = false,
        following = this.creator.viewer?.following?.let { FollowRecord(it) },
        followedBy = this.creator.viewer?.followedBy?.let { FollowRecord(it) },
        numKnownFollowers = 0,
        knownFollowers = persistentListOf(),
        associated = this.creator.associated?.toBskyProfileAssociated(),
        createdAt = this.creator.createdAt?.let { Moment(it) },
        mutedByList = this.creator.viewer?.mutedByList?.toList(),
        block = this.creator.viewer?.blocking?.let { BlockRecord(it) },
        blockedBy = this.creator.viewer?.blockedBy == true,
        blockingByList = this.creator.viewer?.blockingByList?.toList(),
    )
    return BskyLabelService(
        uri = this.uri,
        cid = this.cid,
        creator = fullProfile,
        likeCount = this.likeCount,
        liked = (this.viewer?.like != null),
        likeUri = this.viewer?.like,
        indexedAt = Moment(this.indexedAt),
        policies = persistentListOf(),
        labels = this.labels.mapImmutable { it.toLabel() },
    )
}

fun LabelerView.toLabelServiceLocal(): BskyLabelService {
    val fullProfile = DetailedProfile(
        did = this.creator.did,
        handle = this.creator.handle,
        displayName = this.creator.displayName,
        description = this.creator.description,
        avatar = this.creator.avatar,
        banner = null,
        followersCount = 0,
        followsCount = 0,
        postsCount = 0,
        labels = this.creator.labels.map { it.toLabel() },
        indexedAt = this.creator.indexedAt?.let { Moment(it) },
        mutedByMe = false,
        following = this.creator.viewer?.following?.let { FollowRecord(it) },
        followedBy = this.creator.viewer?.followedBy?.let { FollowRecord(it) },
        numKnownFollowers = 0,
        knownFollowers = persistentListOf(),
        associated = this.creator.associated?.toBskyProfileAssociated(),
        createdAt = this.creator.createdAt?.let { Moment(it) },
        mutedByList = this.creator.viewer?.mutedByList?.toList(),
        block = this.creator.viewer?.blocking?.let { BlockRecord(it) },
        blockedBy = this.creator.viewer?.blockedBy == true,
        blockingByList = this.creator.viewer?.blockingByList?.toList(),
    )
    return BskyLabelService(
        uri = this.uri,
        cid = this.cid,
        creator = fullProfile,
        likeCount = this.likeCount,
        liked = (this.viewer?.like != null),
        likeUri = this.viewer?.like,
        indexedAt = Moment(this.indexedAt),
        policies = persistentListOf(),
        labels = this.labels.mapImmutable { it.toLabel() },
    )
}