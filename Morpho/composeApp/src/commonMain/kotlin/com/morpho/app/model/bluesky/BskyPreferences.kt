package com.morpho.app.model.bluesky


//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import androidx.compose.runtime.Immutable
import app.bsky.actor.*
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable

@Serializable
public data class BskyPreferences(
    public var personalDetails: PersonalDetailsPref? = null,
    public var adultContent: AdultContentPref? = null,
    public val feedViewPrefs: MutableMap<String, BskyFeedPref> = mutableMapOf(),
    public var skyFeedBuilderFeeds: SkyFeedBuilderFeedsPref? = null,
    public var savedFeeds: SavedFeedsPref? = null,
    public val contentLabelPrefs: MutableList<ContentLabelPref> = mutableListOf(),
    public var threadViewPrefs: ThreadViewPref? = null,
    // Get system languages and allow customization of this
    public var languages: List<Language> = persistentListOf(),
    public var mergeFeeds: Boolean = false,
    public val mutes: MutableList<BasicProfile> = mutableListOf(),
    public val listsMuted: MutableMap<AtUri, BskyList> = mutableMapOf(),
    public var mutedWords: List<MutedWord> = persistentListOf(),
    public var hiddenPosts: List<AtUri> = persistentListOf(),
    public var labelers: List<Did> = persistentListOf(),
) {
    fun toRemotePrefs(): ImmutableList<PreferencesUnion> {
        val prefs = persistentListOf<PreferencesUnion>()
        if (this.adultContent != null) prefs.add(PreferencesUnion.AdultContentPref(this.adultContent!!))
        if (this.personalDetails != null) prefs.add(PreferencesUnion.PersonalDetailsPref(this.personalDetails!!))
        if (this.savedFeeds != null) prefs.add(PreferencesUnion.SavedFeedsPref(this.savedFeeds!!))
        if (this.skyFeedBuilderFeeds != null) prefs.add(
            PreferencesUnion.SkyFeedBuilderFeedsPref(this.skyFeedBuilderFeeds!!))
        if (this.threadViewPrefs != null) prefs.add(PreferencesUnion.ThreadViewPref(this.threadViewPrefs!!))
        if (this.feedViewPrefs.isNotEmpty()) this.feedViewPrefs.map { PreferencesUnion.FeedViewPref(
            FeedViewPref(
                it.key,
                it.value.hideReplies,
                it.value.hideRepliesByUnfollowed,
                it.value.hideRepliesByLikeCount,
                it.value.hideReposts,
                it.value.hideQuotePosts,
                if(it.key == "home") this.mergeFeeds else null,
        )) }
        if (this.contentLabelPrefs.isNotEmpty()) this.contentLabelPrefs.map {
            prefs.add(PreferencesUnion.ContentLabelPref(it)) }
        if (this.mutedWords.isNotEmpty()) prefs.add(
            PreferencesUnion.MutedWordsPref(MutedWordsPref(this.mutedWords.toImmutableList())))
        if (this.labelers.isNotEmpty()) prefs.add(
            PreferencesUnion.LabelersPref(
                LabelersPref(this.labelers.toImmutableList().mapImmutable { LabelerPrefItem(it) })))
        return prefs.toImmutableList()
    }

    fun labelsToHide(feed: String): List<BskyLabel> {
        return feedViewPrefs[feed]?.labelsToHide ?: contentLabelPrefs.filter { it.visibility == Visibility.HIDE }.map { BskyLabel(it.label) }
    }
}

@Immutable
@Serializable
data class BskyUser(
    val userDid: String,
    val handle: String,
    val displayName: String?,
    val avatar: String?,
    val profile: SerializableProfile,
) {
    companion object{
        fun makeUser(profile: DetailedProfile): BskyUser {
            return BskyUser(
                profile.did.did,
                profile.handle.handle,
                profile.displayName,
                profile.avatar,
                profile.toSerializableProfile(),
            )
        }
    }

    fun getProfile(): DetailedProfile {
        return profile.toProfile()
    }
}


@Serializable
public data class BskyFeedPref(
    /**
    * Hide replies in the feed.
    */
    public var hideReplies: Boolean = false,
    /**
    * Hide replies in the feed if they are not by followed users.
    */
    public var hideRepliesByUnfollowed: Boolean = false,
    /**
    * Hide replies in the feed if they do not have this number of likes.
    */
    public var hideRepliesByLikeCount: Long = 2,
    /**
    * Hide reposts in the feed.
    */
    public var hideReposts: Boolean = false,
    /**
    * Hide quote posts in the feed.
    */
    public var hideQuotePosts: Boolean = false,

    // Can be per feed, maybe add "warn" to this as well
    public var labelsToHide: List<BskyLabel> = persistentListOf(),
    public var languages: List<Language> = persistentListOf(),
    public var hidePostsByMuted: Boolean = false
)

fun GetPreferencesResponse.toPreferences(prefs: BskyPreferences) : BskyPreferences {
  preferences.map { pref:PreferencesUnion ->
    when(pref) {
      is PreferencesUnion.AdultContentPref -> prefs.adultContent = pref.value
      is PreferencesUnion.ContentLabelPref -> prefs.contentLabelPrefs.add(pref.value)
      is PreferencesUnion.FeedViewPref -> {
        if (pref.value.lab_mergeFeedEnabled != null) prefs.mergeFeeds = pref.value.lab_mergeFeedEnabled!!
        val labelsToHide = prefs.feedViewPrefs[pref.value.feed]?.labelsToHide
        val languages = prefs.feedViewPrefs[pref.value.feed]?.languages
        prefs.feedViewPrefs[pref.value.feed] = BskyFeedPref(
            hideReplies = pref.value.hideReplies == true,
            hideQuotePosts = pref.value.hideQuotePosts == true,
            hideReposts = pref.value.hideReposts == true,
            hideRepliesByUnfollowed = pref.value.hideRepliesByUnfollowed == true,
            hideRepliesByLikeCount = pref.value.hideRepliesByLikeCount?: 0,
        )
        if(!labelsToHide.isNullOrEmpty()) prefs.feedViewPrefs[pref.value.feed]?.labelsToHide = labelsToHide
        if(!languages.isNullOrEmpty()) prefs.feedViewPrefs[pref.value.feed]?.languages = languages else persistentListOf<Language>()
      }
      is PreferencesUnion.PersonalDetailsPref -> prefs.personalDetails = pref.value
      is PreferencesUnion.SavedFeedsPref -> prefs.savedFeeds = pref.value
      is PreferencesUnion.SkyFeedBuilderFeedsPref -> prefs.skyFeedBuilderFeeds = pref.value
      is PreferencesUnion.ThreadViewPref -> prefs.threadViewPrefs = pref.value
      is PreferencesUnion.HiddenPostsPref -> prefs.hiddenPosts = pref.value.items.toPersistentList()
      is PreferencesUnion.LabelersPref -> prefs.labelers = pref.value.labelers.map { it.did }.toPersistentList()
      is PreferencesUnion.InterestsPref -> {}
      is PreferencesUnion.MutedWordsPref -> prefs.mutedWords = pref.value.items.toPersistentList()
      else -> {}
    }
  }
  return prefs
}

fun GetPreferencesResponse.toPreferences() : BskyPreferences {
  val prefs = this.toPreferences(BskyPreferences())
  prefs.feedViewPrefs.map { feed ->
    prefs.contentLabelPrefs.map { label ->
      if (label.visibility == Visibility.HIDE) feed.value.labelsToHide + BskyLabel(label.label)
    }
    feed.value.languages = prefs.languages
  }
  return prefs
}

