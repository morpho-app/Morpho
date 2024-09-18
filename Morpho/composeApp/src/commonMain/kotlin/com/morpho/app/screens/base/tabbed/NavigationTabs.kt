package com.morpho.app.screens.base.tabbed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.model.uidata.MyProfilePresenter
import com.morpho.app.screens.main.tabbed.TabbedHomeView
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.screens.notifications.NotificationViewContent
import com.morpho.app.screens.profile.TabbedProfileContent
import com.morpho.app.screens.thread.ThreadTopBar
import com.morpho.app.screens.thread.ThreadViewContent
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Parcelize
@Immutable
@Serializable
data class TabScreenOptions(
    val index: Int,
    val icon: @Composable () -> Unit,
    val title: String,
): Parcelable


interface TabScreen: Screen, Parcelable {

    val navBar: @Composable (Navigator) -> Unit

    @Composable
    override fun Content()

    val options: TabScreenOptions
        @Composable get

}

@Parcelize
@Immutable
@Serializable
data class HomeTab(
    val k: ScreenKey = "HomeTab"
): TabScreen {
    @kotlin.jvm.Transient @Transient override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }

    override val key: ScreenKey
        get() = "${k}_${hashCode()}${uniqueScreenKey}"

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val sm = LocalNavigator.currentOrThrow.rememberNavigatorScreenModel { TabbedMainScreenModel() }
        TabbedHomeView(sm)
    }

    override val options: TabScreenOptions
    @Composable get() {
        return TabScreenOptions(
            index = 0,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.onBackground) },
            title = "Home"
        )
    }


}

@Parcelize
@Immutable
@Serializable
data object SearchTab: TabScreen {

    override val key: ScreenKey
        get() = "searchTab${uniqueScreenKey}"

    @kotlin.jvm.Transient @Transient override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @Composable
    override fun Content() {
        LoadingCircle()
    }

    override val options: TabScreenOptions
    @Composable get() {
            return TabScreenOptions(
                index = 1,
                icon = { Icon(Icons.Default.Search, contentDescription = "Search",
                              tint = MaterialTheme.colorScheme.onBackground) },
                title = "Search"
            )
        }


}

@Parcelize
@Immutable
@Serializable
data object FeedsTab: TabScreen {

    override val key: ScreenKey
        get() = "feedsTab${uniqueScreenKey}"

    @kotlin.jvm.Transient @Transient override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @Composable
    override fun Content() {
        LoadingCircle()
    }

    override val options: TabScreenOptions
        @Composable get() {
            return TabScreenOptions(
                index = 2,
                icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "Feeds",
                              tint = MaterialTheme.colorScheme.onBackground) },
                title = "Feeds"
            )
        }

}

@Parcelize
@Immutable
@Serializable
data object NotificationsTab: TabScreen {

    override val key: ScreenKey
        get() = "notificationsTab${uniqueScreenKey}"


    @kotlin.jvm.Transient @Transient override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        NotificationViewContent(
            navigator,
        )
    }

    override val options: TabScreenOptions
    @Composable get() {
        return TabScreenOptions(
            index = 3,
            icon = { Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications",
                          tint = MaterialTheme.colorScheme.onBackground) },
            title = "Notifications"
        )
    }


}

@Parcelize
@Serializable
@Immutable
data class ProfileTab(
    val id: Did,
): TabScreen {

    override val key: ScreenKey
        get() = "profileTab_${id}_${uniqueScreenKey}"

    @kotlin.jvm.Transient @Transient override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val sm = LocalNavigator.currentOrThrow.rememberNavigatorScreenModel { TabbedMainScreenModel() }
        val eventStream = sm.globalEvents
        val profilePresenter by  sm.getProfilePresenter(id).collectAsState(null)
        val myProfilePresenter by  sm.getMyProfilePresenter().collectAsState(null)
        if(profilePresenter != null && myProfilePresenter != null) {
            val presenter = profilePresenter!!.first
            val updates = profilePresenter!!.second

            val myProfileState = myProfilePresenter!!.first.profileState
            TabbedProfileContent(
                profileState = presenter.profileState,
                myProfileState = myProfileState,
                eventCallback = { eventStream.tryEmit(it) }
            )
        }
    }


    override val options: TabScreenOptions
        @Composable get() {
            return TabScreenOptions(
                index = 5,
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile",
                              tint = MaterialTheme.colorScheme.onBackground) },
                title = "Profile"
            )
        }


}

@Parcelize
@Immutable
@Serializable
data class ThreadTab(
    val uri: AtUri,
): TabScreen {

    override val key: ScreenKey
        get() = "threadTab_${uri}_$uniqueScreenKey}"

    @kotlin.jvm.Transient @Transient override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val sm = navigator.rememberNavigatorScreenModel { TabbedMainScreenModel() }
        val threadState by sm.getThread(uri).collectAsState(null)
        if(threadState != null) {
            ThreadViewContent(threadState!!, navigator)
        } else {
            TabbedScreenScaffold(
                navBar = { navBar(navigator) },
                topContent = { ThreadTopBar(navigator = navigator) },
                content = { _, _ -> LoadingCircle() },
                state = threadState,
                modifier = Modifier
            )
        }
    }

    override val options: TabScreenOptions
        @Composable get() {
            return TabScreenOptions(
                index = 6,
                icon = { Icon(Icons.Default.NotificationsNone, contentDescription = "Thread",
                              tint = MaterialTheme.colorScheme.onBackground) },
                title = "Thread"
            )
        }
}

@Parcelize
@Immutable
@Serializable
data object MyProfileTab: TabScreen {

    override val key: ScreenKey
        get() = "myProfileTab${uniqueScreenKey}"


    override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val sm = LocalNavigator.currentOrThrow.rememberNavigatorScreenModel { TabbedMainScreenModel() }
        val eventStream = sm.globalEvents
        var myProfilePresenter by remember { mutableStateOf<MyProfilePresenter?>(null) }
        LifecycleEffectOnce {
            sm.screenModelScope.launch {
                sm.getMyProfilePresenter().first().also { it -> myProfilePresenter = it.first }
            }
        }
        if(myProfilePresenter != null) {

            val myProfileState = myProfilePresenter!!.profileState
            TabbedProfileContent(
                ownProfile = true,
                profileState = null,
                myProfileState = myProfileState,
                eventCallback = { eventStream.tryEmit(it) }
            )
        }
    }


    override val options: TabScreenOptions
        @Composable get() {
            return TabScreenOptions(
                index = 4,
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile",
                              tint = MaterialTheme.colorScheme.onBackground) },
                title = "Profile"
            )
        }


}