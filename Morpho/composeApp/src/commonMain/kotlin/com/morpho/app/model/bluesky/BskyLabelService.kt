package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.labeler.LabelerView
import app.bsky.labeler.LabelerViewDetailed
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class BskyLabelService(
    val uri: AtUri,
    val cid: Cid,
    val creator: Profile,
    val likeCount: Long?,
    val liked: Boolean,
    val likeUri: AtUri?,
    val indexedAt: Moment,
    val policies: ImmutableList<BskyLabelDefinition>,
    override val labels: ImmutableList<BskyLabel>,
): Profile {
    override val did: Did
        get() = creator.did
    override val handle: Handle
        get() = creator.handle
    override val displayName: String?
        get() = creator.displayName
    override val avatar: String?
        get() = creator.avatar
    override val mutedByMe: Boolean
        get() = creator.mutedByMe
    override val followingMe: Boolean
        get() = creator.followingMe
    override val followedByMe: Boolean
        get() = creator.followedByMe
}

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