package app.bsky.embed

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface RecordWithMediaMediaUnion {
  public class ImagesSerializer : KSerializer<Images> by valueClassSerializer()

  @Serializable(with = ImagesSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.images")
  public value class Images(
    public val `value`: app.bsky.embed.Images,
  ) : RecordWithMediaMediaUnion

  public class ExternalSerializer : KSerializer<External> by valueClassSerializer()

  @Serializable(with = ExternalSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.external")
  public value class External(
    public val `value`: app.bsky.embed.External,
  ) : RecordWithMediaMediaUnion
}

@Serializable
public data class RecordWithMedia(
  public val record: Record,
  public val media: RecordWithMediaMediaUnion,
)
