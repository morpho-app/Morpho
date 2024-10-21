package com.morpho.app.screens.base.tabbed

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.ui.common.SlideTabTransition
import com.morpho.app.ui.theme.roundedTopR
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import io.ktor.util.reflect.instanceOf
import kotlinx.serialization.Serializable
import kotlin.math.min

@Parcelize
@Serializable
data object TabbedBaseScreen: Tab, Parcelable {

    override val key: ScreenKey = "TabbedBaseScreen_${hashCode()}"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        Navigator(
            HomeTab("startHome"),
            disposeBehavior = NavigatorDisposeBehavior(
                disposeNestedNavigators = false
            )
        ) { navigator ->
            SlideTabTransition(navigator)
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
                nav.items.contains(tab) -> nav.popUntil { it == tab }
                else -> nav.push(tab)
            }
        },
        icon = {
            when (tab) {
                is NotificationsTab -> {
                    val sm = navigator.koinNavigatorScreenModel<TabbedMainScreenModel>()
                    val unread by sm.unreadNotificationsCount.collectAsState(0)
                    BadgedBox(
                        badge = {
                            if (unread > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ) {
                                    Text(unread.toString(),
                                         modifier = Modifier.semantics {
                                             contentDescription = "$unread new notifications"
                                         }
                                    )
                                }
                            }
                        }
                    ) {
                        tab.options.icon()
                    }
                }

                is HomeTab -> {
                    BadgedBox(
                        badge = {
                            if (false) { /// TODO: put this back in later
                                Badge(
                                    modifier = Modifier.size(4.dp),
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    ) {
                        tab.options.icon()
                    }

                }

                else -> { tab.options.icon() }
            }
        },
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
            roundedTopR.medium
        ).systemBarsPadding(),
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
        },
        divider = {}
    ) {
        TabNavigationItem(HomeTab("bottomNavHome"), navigator)
        TabNavigationItem(SearchTab, navigator)
        TabNavigationItem(FeedsTab, navigator)
        TabNavigationItem(NotificationsTab, navigator)
        TabNavigationItem(MyProfileTab, navigator)
    }
}