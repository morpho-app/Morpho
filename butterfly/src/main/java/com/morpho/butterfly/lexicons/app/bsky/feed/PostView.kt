package app.bsky.feed

import app.bsky.actor.ProfileViewBasic
import com.atproto.label.Label
import kotlin.Long
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface PostViewEmbedUnion {
  public class ImagesViewSerializer : KSerializer<ImagesView> by valueClassSerializer()

  @Serializable(with = ImagesViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.images#view")
  public value class ImagesView(
    public val `value`: app.bsky.embed.ImagesView,
  ) : PostViewEmbedUnion

  public class ExternalViewSerializer : KSerializer<ExternalView> by valueClassSerializer()

  @Serializable(with = ExternalViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.external#view")
  public value class ExternalView(
    public val `value`: app.bsky.embed.ExternalView,
  ) : PostViewEmbedUnion

  public class RecordViewSerializer : KSerializer<RecordView> by valueClassSerializer()

  @Serializable(with = RecordViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.record#view")
  public value class RecordView(
    public val `value`: app.bsky.embed.RecordView,
  ) : PostViewEmbedUnion

  public class RecordWithMediaViewSerializer : KSerializer<RecordWithMediaView> by
      valueClassSerializer()

  @Serializable(with = RecordWithMediaViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.recordWithMedia#view")
  public value class RecordWithMediaView(
    public val `value`: app.bsky.embed.RecordWithMediaView,
  ) : PostViewEmbedUnion
}

@Serializable
public data class PostView(
  public val uri: AtUri,
  public val cid: Cid,
  public val author: ProfileViewBasic,
  public val record: JsonElement,
  public val embed: PostViewEmbedUnion? = null,
  public val replyCount: Long? = null,
  public val repostCount: Long? = null,
  public val likeCount: Long? = null,
  public val indexedAt: Timestamp,
  public val viewer: ViewerState? = null,
  public val labels: ReadOnlyList<Label> = persistentListOf(),
  public val threadgate: ThreadgateView? = null,
)
