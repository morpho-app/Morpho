package app.bsky.graph

import app.bsky.richtext.Facet
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.model.Timestamp
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface ListLabelsUnion {
  public class SelfLabelsSerializer : KSerializer<SelfLabels> by valueClassSerializer()

  @Serializable(with = SelfLabelsSerializer::class)
  @JvmInline
  @SerialName("com.atproto.label.defs#selfLabels")
  public value class SelfLabels(
    public val `value`: com.atproto.label.SelfLabels,
  ) : ListLabelsUnion
}

@Serializable
public data class List(
  public val purpose: Token,
  public val name: String,
  public val description: String? = null,
  public val descriptionFacets: ReadOnlyList<Facet> = persistentListOf(),
  public val avatar: JsonElement? = null,
  public val labels: ListLabelsUnion? = null,
  public val createdAt: Timestamp,
) {
  init {
    require(name.count() >= 1) {
      "name.count() must be >= 1, but was ${name.count()}"
    }
    require(name.count() <= 64) {
      "name.count() must be <= 64, but was ${name.count()}"
    }
    require(description == null || description.count() <= 3_000) {
      "description.count() must be <= 3_000, but was ${description?.count()}"
    }
  }
}
