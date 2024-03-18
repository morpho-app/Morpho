package app.bsky.embed

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface RecordWithMediaViewMediaUnion {
  public class ImagesViewSerializer : KSerializer<ImagesView> by valueClassSerializer()

  @Serializable(with = ImagesViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.images#view")
  public value class ImagesView(
    public val `value`: app.bsky.embed.ImagesView,
  ) : RecordWithMediaViewMediaUnion

  public class ExternalViewSerializer : KSerializer<ExternalView> by valueClassSerializer()

  @Serializable(with = ExternalViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.external#view")
  public value class ExternalView(
    public val `value`: app.bsky.embed.ExternalView,
  ) : RecordWithMediaViewMediaUnion
}

@Serializable
public data class RecordWithMediaView(
  public val record: RecordView,
  public val media: RecordWithMediaViewMediaUnion,
)
