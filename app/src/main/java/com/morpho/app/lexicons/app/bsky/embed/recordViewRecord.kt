package app.bsky.embed

import app.bsky.actor.ProfileViewBasic
import com.atproto.label.Label
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import morpho.app.api.AtUri
import morpho.app.api.Cid
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.model.Timestamp
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface RecordViewRecordEmbedUnion {
  public class ImagesViewSerializer : KSerializer<ImagesView> by valueClassSerializer()

  @Serializable(with = ImagesViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.images#view")
  public value class ImagesView(
    public val `value`: app.bsky.embed.ImagesView,
  ) : RecordViewRecordEmbedUnion

  public class ExternalViewSerializer : KSerializer<ExternalView> by valueClassSerializer()

  @Serializable(with = ExternalViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.external#view")
  public value class ExternalView(
    public val `value`: app.bsky.embed.ExternalView,
  ) : RecordViewRecordEmbedUnion

  public class RecordViewSerializer : KSerializer<RecordView> by valueClassSerializer()

  @Serializable(with = RecordViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.record#view")
  public value class RecordView(
    public val `value`: app.bsky.embed.RecordView,
  ) : RecordViewRecordEmbedUnion

  public class RecordWithMediaViewSerializer : KSerializer<RecordWithMediaView> by
      valueClassSerializer()

  @Serializable(with = RecordWithMediaViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.recordWithMedia#view")
  public value class RecordWithMediaView(
    public val `value`: app.bsky.embed.RecordWithMediaView,
  ) : RecordViewRecordEmbedUnion
}

@Serializable
public data class RecordViewRecord(
  public val uri: AtUri,
  public val cid: Cid,
  public val author: ProfileViewBasic,
  public val `value`: JsonElement,
  public val labels: ReadOnlyList<Label> = persistentListOf(),
  public val embeds: ReadOnlyList<RecordViewRecordEmbedUnion> = persistentListOf(),
  public val indexedAt: Timestamp,
)
