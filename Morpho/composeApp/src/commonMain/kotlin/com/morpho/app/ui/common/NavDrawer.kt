package com.morpho.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.screens.base.tabbed.FeedsTab
import com.morpho.app.screens.base.tabbed.HomeTab
import com.morpho.app.screens.base.tabbed.MyProfileTab
import com.morpho.app.screens.base.tabbed.NotificationsTab
import com.morpho.app.screens.base.tabbed.SearchTab
import com.morpho.app.screens.base.tabbed.SettingsTab
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.util.openBrowser
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavDrawer(
    profile: DetailedProfile? = null,
    navigator: Navigator = if (LocalNavigator.current?.parent?.instanceOf(TabNavigator::class) == true) {
        LocalNavigator.currentOrThrow
    } else LocalNavigator.currentOrThrow.parent!!,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    navDrawerContent: @Composable ColumnScope.(drawerState: DrawerState, navigator: Navigator) -> Unit = {
        drawer, nav ->
        NavDrawerItems(drawerState = drawer, navigator = nav)
    },
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        gesturesEnabled = true,
        drawerState = drawerState,
        drawerContent = {
            val uriHandler = LocalUriHandler.current
            ModalDrawerSheet(
                Modifier.width(300.dp)
            ) {
                val hPad = 16.dp
                FlowRow(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    OutlinedAvatar(
                        url = profile?.avatar.orEmpty(),
                        contentDescription =
                        "Avatar for ${profile?.displayName.orEmpty()} ${profile?.handle?.handle.orEmpty()}",
                        modifier = Modifier.padding(start = hPad, top = hPad, bottom = 4.dp),
                        size = 80.dp,
                        avatarShape = AvatarShape.Rounded,
                        outlineColor = MaterialTheme.colorScheme.background,
                        onClicked = { navigator.push(MyProfileTab) }
                    )
                    Column(
                        modifier = Modifier.align(Alignment.Bottom).padding(vertical = 4.dp)
                    ) {
                        if(profile?.displayName != null) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = hPad, vertical = 0.dp),
                            )
                        }
                        Text(
                            text = "@${profile?.handle?.handle?: "Invalid Handle"}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = hPad, vertical = 0.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(horizontal = hPad)
                ) {
                    TextButton(
                        onClick = { /*TODO*/ },
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 20.dp, max = 48.dp)
                            .defaultMinSize(minWidth = 10.dp)
                    ) {
                        Text(
                            text = "${profile?.followersCount}",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = " followers",
                            fontSize = MaterialTheme.typography.labelMedium.fontSize.times(0.9),
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    TextButton(
                        onClick = { /*TODO*/ },
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 20.dp, max = 48.dp)
                            .defaultMinSize(minWidth = 10.dp)
                    ) {
                        Text(
                            text = "${profile?.followsCount}",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = " following",
                            fontSize = MaterialTheme.typography.labelMedium.fontSize.times(0.9),
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                navDrawerContent(drawerState, navigator)
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                ) {

                    TextButton(
                        onClick = {
                            openBrowser("https://github.com/morpho-app/Morpho/issues/new", uriHandler)
                        },
                        colors = ButtonDefaults.buttonColors(),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(horizontal = 8.dp),

                    ) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = "",
                            //tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .padding(end = 8.dp)
                        )
                        Text(
                            "Feedback",
                            //color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    TextButton(
                        onClick = {
                            openBrowser("https://github.com/morpho-app/Morpho", uriHandler)
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {

                        Text(
                            "Help",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    ) {
        content()
    }
}

@Composable
fun NavDrawerItem(
    tab: TabScreen,
    navigator: Navigator = LocalNavigator.currentOrThrow,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
    badge: @Composable() (() -> Unit)? = null,
) {
    val nav = if (navigator.instanceOf(TabNavigator::class)) {
        navigator.parent!!
    } else navigator
    val selected = nav.lastItem.key == tab.key
    NavigationDrawerItem(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        icon = { tab.options.icon() },
        label = { Text(tab.options.title) },
        selected = selected,
        badge = badge,
        shape = MaterialTheme.shapes.medium,
        onClick = {
            if(selected) scope.launch { drawerState.close() }
            nav.popUntil { it == tab }
            nav.push(tab)
        },
    )
}

@Composable
fun ColumnScope.NavDrawerItems(
    navigator: Navigator = LocalNavigator.currentOrThrow,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    NavDrawerItem(SearchTab, drawerState = drawerState, navigator = navigator)
    NavDrawerItem(HomeTab("home"), drawerState = drawerState, navigator = navigator)
    NavDrawerItem(NotificationsTab, drawerState = drawerState, navigator = navigator,
                    badge = {
                        val sm = LocalNavigator.currentOrThrow.koinNavigatorScreenModel<TabbedMainScreenModel>()
                        val unread by sm.unreadNotificationsCount().collectAsState(0)
                        if(unread > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    })
    NavDrawerItem(FeedsTab, drawerState = drawerState, navigator = navigator)
    NavDrawerItem(MyProfileTab, drawerState = drawerState, navigator = navigator)
    NavDrawerItem(SettingsTab, drawerState = drawerState, navigator = navigator)
}