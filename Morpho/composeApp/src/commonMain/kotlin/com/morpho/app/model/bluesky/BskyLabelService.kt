package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.labeler.LabelerView
import app.bsky.labeler.LabelerViewDetailed
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
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Immutable
open class BskyLabelService(
    val uri: AtUri,
    val cid: Cid,
    val creator: Profile?,
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