package radiant.nimbus.model

import app.bsky.embed.ExternalView
import app.bsky.embed.ImagesView
import app.bsky.embed.RecordViewRecordUnion
import app.bsky.embed.RecordWithMediaViewMediaUnion
import app.bsky.feed.Post
import app.bsky.feed.PostEmbedUnion
import app.bsky.feed.PostViewEmbedUnion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.util.deserialize
import radiant.nimbus.util.mapImmutable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Uri

sealed interface BskyPostFeature {
    @Serializable
    data class ImagesFeature(
        val images: ImmutableList<EmbedImage>,
    ) : BskyPostFeature, TimelinePostMedia

    @Serializable
    data class ExternalFeature(
        val uri: Uri,
        val title: String,
        val description: String,
        val thumb: String?,
    ) : BskyPostFeature, TimelinePostMedia

    @Serializable
    data class PostFeature(
        val post: EmbedPost,
    ) : BskyPostFeature

    @Serializable
    data class MediaPostFeature(
        val post: EmbedPost,
        val media: TimelinePostMedia,
    ) : BskyPostFeature
}

sealed interface TimelinePostMedia

@Serializable
data class EmbedImage(
    val thumb: String,
    val fullsize: String,
    val alt: String,
)

sealed interface EmbedPost {

    @Serializable
    data class VisibleEmbedPost(
        val uri: AtUri,
        val cid: Cid,
        val author: Profile,
        val litePost: LitePost,
    ) : EmbedPost {
        val reference: Reference = Reference(uri, cid)
    }

    @Serializable
    data class InvisibleEmbedPost(
        val uri: AtUri,
    ) : EmbedPost

    @Serializable
    data class BlockedEmbedPost(
        val uri: AtUri,
    ) : EmbedPost
}

fun PostViewEmbedUnion.toFeature(): BskyPostFeature {
    return when (this) {
        is PostViewEmbedUnion.ImagesView -> {
            value.toImagesFeature()
        }
        is PostViewEmbedUnion.ExternalView -> {
            value.toExternalFeature()
        }
        is PostViewEmbedUnion.RecordView -> {
            BskyPostFeature.PostFeature(
                post = value.record.toEmbedPost(),
            )
        }
        is PostViewEmbedUnion.RecordWithMediaView -> {
            BskyPostFeature.MediaPostFeature(
                post = value.record.record.toEmbedPost(),
                media = when (val media = value.media) {
                    is RecordWithMediaViewMediaUnion.ExternalView -> media.value.toExternalFeature()
                    is RecordWithMediaViewMediaUnion.ImagesView -> media.value.toImagesFeature()
                },
            )
        }
    }
}

private fun ImagesView.toImagesFeature(): BskyPostFeature.ImagesFeature {
    return BskyPostFeature.ImagesFeature(
        images = images.mapImmutable {
            EmbedImage(
                thumb = it.thumb,
                fullsize = it.fullsize,
                alt = it.alt,
            )
        }
    )
}

private fun ExternalView.toExternalFeature(): BskyPostFeature.ExternalFeature {
    return BskyPostFeature.ExternalFeature(
        uri = external.uri,
        title = external.title,
        description = external.description,
        thumb = external.thumb,
    )
}

private fun RecordViewRecordUnion.toEmbedPost(): EmbedPost {
    return when (this) {
        is RecordViewRecordUnion.ViewBlocked -> {
            EmbedPost.BlockedEmbedPost(
                uri = value.uri,
            )
        }
        is RecordViewRecordUnion.ViewNotFound -> {
            EmbedPost.InvisibleEmbedPost(
                uri = value.uri,
            )
        }
        is RecordViewRecordUnion.ViewRecord -> {
            // TODO verify via recordType before blindly deserialized.
            val litePost = Post.serializer().deserialize(value.value).toLitePost()

            EmbedPost.VisibleEmbedPost(
                uri = value.uri,
                cid = value.cid,
                author = value.author.toProfile(),
                litePost = litePost,
            )
        }
        is RecordViewRecordUnion.FeedGeneratorView -> {
            // TODO support generator views.
            EmbedPost.InvisibleEmbedPost(
                uri = value.uri,
            )
        }
        is RecordViewRecordUnion.GraphListView -> {
            // TODO support graph list views.
            EmbedPost.InvisibleEmbedPost(
                uri = value.uri,
            )
        }
    }
}

public fun PostEmbedUnion.toFeature(): BskyPostFeature? {
    return when (this) {
        is PostEmbedUnion.Images -> {
            this.toEmbedImagesFeature()
        }
        is PostEmbedUnion.External -> {
            this.toEmbedExternalFeature()
        }
        is PostEmbedUnion.Record -> {
            null // Don't nest embeds too hard
        }
        is PostEmbedUnion.RecordWithMedia -> {
            null // Don't nest embeds too hard
        }
    }
}

private fun PostEmbedUnion.External.toEmbedExternalFeature(): BskyPostFeature.ExternalFeature {
    return BskyPostFeature.ExternalFeature(
        uri = this.value.external.uri,
        title = this.value.external.title,
        description = this.value.external.description,
        thumb = this.value.external.thumb.toString(),
    )
}

private fun PostEmbedUnion.Images.toEmbedImagesFeature(): BskyPostFeature.ImagesFeature {
    return BskyPostFeature.ImagesFeature(
        images = this.value.images.mapImmutable {
            EmbedImage(
                thumb = it.image.toString(),
                fullsize = it.image.toString(),
                alt = it.alt,
            )
        }
    )
}