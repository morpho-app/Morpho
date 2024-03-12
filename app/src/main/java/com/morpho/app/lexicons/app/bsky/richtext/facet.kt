package app.bsky.richtext

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface FacetFeatureUnion {
  public class MentionSerializer : KSerializer<Mention> by valueClassSerializer()

  @Serializable(with = MentionSerializer::class)
  @JvmInline
  @SerialName("app.bsky.richtext.facet#mention")
  public value class Mention(
    public val `value`: FacetMention,
  ) : FacetFeatureUnion

  public class LinkSerializer : KSerializer<Link> by valueClassSerializer()

  @Serializable(with = LinkSerializer::class)
  @JvmInline
  @SerialName("app.bsky.richtext.facet#link")
  public value class Link(
    public val `value`: FacetLink,
  ) : FacetFeatureUnion

  public class TagSerializer : KSerializer<Tag> by valueClassSerializer()

  @Serializable(with = TagSerializer::class)
  @JvmInline
  @SerialName("app.bsky.richtext.facet#tag")
  public value class Tag(
    public val `value`: FacetTag,
  ) : FacetFeatureUnion


  public class PollBlueOptionFacetSerializer : KSerializer<PollBlueOption> by valueClassSerializer()
  @Serializable(with = PollBlueOptionFacetSerializer::class)
  @JvmInline
  @SerialName("blue.poll.post.facet#option")
  public value class PollBlueOption(
    public val `value`: PollBlueOptionFacet,
  ) : FacetFeatureUnion

  public class PollBlueQuestionFacetSerializer : KSerializer<PollBlueQuestion> by valueClassSerializer()
  @Serializable(with = PollBlueQuestionFacetSerializer::class)
  @JvmInline
  @SerialName("blue.poll.post.facet#question")
  public value class PollBlueQuestion(
    public val `value`: PollBlueOptionFacet,
  ) : FacetFeatureUnion
}


@Serializable
public data class Facet(
  public val index: FacetByteSlice,
  public val features: ReadOnlyList<FacetFeatureUnion>,
)
