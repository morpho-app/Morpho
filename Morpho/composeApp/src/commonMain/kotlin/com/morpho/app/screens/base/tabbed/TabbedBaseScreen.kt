package com.morpho.app.screens.base.tabbed

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.ktor.util.reflect.instanceOf
import kotlin.math.min


data class TabbedBaseScreen(
    val k: ScreenKey = ""
) : Tab {

    override val key: ScreenKey =  hashCode().toString() + "BaseTabbedScreen" + k

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Navigator(
            HomeTab("startHome"),
        ) { navigator ->
            LaunchedEffect(Unit) { navigator.replaceAll(HomeTab("startHome2")) }
            CurrentScreen()
        }

    }

    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 0u,
            title = "Morpho",
        )

}

@Composable
fun TabNavigationItem(
    tab: TabScreen,
    navigator: Navigator = LocalNavigator.currentOrThrow
) {
    val nav = if (navigator.instanceOf(TabNavigator::class)) {
        navigator.parent!!
    } else navigator

    val selected = remember { nav.lastItem.key == tab.key }
    val newIndex = tab.options.index

    Tab(
        selected = selected,
        onClick = {
            when {
                nav.lastItem.key == tab.key -> return@Tab
                newIndex == 0 -> nav.replaceAll(tab)
                else -> nav.push(tab)
            }
        },
        icon = { tab.options.icon() },
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedNavBar(
    selectedTab: Int = 0,
    navigator: Navigator = LocalNavigator.currentOrThrow,
) {
    PrimaryTabRow(
        selectedTabIndex = min(selectedTab, 4),
        modifier = Modifier.clip(
            MaterialTheme.shapes.medium.copy(
                bottomEnd = CornerSize(0.dp),
                bottomStart = CornerSize(0.dp),
                topStart = CornerSize(0.dp),
            )
        ),
        indicator = {
            if (selectedTab <= 4) {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        selectedTab,
                        matchContentSize = true
                    ),
                    width = Dp.Unspecified,
                )
            }
        }
    ) {
        TabNavigationItem(HomeTab("bottomNavHome"))
        TabNavigationItem(SearchTab)
        TabNavigationItem(FeedsTab)
        TabNavigationItem(NotificationsTab)
        TabNavigationItem(MyProfileTab)
    }
}