package radiant.nimbus.model

import app.bsky.embed.ExternalView
import app.bsky.embed.ImagesView
import app.bsky.embed.RecordViewRecordUnion
import app.bsky.embed.RecordWithMediaViewMediaUnion
import app.bsky.feed.Post
import app.bsky.feed.PostViewEmbedUnion
import kotlinx.collections.immutable.ImmutableList
import radiant.nimbus.util.deserialize
import radiant.nimbus.util.mapImmutable
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import sh.christian.ozone.api.Uri

sealed interface BskyPostFeature {
    data class ImagesFeature(
        val images: ImmutableList<EmbedImage>,
    ) : BskyPostFeature, TimelinePostMedia

    data class ExternalFeature(
        val uri: Uri,
        val title: String,
        val description: String,
        val thumb: String?,
    ) : BskyPostFeature, TimelinePostMedia

    data class PostFeature(
        val post: EmbedPost,
    ) : BskyPostFeature

    data class MediaPostFeature(
        val post: EmbedPost,
        val media: TimelinePostMedia,
    ) : BskyPostFeature
}

sealed interface TimelinePostMedia

data class EmbedImage(
    val thumb: String,
    val fullsize: String,
    val alt: String,
)

sealed interface EmbedPost {
    data class VisibleEmbedPost(
        val uri: AtUri,
        val cid: Cid,
        val author: Profile,
        val litePost: LitePost,
    ) : EmbedPost {
        val reference: Reference = Reference(uri, cid)
    }

    data class InvisibleEmbedPost(
        val uri: AtUri,
    ) : EmbedPost

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