package com.morpho.app.model

import android.util.Log
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.bsky.actor.AdultContentPref
import app.bsky.actor.GetPreferencesResponse
import app.bsky.actor.HiddenPostsPref
import app.bsky.actor.MutedWord
import app.bsky.actor.MutedWordsPref
import app.bsky.actor.PersonalDetailsPref
import app.bsky.actor.PreferencesUnion
import app.bsky.actor.SavedFeedsPref
import app.bsky.actor.SkyFeedBuilderFeedsPref
import app.bsky.actor.Sort
import app.bsky.actor.ThreadViewPref
import app.bsky.actor.Visibility
import app.bsky.graph.ListViewerState
import app.bsky.graph.Token
import app.bsky.richtext.Facet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language

@Serializable
public data class BskyPreferences(
    public var personalDetails: PersonalDetailsPref? = null,
    public var adultContent: AdultContentPref? = null,
    public val feedViewPrefs: MutableMap<String, BskyFeedPref> = mutableMapOf(),
    public var skyFeedBuilderFeeds: SkyFeedBuilderFeedsPref? = null,
    public var savedFeeds: SavedFeedsPref? = null,
    public val contentLabelPrefs: MutableMap<String, Visibility> = mutableMapOf(),
    public var threadViewPrefs: ThreadViewPref? = null,
    // Get system languages and allow customization of this
    public var languages: List<Language> = persistentListOf(Language("en")),
    public var mergeFeeds: Boolean = false,
    public val mutes: MutableList<BasicProfile> = mutableListOf(),
    public val listsMuted: MutableMap<AtUri, BskyList> = mutableMapOf(),
    public var mutedWords: MutableList<MutedWord> = mutableListOf(),
    public var hiddenPosts: MutableList<AtUri> = mutableListOf(),
    public var labelers: MutableList<Did> = mutableListOf(),
) {
    suspend fun pullPrefs(api: Butterfly) = runCatching {
        val response = api.api.getPreferences().onFailure {
            Log.e("Prefs", it.toString())
        }.getOrThrow()
        this.threadViewPrefs =  response.toPreferences(this).threadViewPrefs
        this.adultContent =  response.toPreferences(this).adultContent
        this.savedFeeds =  response.toPreferences(this).savedFeeds
        this.skyFeedBuilderFeeds =  response.toPreferences(this).skyFeedBuilderFeeds
        this.personalDetails =  response.toPreferences(this).personalDetails
        this.feedViewPrefs.putAll(response.toPreferences(this).feedViewPrefs)
        this.contentLabelPrefs.putAll(response.toPreferences(this).contentLabelPrefs)
        this.mergeFeeds = response.toPreferences(this).mergeFeeds
    }
}

@Entity
data class User(
    @PrimaryKey val userDid: String,
    val handle: String,
    val displayName: String?,
    val avatar: String?,
    val description: String?,
    val banner: String?,
    val followersCount: Long = 0,
    val followsCount: Long = 0,
    val postsCount: Long = 0,

    val birthdate: String?,
    val mergeFeeds: Boolean = false,
    val adultContentEnabled: Boolean,
)

data class UserPreferences(
    @Embedded val user: User,
    @Relation(
        entity = FeedViewPreference::class,
        parentColumn = "userDid",
        entityColumn = "userDid"
    )
    val feedViewPreferences: List<FeedViewPreference>,
    @Relation(
        entity = ContentLabelPreference::class,
        parentColumn = "userDid",
        entityColumn = "userDid"
    )
    val contentLabelPreferences: List<ContentLabelPreference>,
    @Relation(
        entity = PinnedFeedPreference::class,
        parentColumn = "userDid",
        entityColumn = "userDid"
    )
    val pinnedFeeds: List<PinnedFeedPreference>,
    @Relation(
        parentColumn = "userDid",
        entityColumn = "userDid"
    )
    val threadViewPreferences: ThreadViewPreference,
    @Relation(
    parentColumn = "userDid",
    entityColumn = "userDid"
    )
    val savedFeeds: SavedFeedsPreference,
)

@Entity
data class FeedViewPreference(
    @PrimaryKey(autoGenerate = true) val feedViewPrefId: Int,
    val userDid: String,
    val feedName: String,
    val hideReplies: Boolean = false,
    val hideRepliesByUnfollowed: Boolean = false,
    val hideRepliesByLikeCount: Long = 2,
    val hideReposts: Boolean = false,
    val hideQuotePosts: Boolean = false,
    // Can be per feed, maybe add "warn" to this as well
    var labelsToHide: List<String> = persistentListOf(),
    var languages: List<String> = persistentListOf(),
    var hidePostsByMuted: Boolean = false
)

@Entity
data class ContentLabelPreference(
    @PrimaryKey(autoGenerate = true) val contentPrefId: Int,
    val userDid: String,
    val type: String,
    val visibility: Visibility,
)

@Entity
data class ThreadViewPreference(
    @PrimaryKey(autoGenerate = true) val threadViewPrefId: Int,
    val userDid: String,
    val sort: Sort,
    val prioritizeFollowedUsers: Boolean = true,
)

@Entity
data class PinnedFeedPreference(
    @PrimaryKey(autoGenerate = true) val pinnedFeedPrefId: Int,
    val userDid: String,
    val title: String,
    val uri: AtUri,
)

@Entity
data class SavedFeedsPreference(
    @PrimaryKey(autoGenerate = true) val savedFeedPrefId: Int,
    val userDid: String,
    val pinnedUris: List<String>,
    val savedUris: List<String>,
)

@Dao
interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserWithPrefs(
        user: User,
        feedPref: FeedViewPreference,

        )

}

@Serializable
data class BskyList(
    public val uri: AtUri,
    public val cid: Cid,
    public val creator: BasicProfile,
    public val name: String,
    public val purpose: Token,
    public val description: String? = null,
    public val descriptionFacets: List<Facet> = persistentListOf(),
    public val avatar: String? = null,
    public val viewer: ListViewerState? = null,
    public val indexedAt: Moment,
    public val listItems: MutableList<BasicProfile> = mutableListOf(),
)


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
      is PreferencesUnion.ContentLabelPref -> prefs.contentLabelPrefs[pref.value.label] = pref.value.visibility
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
        if(!languages.isNullOrEmpty()) prefs.feedViewPrefs[pref.value.feed]?.languages = languages
      }
      is PreferencesUnion.PersonalDetailsPref -> prefs.personalDetails = pref.value
      is PreferencesUnion.SavedFeedsPref -> prefs.savedFeeds = pref.value
      is PreferencesUnion.SkyFeedBuilderFeedsPref -> prefs.skyFeedBuilderFeeds = pref.value
      is PreferencesUnion.ThreadViewPref -> prefs.threadViewPrefs = pref.value
      is PreferencesUnion.HiddenPostsPref -> prefs.hiddenPosts = pref.value.items.toMutableList()
      is PreferencesUnion.LabelersPref -> prefs.labelers = pref.value.labelers.map { it.did }.toMutableList()
      is PreferencesUnion.InterestsPref -> {}
      is PreferencesUnion.MutedWordsPref -> prefs.mutedWords = pref.value.items.toMutableList()
      else -> {}
    }
  }
  return prefs
}

fun GetPreferencesResponse.toPreferences() : BskyPreferences {
  val prefs = this.toPreferences(BskyPreferences())
  prefs.feedViewPrefs.map { feed ->
    prefs.contentLabelPrefs.map { label ->
      if (label.value == Visibility.HIDE) feed.value.labelsToHide + BskyLabel(label.key)
    }
    feed.value.languages = prefs.languages
  }
  return prefs
}