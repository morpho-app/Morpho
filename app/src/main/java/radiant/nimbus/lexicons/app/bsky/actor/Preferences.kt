package app.bsky.actor

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface PreferencesUnion {
  public class AdultContentPrefSerializer : KSerializer<AdultContentPref> by valueClassSerializer()

  @Serializable(with = AdultContentPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#adultContentPref")
  public value class AdultContentPref(
    public val `value`: app.bsky.actor.AdultContentPref,
  ) : PreferencesUnion

  public class ContentLabelPrefSerializer : KSerializer<ContentLabelPref> by valueClassSerializer()

  @Serializable(with = ContentLabelPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#contentLabelPref")
  public value class ContentLabelPref(
    public val `value`: app.bsky.actor.ContentLabelPref,
  ) : PreferencesUnion

  public class SavedFeedsPrefSerializer : KSerializer<SavedFeedsPref> by valueClassSerializer()

  @Serializable(with = SavedFeedsPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#savedFeedsPref")
  public value class SavedFeedsPref(
    public val `value`: app.bsky.actor.SavedFeedsPref,
  ) : PreferencesUnion

  public class PersonalDetailsPrefSerializer : KSerializer<PersonalDetailsPref> by
      valueClassSerializer()

  @Serializable(with = PersonalDetailsPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#personalDetailsPref")
  public value class PersonalDetailsPref(
    public val `value`: app.bsky.actor.PersonalDetailsPref,
  ) : PreferencesUnion

  public class FeedViewPrefSerializer : KSerializer<FeedViewPref> by valueClassSerializer()

  @Serializable(with = FeedViewPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#feedViewPref")
  public value class FeedViewPref(
    public val `value`: app.bsky.actor.FeedViewPref,
  ) : PreferencesUnion

  public class ThreadViewPrefSerializer : KSerializer<ThreadViewPref> by valueClassSerializer()

  @Serializable(with = ThreadViewPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#threadViewPref")
  public value class ThreadViewPref(
    public val `value`: app.bsky.actor.ThreadViewPref,
  ) : PreferencesUnion
}
