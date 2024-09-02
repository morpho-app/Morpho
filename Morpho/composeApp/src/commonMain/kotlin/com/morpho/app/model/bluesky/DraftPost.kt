package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.embed.AspectRatio
import app.bsky.embed.Images
import app.bsky.feed.Post
import app.bsky.feed.PostEmbedUnion
import app.bsky.feed.PostLabelsUnion
import app.bsky.feed.PostReplyRef
import app.bsky.richtext.Facet
import com.atproto.repo.StrongRef
import com.morpho.app.data.SharedImage
import com.morpho.app.data.imageToBlob
import com.morpho.app.util.makeBlueskyText
import com.morpho.app.util.resolveBlueskyText
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.Language
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
data class DraftPost(
    val text: String = "",
    val facets: MutableList<Facet> = mutableListOf(),
    val reply: BskyPost? = null,
    val embed: PostEmbedUnion? = null,
    val quote: BskyPost? = null,
    val langs: MutableList<Language> = mutableListOf(),
    val labels: PostLabelsUnion? = null,
    /**
     * Additional non-inline tags describing this post.
     */
    val tags: MutableList<String> = mutableListOf(),

    val images: MutableList<DraftImage> = mutableListOf(),
) {
    suspend fun createPost(api: Butterfly): Post {
        val text = makeBlueskyText(text)
        val blueskyText = resolveBlueskyText(text, api).getOrDefault(text)
        val replyRef = if (reply != null) {
            val root = if (reply.reply?.root != null) {
                StrongRef(reply.reply.root.uri, reply.reply.root.cid)
            } else if (reply.reply?.parent != null) {
                StrongRef(reply.reply.parent.uri, reply.reply.parent.cid)
            } else {
                StrongRef(reply.uri, reply.cid)
            }
            val parent = StrongRef(reply.uri, reply.cid)
            val grandParentAuthor = (if (reply.reply?.parent != null) {
                reply.reply.grandparentAuthor
            } else {
                reply.author
            })?.toProfileViewBasic()
            PostReplyRef(root, parent, grandParentAuthor)
        } else null
        val quoteRef = quote?.let {
            StrongRef(it.uri, it.cid)
        }
        // TODO: handle link embeds, etc.
        val embed = if(images.isEmpty() && quoteRef != null) {
            PostEmbedUnion.Record(value = app.bsky.embed.Record(quoteRef))
        } else if (images.isNotEmpty() && quoteRef != null) {
            PostEmbedUnion.RecordWithMedia(
                value = app.bsky.embed.RecordWithMedia(
                    app.bsky.embed.Record(quoteRef),
                    app.bsky.embed.RecordWithMediaMediaUnion.Images(
                        Images(
                            images.mapNotNull {
                                it.toImageRef(api)
                            }.toPersistentList()
                        )
                    )
                )
            )
        } else if (images.isNotEmpty()){
            PostEmbedUnion.Images(
                Images(
                    images.mapNotNull {
                        it.toImageRef(api)
                    }.toPersistentList()
                )
            )
        } else {
            null
        }
        return Post(
            text =  blueskyText.text,
            facets = blueskyText.facets.map { it.toFacet() }.toPersistentList(),
            reply = replyRef,
            embed = embed,
            createdAt = Clock.System.now(),
            langs = langs.toPersistentList(),
            labels = labels,
            tags = tags.toPersistentList(),
        )
    }
}


@Immutable
@Serializable
data class DraftImage(
    val image: @Contextual SharedImage,
    val altText: String? = null,
    val aspectRatio: AspectRatio? = null,
) {
    suspend fun toImageRef(api: Butterfly) : app.bsky.embed.ImagesImage? {
        return app.bsky.embed.ImagesImage(
            image = imageToBlob(image, api)?: return null,
            alt = altText ?: "",
            aspectRatio = aspectRatio,
        )
    }
}