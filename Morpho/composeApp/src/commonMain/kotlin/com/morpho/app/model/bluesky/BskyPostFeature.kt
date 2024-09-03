package com.morpho.app.model.bluesky

import app.bsky.embed.*
import app.bsky.feed.Post
import app.bsky.feed.PostEmbedUnion
import app.bsky.feed.PostViewEmbedUnion
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.*
import com.morpho.butterfly.model.Blob
import kotlinx.serialization.Serializable

@Serializable
sealed interface BskyPostFeature {
    @Serializable
    data class ImagesFeature(
        val images: List<EmbedImage>,
    ) : BskyPostFeature, TimelinePostMedia

    @Serializable
    data class VideoFeature(
        val video: VideoEmbed,
        val alt: String,
        val aspectRatio: AspectRatio?,
    ) : BskyPostFeature, TimelinePostMedia

    @Serializable
    data class ExternalFeature(
        val uri: Uri,
        val title: String,
        val description: String,
        val thumb: String?,
    ) : BskyPostFeature, TimelinePostMedia

    @Serializable
    data class RecordFeature(
        val record: EmbedRecord,
    ) : BskyPostFeature

    @Serializable
    data class MediaRecordFeature(
        val record: EmbedRecord,
        val media: TimelinePostMedia,
    ) : BskyPostFeature

    @Serializable
    data class UnknownEmbed(
        val value: String,
    ) : BskyPostFeature
}

@Serializable
sealed interface TimelinePostMedia

@Serializable
sealed interface VideoEmbed

@Serializable
data class EmbedVideoView(
    val cid: Cid,
    val playlist: AtUri,
    val thumbnail: AtUri,
): VideoEmbed

@Serializable
data class EmbedVideo(
    val blob: Blob,
    val captions: List<VideoCaption>?,
): VideoEmbed

@Serializable
data class EmbedImage(
    val thumb: String,
    val fullsize: String,
    val alt: String,
    val aspectRatio: AspectRatio? = null,
)



@Serializable
sealed interface EmbedRecord {

    @Serializable
    data class VisibleEmbedPost(
        val uri: AtUri,
        val cid: Cid,
        val author: Profile,
        val litePost: LitePost,
    ) : EmbedRecord {
        val reference: Reference = Reference(uri, cid)
    }

    @Serializable
    data class EmbedFeed(
        val uri: AtUri,
        val cid: Cid,
        val did: Did,
        val author: Profile,
        val feed: FeedGenerator,
    ) : EmbedRecord

    @Serializable
    data class EmbedList(
        val uri: AtUri,
        val cid: Cid,
        val author: Profile,
        val list: BskyList,
    ) : EmbedRecord

    @Serializable
    data class EmbedLabelService(
        val uri: AtUri,
        val cid: Cid,
        val author: Profile,
        val labelService: BskyLabelService,
    ) : EmbedRecord


    @Serializable
    data class InvisibleEmbedPost(
        val uri: AtUri,
    ) : EmbedRecord

    @Serializable
    data class BlockedEmbedPost(
        val uri: AtUri,
    ) : EmbedRecord

    @Serializable
    data class DetachedQuotePost(
        val uri: AtUri,
    ) : EmbedRecord

    @Serializable
    data class EmbedVideo(
        val video: VideoEmbed,
        val alt: String,
        val aspectRatio: AspectRatio?,
    ) : EmbedRecord

