package com.morpho.app.screens.base.tabbed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.screens.profile.TabbedProfileContent
import com.morpho.app.screens.profile.TabbedProfileTopBar
import com.morpho.app.screens.profile.TabbedProfileViewModel
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.butterfly.AtIdentifier
import kotlinx.coroutines.launch


data class TabScreenOptions(
    val index: Int,
    val icon: @Composable () -> Unit,
    val title: String,
)

interface TabScreen: Screen {

    val navBar: @Composable (Int) -> Unit

    @Composable
    override fun Content()

    val options: TabScreenOptions
        @Composable get

}

data class HomeTab(
    override val navBar: @Composable (Int) -> Unit = {},
): TabScreen {

    @Composable
    override fun Content() {

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

data class SearchTab(
    override val navBar: @Composable (Int) -> Unit = {},
): TabScreen {

    @Composable
    override fun Content() {
        TODO("Not yet implemented")
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

data class FeedsTab(
    override val navBar: @Composable (Int) -> Unit = {},
): TabScreen {

    @Composable
    override fun Content() {
        TODO("Not yet implemented")
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

data class NotificationsTab(
    override val navBar: @Composable (Int) -> Unit = {},
): TabScreen {

    @Composable
    override fun Content() {
        TODO("Not yet implemented")
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
    override val navBar: @Composable (Int) -> Unit = {},
): TabScreen {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.getNavigatorScreenModel<TabbedProfileViewModel>()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = rememberTopAppBarState(),
            snapAnimationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = Spring.DampingRatioNoBouncy
            ),
            //flingAnimationSpec = exponentialDecay()
        )
        val ownProfile = screenModel.api.id == id
        if (screenModel.profileState?.profile != null) {
            TabbedScreenScaffold(
                navBar = { navBar(options.index) },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topContent = {
                    TabbedProfileTopBar(screenModel.profileState?.profile, ownProfile, scrollBehavior,
                                        switchTab = { screenModel.screenModelScope.launch { screenModel.switchTab(it) } }
                    )
                },
                content = { insets -> TabbedProfileContent(screenModel, ownProfile, insets) }
            )
        }

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


data class MyProfileTab(
    override val navBar: @Composable (Int) -> Unit = {},
): TabScreen {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.getNavigatorScreenModel<TabbedProfileViewModel>()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = rememberTopAppBarState(),
            snapAnimationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = Spring.DampingRatioNoBouncy
            ),
            //flingAnimationSpec = exponentialDecay()
        )
        if (screenModel.profileState?.profile != null) {
            TabbedScreenScaffold(
                navBar = { navBar(options.index) },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topContent = {
                    TabbedProfileTopBar(screenModel.profileState?.profile, true, scrollBehavior,
                                        switchTab = {
                                            screenModel.screenModelScope.launch { screenModel.switchTab(it) }
                                        }
                    )
                },
                content = { insets -> TabbedProfileContent(screenModel, true, insets) }
            )
        }
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