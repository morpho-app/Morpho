package com.atproto.admin

import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Cid
import com.morpho.butterfly.model.Timestamp
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface BlobViewDetailsUnion {
  public class ImageDetailsSerializer : KSerializer<ImageDetails> by valueClassSerializer()

  @Serializable(with = ImageDetailsSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#imageDetails")
  public value class ImageDetails(
    public val `value`: com.atproto.admin.ImageDetails,
  ) : BlobViewDetailsUnion

  public class VideoDetailsSerializer : KSerializer<VideoDetails> by valueClassSerializer()

  @Serializable(with = VideoDetailsSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#videoDetails")
  public value class VideoDetails(
    public val `value`: com.atproto.admin.VideoDetails,
  ) : BlobViewDetailsUnion
}

@Serializable
public data class BlobView(
  public val cid: Cid,
  public val mimeType: String,
  public val size: Long,
  public val createdAt: Timestamp,
  public val details: BlobViewDetailsUnion? = null,
  public val moderation: Moderation? = null,
)
