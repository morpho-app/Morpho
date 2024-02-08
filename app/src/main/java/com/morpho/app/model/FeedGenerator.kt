package morpho.app.model

import app.bsky.feed.GeneratorView
import app.bsky.richtext.Facet
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.Cid
import morpho.app.api.Did

@Serializable
data class FeedGenerator(
    public val uri: AtUri,
    public val cid: Cid,
    public val did: Did,
    public val creator: Profile,
    public val displayName: String,
    public val description: String? = null,
    public val descriptionFacets: ImmutableList<Facet> = persistentListOf(),
    public val avatar: String? = null,
    public val likeCount: Long,
    public val likedByMe: Boolean = false,
    public val likeRecord: AtUri? = null,
    public val indexedAt: Moment,
)


fun GeneratorView.toFeedGenerator() : FeedGenerator  {
    return FeedGenerator(
        uri = uri,
        cid = cid,
        did = did,
        creator = creator.toProfile(),
        displayName = displayName,
        description = description,
        descriptionFacets = descriptionFacets,
        avatar = avatar,
        likeCount = likeCount ?: 0,
        likedByMe = viewer?.like != null,
        likeRecord = viewer?.like,
        indexedAt = Moment(indexedAt)
    )
}