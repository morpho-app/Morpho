package app.bsky.actor

import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.valueClassSerializer

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

  public class PersonalDetailsPrefSerializer : KSerializer<PersonalDetailsPref> by valueClassSerializer()

  @Serializable(with = PersonalDetailsPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#personalDetailsPref")
  public value class PersonalDetailsPref(
    public val `value`: app.bsky.actor.PersonalDetailsPref,
  ) : PreferencesUnion


  public class SkyFeedBuilderFeedsPrefSerializer : KSerializer<SkyFeedBuilderFeedsPref> by valueClassSerializer()

  @Serializable(with = SkyFeedBuilderFeedsPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#skyfeedBuilderFeedsPref")
  public value class SkyFeedBuilderFeedsPref(
    public val `value`: app.bsky.actor.SkyFeedBuilderFeedsPref,
  ) : PreferencesUnion

  public class FeedViewPrefSerializer : KSerializer<FeedViewPref> by valueClassSerializer()

  @Serializable(with = FeedViewPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#feedViewPref")
  public value class FeedViewPref(
    public val `value`: app.bsky.actor.FeedViewPref,
  ) : PreferencesUnion

  public class MutedWordsPrefSerializer : KSerializer<MutedWordsPref> by valueClassSerializer()

  @Serializable(with = MutedWordsPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#mutedWordsPref")
  public value class MutedWordsPref(
    public val `value`: app.bsky.actor.MutedWordsPref,
  ) : PreferencesUnion

  public class ThreadViewPrefSerializer : KSerializer<ThreadViewPref> by valueClassSerializer()
  @Serializable(with = ThreadViewPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#threadViewPref")
  public value class ThreadViewPref(
    public val `value`: app.bsky.actor.ThreadViewPref,
  ) : PreferencesUnion

  public class HiddenPostsPrefSerializer : KSerializer<HiddenPostsPref> by valueClassSerializer()
  @Serializable(with = HiddenPostsPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#hiddenPostsPref")
  public value class HiddenPostsPref(
    public val `value`: app.bsky.actor.HiddenPostsPref,
  ) : PreferencesUnion

  public class LabelersPrefSerializer : KSerializer<LabelersPref> by valueClassSerializer()
  @Serializable(with = LabelersPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#labelersPref")
  public value class LabelersPref(
    public val `value`: app.bsky.actor.LabelersPref,
  ) : PreferencesUnion

  public class InterestsPrefSerializer : KSerializer<InterestsPref> by valueClassSerializer()
  @Serializable(with = InterestsPrefSerializer::class)
  @JvmInline
  @SerialName("app.bsky.actor.defs#interestsPref")
  public value class InterestsPref(
    public val `value`: app.bsky.actor.InterestsPref,
  ) : PreferencesUnion
}

@Serializable
public data class SkyFeedBuilderFeedsPref(
  /**
   * List of feeds
   */
  public val feeds: ReadOnlyList<AtUri>,
)