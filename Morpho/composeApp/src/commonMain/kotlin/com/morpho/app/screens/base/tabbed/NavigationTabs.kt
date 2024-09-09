package com.morpho.app.screens.base.tabbed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.jetpack.navigatorViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.main.tabbed.TabbedHomeView
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.screens.notifications.NotificationViewContent
import com.morpho.app.screens.profile.TabbedProfileContent
import com.morpho.app.screens.thread.ThreadTopBar
import com.morpho.app.screens.thread.ThreadViewContent
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.app.util.JavaSerializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Immutable
@Serializable
data class TabScreenOptions(
    val index: Int,
    val icon: @Composable () -> Unit,
    val title: String,
)


interface TabScreen: Screen, JavaSerializable {

    val navBar: @Composable (Navigator) -> Unit

    @Composable
    override fun Content()

    val options: TabScreenOptions
        @Composable get

}

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
        val sm = navigatorViewModel { TabbedMainScreenModel() }
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

data class ProfileTab(
    val id: AtIdentifier,
): TabScreen {

    override val key: ScreenKey
        get() = "profileTab_${id}_${uniqueScreenKey}"

    @kotlin.jvm.Transient @Transient override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        TabbedProfileContent(id)
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
        val sm = navigatorViewModel { TabbedMainScreenModel() }
        var threadState: StateFlow<ContentCardState.PostThread>? by remember { mutableStateOf(null)}
        LifecycleEffectOnce {
            sm.viewModelScope.launch { threadState = sm.loadThread(uri) }
        }
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

@Immutable
@Serializable
data object MyProfileTab: TabScreen {

    override val key: ScreenKey
        get() = "myProfileTab${uniqueScreenKey}"


    override val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(options.index, n)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        TabbedProfileContent()
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