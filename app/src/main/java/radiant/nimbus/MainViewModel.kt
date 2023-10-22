package radiant.nimbus

import android.app.Application
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.room.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.BskyPreferences
import radiant.nimbus.api.ServerRepository
import radiant.nimbus.api.auth.LoginRepository
import radiant.nimbus.app.Supervisor
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.AppDatabase
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.screens.skyline.FeedTab
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    val apiProvider: ApiProvider = ApiProvider(ServerRepository(app), LoginRepository(app)),
) : BaseViewModel(app) {
    var supervisors: Set<Supervisor> = setOf()
    var currentUser: DetailedProfile? = null
    var pinnedFeeds = mutableStateListOf<FeedTab>()
    var userPreferences: BskyPreferences? = null
    var windowSizeClass: WindowSizeClass? = null
    val db: AppDatabase = Room.databaseBuilder(app.applicationContext, AppDatabase::class.java, "nimbus_db")
        .build()

    var navBar: @Composable ((index: Int) -> Unit)? = null

    /*
    private val currentUserPref = app.storage.preference<DetailedProfile>("current_user", null)
    var currentUser by mutableStateOf(currentUserPref)
    fun currentUser(): Flow<DetailedProfile?> = currentUserPref.updates.distinctUntilChanged()

    private val followsPref = app.storage.preference<List<BasicProfile>>("user_follows", listOf())
    val follows by mutableStateOf(followsPref)
    fun follows(): Flow<List<BasicProfile>> = followsPref.updates.filterNotNull().distinctUntilChanged()

    private val pinnedFeedsPref = app.storage.preference<List<FeedTab>>("user_pinned_feeds", listOf())
    var pinnedFeeds by mutableStateOf(pinnedFeedsPref)
    fun pinnedFeeds(): Flow<List<FeedTab>> = pinnedFeedsPref.updates.filterNotNull().distinctUntilChanged()

    private val userPreferencesStore = app.storage.preference<BskyPreferences>("user_pinned_feeds", null)
    var userPreferences by mutableStateOf(userPreferencesStore)
    fun userPreferences(): Flow<BskyPreferences?> = userPreferencesStore.updates.distinctUntilChanged()

     */
}
