package com.morpho.butterfly.model


import com.atproto.repo.StrongRef
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Nsid

sealed interface RecordUnion {
    val type: RecordType

    @Serializable
    data class MakePost(
        val post: app.bsky.feed.Post
    ) : RecordUnion {
        override val type = RecordType.Post
    }

    @Serializable
    data class Like(val subject: StrongRef) : RecordUnion {
        override val type = RecordType.Like
    }

    @Serializable
    data class Repost(val subject: StrongRef) : RecordUnion {
        override val type = RecordType.Repost
    }



}

enum class RecordType(val collection: Nsid) {
    Post(Nsid("app.bsky.feed.post")),
    Like(Nsid("app.bsky.feed.like")),
    Repost(Nsid("app.bsky.feed.repost")),
}