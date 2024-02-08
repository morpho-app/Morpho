package app.bsky.actor

import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface ProfileLabelsUnion {
  public class SelfLabelsSerializer : KSerializer<SelfLabels> by valueClassSerializer()

  @Serializable(with = SelfLabelsSerializer::class)
  @JvmInline
  @SerialName("com.atproto.label.defs#selfLabels")
  public value class SelfLabels(
    public val `value`: com.atproto.label.SelfLabels,
  ) : ProfileLabelsUnion
}

@Serializable
public data class Profile(
  public val displayName: String? = null,
  public val description: String? = null,
  public val avatar: JsonElement? = null,
  public val banner: JsonElement? = null,
  public val labels: ProfileLabelsUnion? = null,
) {
  init {
    require(displayName == null || displayName.count() <= 640) {
      "displayName.count() must be <= 640, but was ${displayName?.count()}"
    }
    require(description == null || description.count() <= 2_560) {
      "description.count() must be <= 2_560, but was ${description?.count()}"
    }
  }
}
