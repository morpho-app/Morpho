package com.morpho.app.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri

@Serializable
sealed interface SkylineQuery {
    @Serializable
    data class GetFeed(
        public val feed: AtUri,
        public val limit: Long? = null,
        public val cursor: String? = null,
    ) : SkylineQuery

    @Serializable
    data class GetPosts(
        public val uris: ImmutableList<AtUri> = persistentListOf(),
    ) : SkylineQuery

    @Serializable
    data class GetTimeline(
        public val algorithm: String? = null,
        public val limit: Long? = null,
        public val cursor: String? = null,
    ) : SkylineQuery

    @Serializable
    data class GetAuthorFeed(
        public val actor: AtIdentifier,
        public val limit: Long? = null,
        public val cursor: String? = null,
    ) : SkylineQuery

    //@Serializable
    //data class GetActorFeeds(
    //    public val actor: AtIdentifier,
    //    public val limit: Long? = null,
    //    public val cursor: String? = null,
    //) : SkylineQuery

}

data class CachedSkylineQuery(
    val query: SkylineQuery,
    var results: MutableList<SkylineItem>,
    val index: Long,
)

