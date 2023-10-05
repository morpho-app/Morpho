package app.bsky.feed

import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.model.Timestamp
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface ThreadgateAllowUnion {
  public class MentionRuleSerializer : KSerializer<MentionRule> by valueClassSerializer()

  @Serializable(with = MentionRuleSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.threadgate#mentionRule")
  public value class MentionRule(
    public val `value`: ThreadgateMentionRule,
  ) : ThreadgateAllowUnion

  public class FollowingRuleSerializer : KSerializer<FollowingRule> by valueClassSerializer()

  @Serializable(with = FollowingRuleSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.threadgate#followingRule")
  public value class FollowingRule(
    public val `value`: ThreadgateFollowingRule,
  ) : ThreadgateAllowUnion

  public class ListRuleSerializer : KSerializer<ListRule> by valueClassSerializer()

  @Serializable(with = ListRuleSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.threadgate#listRule")
  public value class ListRule(
    public val `value`: ThreadgateListRule,
  ) : ThreadgateAllowUnion
}

@Serializable
public data class Threadgate(
  public val post: AtUri,
  public val allow: ReadOnlyList<ThreadgateAllowUnion> = persistentListOf(),
  public val createdAt: Timestamp,
) {
  init {
    require(allow == null || allow.count() <= 5) {
      "allow.count() must be <= 5, but was ${allow?.count()}"
    }
  }
}
