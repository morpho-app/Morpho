package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.fastMap
import app.bsky.actor.FeedType
import app.bsky.actor.SavedFeed
import app.bsky.feed.GeneratorView
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.TID
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class FeedGenerator(
    public val uri: AtUri,
    public val cid: Cid,
    public val did: Did,
    public val creator: Profile,
    public val displayName: String,
    public val description: String?,
    public val descriptionFacets: List<BskyFacet>,
    public val avatar: String?,
    public val likeCount: Long,
    public val likedByMe: Boolean,
    public val likeRecord: AtUri?,
    public val indexedAt: Moment,
)


fun GeneratorView.toFeedGenerator() : FeedGenerator {
    return FeedGenerator(
        uri = uri,
        cid = cid,
        did = did,
        creator = creator.toProfile(),
        displayName = displayName,
        description = description,
        descriptionFacets = descriptionFacets.mapImmutable { it.toBskyFacet() },
        avatar = avatar,
        likeCount = likeCount ?: 0,
        likedByMe = viewer?.like != null,
        likeRecord = viewer?.like,
        indexedAt = Moment(indexedAt)
    )
}

fun List<GeneratorView>.toFeedGenList(): List<FeedGenerator> {
    return this.fastMap { it.toFeedGenerator() }
}

fun FeedGenerator.toSavedFeed(pinned: Boolean = false): SavedFeed {
    return SavedFeed(
        id = TID.next().toString(),
        type = FeedType.FEED,
        value = this.uri.atUri,
        pinned = pinned,
    )
}