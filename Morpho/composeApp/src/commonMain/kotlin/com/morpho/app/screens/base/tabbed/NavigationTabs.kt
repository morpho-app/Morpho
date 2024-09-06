package com.morpho.app.screens.base.tabbed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.main.tabbed.TabbedHomeView
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.screens.notifications.NotificationViewContent
import com.morpho.app.screens.notifications.TabbedNotificationScreenModel
import com.morpho.app.screens.profile.TabbedProfileContent
import com.morpho.app.screens.profile.TabbedProfileViewModel
import com.morpho.app.screens.thread.ThreadTopBar
import com.morpho.app.screens.thread.ThreadViewContent
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class TabScreenOptions(
    val index: Int,
    val icon: @Composable () -> Unit,
    val title: String,
)

interface TabScreen: Screen {

    val navBar: @Composable (Navigator) -> Unit

    @Composable
    override fun Content()

    val options: TabScreenOptions
        @Composable get

}

data class HomeTab(
    val k: ScreenKey = "HomeTab"
): TabScreen {
    override val navBar: @Composable (Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }

    override val key: ScreenKey  = "${k}_${hashCode()}"

    @Composable
    override fun Content() {
        TabbedHomeView()
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

data object SearchTab: TabScreen {

    override val key: ScreenKey = "searchTab"

    override val navBar: @Composable (Navigator) -> Unit = { n ->
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

data object FeedsTab: TabScreen {

    override val key: ScreenKey = "feedsTab"

    override val navBar: @Composable (Navigator) -> Unit = { n ->
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

data object NotificationsTab: TabScreen {

    override val key: ScreenKey = "notificationsTab"


    override val navBar: @Composable (Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        NotificationViewContent(
            navigator,
            navigator.getNavigatorScreenModel<TabbedNotificationScreenModel>()
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

data class ProfileTab(
    val id: AtIdentifier,
    ): TabScreen {

    override val key: ScreenKey = "profileTab_${id}_${hashCode()}"

    override val navBar: @Composable (Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { TabbedProfileViewModel(id) }
        val ownProfile = remember { screenModel.api.atpUser?.id == id }
        TabbedProfileContent(ownProfile, screenModel)

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

data class ThreadTab(
    val uri: AtUri,
): TabScreen {

        override val key: ScreenKey = "threadTab_${uri}_${hashCode()}"

        override val navBar: @Composable (Navigator) -> Unit = { n ->
            TabbedNavBar(options.index, n)
        }
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            val sm = navigator.getNavigatorScreenModel<TabbedMainScreenModel>()
            var threadState: StateFlow<ContentCardState.PostThread>? by remember { mutableStateOf(null)}
            LifecycleEffect(
                onStarted = {
                    sm.screenModelScope.launch { threadState = sm.loadThread(uri) }
                }
            )
            if(threadState != null) {
                ThreadViewContent(threadState!!, navigator, sm)
            } else {
                TabbedScreenScaffold(
                navBar = { navBar(navigator) },
                topContent = { ThreadTopBar(navigator = navigator) },
                content = { _ -> LoadingCircle() }
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


data object MyProfileTab: TabScreen {

    override val key: ScreenKey = "myProfileTab"


    override val navBar: @Composable (Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        TabbedProfileContent(true)
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