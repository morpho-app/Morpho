package com.morpho.app.screens.base.tabbed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.screens.main.tabbed.TabbedHomeView
import com.morpho.app.screens.profile.TabbedProfileContent
import com.morpho.app.screens.profile.TabbedProfileViewModel
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri


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

    override val key: ScreenKey  = (hashCode().toString() + k)

    @Composable
    override fun Content() {
        TabbedHomeView()
    }

    override val options: TabScreenOptions
    @Composable get() {
        return TabScreenOptions(
            index = 0,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            title = "Home"
        )
    }


}

data object SearchTab: TabScreen {

    override val key: ScreenKey
        get() = "searchTab"

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
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                title = "Search"
            )
        }


}

data object FeedsTab: TabScreen {

    override val key: ScreenKey
        get() = "feedsTab"

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
                icon = { Icon(Icons.Default.NotificationsNone, contentDescription = "Feeds") },
                title = "Feeds"
            )
        }

}

data object NotificationsTab: TabScreen {

    override val key: ScreenKey
        get() = "notificationsTab"

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
            index = 3,
            icon = { Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications") },
            title = "Notifications"
        )
    }


}

data class ProfileTab(
    val id: AtIdentifier,
    ): TabScreen {

    override val key: ScreenKey
        get() = "profileTab$id"

    override val navBar: @Composable (Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.getNavigatorScreenModel<TabbedProfileViewModel>()
        LifecycleEffect(
            onStarted = {
                screenModel.initProfile()
            },
            onDisposed = {},
        )
        val ownProfile = rememberSaveable { screenModel.api.id == id }
        TabbedProfileContent(screenModel, ownProfile)

    }


    override val options: TabScreenOptions
        @Composable get() {
            return TabScreenOptions(
                index = 5,
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                title = "Profile"
            )
        }


}

data class ThreadTab(
    val uri: AtUri,
): TabScreen {

        override val key: ScreenKey
            get() = "threadTab"

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
                    index = 6,
                    icon = { Icon(Icons.Default.NotificationsNone, contentDescription = "Thread") },
                    title = "Thread"
                )
            }
}


data object MyProfileTab: TabScreen {

    override val key: ScreenKey
        get() = "myProfileTab"

    override val navBar: @Composable (Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.getNavigatorScreenModel<TabbedProfileViewModel>()
        LifecycleEffect(
            onStarted = {
                screenModel.initProfile()
            },
            onDisposed = {},
        )
        TabbedProfileContent(screenModel, true)
    }


    override val options: TabScreenOptions
        @Composable get() {
            return TabScreenOptions(
                index = 4,
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                title = "Profile"
            )
        }


}