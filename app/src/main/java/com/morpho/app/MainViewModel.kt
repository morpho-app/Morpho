package com.morpho.app

import android.app.Application
import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import app.bsky.notification.GetUnreadCountQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.morpho.app.model.BskyPreferences
import com.morpho.butterfly.Butterfly
import com.morpho.app.base.BaseViewModel
import com.morpho.app.model.AppDatabase
import com.morpho.app.model.DetailedProfile
import com.morpho.app.screens.skyline.FeedTab
import javax.inject.Inject



@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app) {
    val butterfly: Butterfly = app.butterfly
    var currentUser: DetailedProfile? = null

    var pinnedFeeds = mutableStateListOf<FeedTab>()
    var userPreferences: BskyPreferences? = null
    var windowSizeClass: WindowSizeClass? = null
    val db: AppDatabase = Room.databaseBuilder(
            app.applicationContext, AppDatabase::class.java, "morpho_db"
        ).build()

    var navBar: @Composable ((index: Int) -> Unit)? = null
    private val _unreadNotifications = MutableStateFlow(-1L)
    val unreadNotifications: StateFlow<Long>
        get() = _unreadNotifications.asStateFlow()

    init {
        updateNotifications()

    }

    private fun updateNotifications(): Job = viewModelScope.launch(SupervisorJob() + Dispatchers.Default) {
        while (true) {
            getUnreadCount()
            delay(30000L)
        }
    }

    fun getUnreadCount() = viewModelScope.launch(Dispatchers.IO) {
        butterfly.api.getUnreadCount(GetUnreadCountQuery()).onFailure {
            Log.e("Notifications", it.toString())
        }.onSuccess {
            _unreadNotifications.emit(it.count)
        }

    }

    fun initPushNotifications() = viewModelScope.launch(Dispatchers.IO) {

    }
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
