package app.bsky.feed

import app.bsky.richtext.Facet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Language
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface PostEmbedUnion {
  public class ImagesSerializer : KSerializer<Images> by valueClassSerializer()

  @Serializable(with = ImagesSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.images")
  public value class Images(
    public val `value`: app.bsky.embed.Images,
  ) : PostEmbedUnion

  public class ExternalSerializer : KSerializer<External> by valueClassSerializer()

  @Serializable(with = ExternalSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.external")
  public value class External(
    public val `value`: app.bsky.embed.External,
  ) : PostEmbedUnion

  public class ExternalMainSerializer : KSerializer<ExternalMain> by valueClassSerializer()
  @Serializable(with = ExternalMainSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.external#main")
  public value class ExternalMain(
    public val `value`: app.bsky.embed.ExternalMain,
  ) : PostEmbedUnion

  public class RecordSerializer : KSerializer<Record> by valueClassSerializer()

  @Serializable(with = RecordSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.record")
  public value class Record(
    public val `value`: app.bsky.embed.Record,
  ) : PostEmbedUnion

  public class RecordMainSerializer : KSerializer<RecordMain> by valueClassSerializer()

  @Serializable(with = RecordMainSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.record#main")
  public value class RecordMain(
    public val `value`: app.bsky.embed.RecordMain,
  ) : PostEmbedUnion


  public class RecordWithMediaSerializer : KSerializer<RecordWithMedia> by valueClassSerializer()

  @Serializable(with = RecordWithMediaSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.recordWithMedia")
  public value class RecordWithMedia(
    public val `value`: app.bsky.embed.RecordWithMedia,
  ) : PostEmbedUnion
}

@Serializable
public sealed interface PostLabelsUnion {
  public class SelfLabelsSerializer : KSerializer<SelfLabels> by valueClassSerializer()

  @Serializable(with = SelfLabelsSerializer::class)
  @JvmInline
  @SerialName("com.atproto.label.defs#selfLabels")
  public value class SelfLabels(
    public val `value`: com.atproto.label.SelfLabels,
  ) : PostLabelsUnion
}

@Serializable
public data class Post(
  public val text: String,
  /**
   * Deprecated: replaced by app.bsky.richtext.facet.
   */
  public val entities: ReadOnlyList<PostEntity> = persistentListOf(),
  public val facets: ReadOnlyList<Facet> = persistentListOf(),
  public val reply: PostReplyRef? = null,
  public val embed: PostEmbedUnion? = null,
  public val langs: ReadOnlyList<Language> = persistentListOf(),
  public val labels: PostLabelsUnion? = null,
  /**
   * Additional non-inline tags describing this post.
   */
  public val tags: ReadOnlyList<String> = persistentListOf(),
  public val createdAt: Timestamp,
) {
  init {
    require(text.count() <= 3_000) {
      "text.count() must be <= 3_000, but was ${text.count()}"
    }
    require(langs.count() <= 3) {
      "langs.count() must be <= 3, but was ${langs.count()}"
    }
    require(tags.count() <= 8) {
      "tags.count() must be <= 8, but was ${tags.count()}"
    }
    require(tags.count() <= 640) {
      "tags.count() must be <= 640, but was ${tags.count()}"
    }
  }
}
