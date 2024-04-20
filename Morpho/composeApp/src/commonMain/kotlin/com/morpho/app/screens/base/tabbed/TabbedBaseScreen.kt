package com.morpho.app.screens.base.tabbed

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.util.reflect.instanceOf
import kotlin.math.min


class TabbedBaseScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Navigator(
            screen = HomeTab(
                navBar = { TabbedNavBar() }
            ),
        ) { navigator ->
            CurrentScreen()
        }

    }

    @Composable
    private fun TabNavigationItem(tab: TabScreen) {
        val navigator = LocalNavigator.currentOrThrow
        val newIndex = tab.options.index
        val selected =
            if (navigator.lastItem.instanceOf(TabScreen::class) && navigator.lastItem != tab) {
            val selectedTab = navigator.lastItem as TabScreen
            selectedTab.options.index == tab.options.index
        } else false

        Tab(
            selected = selected,
            onClick = {
                when {
                    newIndex == 0 -> navigator.replaceAll(tab)
                    else -> navigator.push(tab)
                }
                      },
            icon = { tab.options.icon() },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabbedNavBar(
        selectedTab: Int = 0,
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
            TabNavigationItem(HomeTab{ TabbedNavBar(it) })
            TabNavigationItem(SearchTab { TabbedNavBar(it) })
            TabNavigationItem(FeedsTab { TabbedNavBar(it) })
            TabNavigationItem(NotificationsTab { TabbedNavBar(it) })
            TabNavigationItem(MyProfileTab { TabbedNavBar(it) })
        }
    }

}