    @Serializable
    data class UnknownEmbed(
        val value: String,
    ) : EmbedRecord
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
            BskyPostFeature.RecordFeature(
                record = value.record.toEmbedRecord(),
            )
        }
        is PostViewEmbedUnion.VideoView -> {
            BskyPostFeature.VideoFeature(
                video = EmbedVideo(
                    blob = value.video,
                    captions = value.captions?.mapImmutable {
                        VideoCaption(
                            lang = it.lang,
                            file = it.file,
                        )
                    }
                ),
                alt = value.alt?:"",
                aspectRatio = value.aspectRatio,
            )
        }
        is PostViewEmbedUnion.VideoViewVideo -> {
            BskyPostFeature.VideoFeature(
                video = EmbedVideoView(
                    cid = value.cid,
                    playlist = value.playlist,
                    thumbnail = value.thumbnail,
                ),
                alt = value.alt?:"",
                aspectRatio = value.aspectRatio,
            )
        }
        is PostViewEmbedUnion.RecordWithMediaView -> {
            BskyPostFeature.MediaRecordFeature(
                record = value.record.record.toEmbedRecord(),
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
                aspectRatio = it.aspectRatio
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

private fun RecordViewRecordUnion.toEmbedRecord(): EmbedRecord {
    return when (this) {
        is RecordViewRecordUnion.ViewBlocked -> {
            EmbedRecord.BlockedEmbedPost(
                uri = value.uri,
            )
        }
        is RecordViewRecordUnion.ViewNotFound -> {
            EmbedRecord.InvisibleEmbedPost(
                uri = value.uri,
            )
        }
        is RecordViewRecordUnion.ViewDetached -> {
            EmbedRecord.DetachedQuotePost(
                uri = value.uri,
            )
        }

        is RecordViewRecordUnion.ViewRecord -> {
            // TODO verify via recordType before blindly deserialized.
            val litePost = Post.serializer().deserialize(value.value).toLitePost()

            EmbedRecord.VisibleEmbedPost(
                uri = value.uri,
                cid = value.cid,
                author = value.author.toProfile(),
                litePost = litePost,
            )
        }
        is RecordViewRecordUnion.FeedGeneratorView -> {

            EmbedRecord.EmbedFeed(
                uri = value.uri,
                cid = value.cid,
                did = value.did,
                author = value.creator.toProfile(),
                feed = value.toFeedGenerator(),
            )
        }
        is RecordViewRecordUnion.GraphListView -> {

            EmbedRecord.EmbedList(
                uri = value.uri,
                cid = value.cid,
                author = value.creator.toProfile(),
                list = value.toList(),
            )
        }

        is RecordViewRecordUnion.LabelerLabelerView -> {

            EmbedRecord.EmbedLabelService(
                uri = value.uri,
                cid = value.cid,
                author = value.creator.toProfile(),
                labelService = value.toLabelService(),
            )
        }

        is RecordViewRecordUnion.VideoView -> {
            EmbedRecord.EmbedVideo(
                video = EmbedVideo(
                    blob = value.video,
                    captions = value.captions?.mapImmutable {
                        VideoCaption(
                            lang = it.lang,
                            file = it.file,
                        )
                    }
                ),
                alt = value.alt?:"",
                aspectRatio = value.aspectRatio,
            )
        }
        is RecordViewRecordUnion.VideoViewVideo ->{
            EmbedRecord.EmbedVideo(
                video = EmbedVideoView(
                    cid = value.cid,
                    playlist = value.playlist,
                    thumbnail = value.thumbnail,
                ),
                alt = value.alt?:"",
                aspectRatio = value.aspectRatio,
            )
        }
    }
}

public fun PostEmbedUnion.toFeature(): BskyPostFeature? {
    return when (this) {
        is PostEmbedUnion.ImagesView -> {
            this.toEmbedImagesFeature()
        }
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

        is PostEmbedUnion.VideoView -> {
            this.toEmbedVideoFeature()
        }
        is PostEmbedUnion.VideoViewVideo -> {
            this.toEmbedVideoFeature()
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

private fun PostEmbedUnion.ImagesView.toEmbedImagesFeature(): BskyPostFeature.ImagesFeature {
    return BskyPostFeature.ImagesFeature(
        images = this.value.images.mapImmutable {

            EmbedImage(
                thumb = it.thumb,
                fullsize = it.fullsize,
                alt = it.alt,
                aspectRatio = it.aspectRatio
            )
        }
    )
}

private fun PostEmbedUnion.Images.toEmbedImagesFeature(): BskyPostFeature.ImagesFeature {
    return BskyPostFeature.ImagesFeature(
        images = this.value.images.mapImmutable {
            val thumbLink = when(it.image) {
                is Blob.StandardBlob -> (it.image as Blob.StandardBlob).ref.link
                is Blob.LegacyBlob -> (it.image as Blob.LegacyBlob).cid
            }
            EmbedImage(
                thumb = thumbLink,
                fullsize = thumbLink,
                alt = it.alt,
                aspectRatio = it.aspectRatio
            )
        }
    )
}

private fun PostEmbedUnion.VideoView.toEmbedVideoFeature(): BskyPostFeature.VideoFeature {
    return BskyPostFeature.VideoFeature(
        video = EmbedVideo(
            blob = this.value.video,
            captions = this.value.captions?.mapImmutable {
                VideoCaption(
                    lang = it.lang,
                    file = it.file,
                )
            },
        ),
        alt = this.value.alt?:"",
        aspectRatio = this.value.aspectRatio,
    )
}

private fun PostEmbedUnion.VideoViewVideo.toEmbedVideoFeature(): BskyPostFeature.VideoFeature {
    return BskyPostFeature.VideoFeature(
        video = EmbedVideoView(
            cid = this.value.cid,
            playlist = this.value.playlist,
            thumbnail = this.value.thumbnail,
        ),
        alt = this.value.alt?:"",
        aspectRatio = this.value.aspectRatio,
    )
}