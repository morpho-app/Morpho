package com.morpho.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.morpho.app.MainViewModel
import com.morpho.app.components.NavBarLocation
import com.morpho.app.screens.NavGraphs
import com.morpho.app.screens.appCurrentDestinationAsState
import com.morpho.app.screens.destinations.*
import com.morpho.app.screens.startAppDestination
import kotlin.math.min


@Composable
fun MorphoNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    location: NavBarLocation = NavBarLocation.BottomFull,
    profilePic: (@Composable (Boolean, () -> Unit) -> Unit)? = null,
    selected: Int = 0,
    viewModel: MainViewModel,
) {
    if (location == NavBarLocation.BottomFull || location == NavBarLocation.BottomPartial) {
        MorphoBottomNavBar(
            navController = navController,
            modifier = modifier,
            selected,
            profilePic,
            viewModel
        )
    } else {
        MorphoNavRail(
            navController = navController,
            modifier = modifier,
            profilePic,
            viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphoBottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    selected: Int,
    profilePic: @Composable() ((Boolean, () -> Unit) -> Unit)? = null,
    viewModel: MainViewModel,
    ) {
    val currentDestination: Destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination
    var selectedTab by rememberSaveable { mutableIntStateOf(selected) }
    val unselectedColor = MaterialTheme.colorScheme.outline
    selectedTab = when(currentDestination) {
        BottomSheetScreenDestination -> 0
        FeedDiscoveryScreenDestination -> 0
        FeedListScreenDestination -> 2
        LoginScreenDestination -> 0
        MyProfileScreenDestination -> 4
        NotificationsScreenDestination -> 3
        SettingsScreenDestination -> 0
        SkylineScreenDestination -> 0
        SearchScreenScreenDestination -> 0
        TemplateScreenDestination -> 0
        PostThreadScreenDestination -> 5
        ProfileScreenDestination -> 5
        else -> {5}
    }
    val unread = viewModel.unreadNotifications.collectAsStateWithLifecycle(initialValue = -1)
    LaunchedEffect(Unit) {
        viewModel.getUnreadCount()
    }

    Column(
        Modifier.background(
            TabRowDefaults.primaryContainerColor,
            MaterialTheme.shapes.medium.copy(
                bottomEnd = CornerSize(0.dp),
                bottomStart = CornerSize(0.dp),
                topStart = CornerSize(0.dp),
            )
        )
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
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    viewModel.getUnreadCount()
                    selectedTab = 0
                    if (navController.isRouteOnBackStack(SkylineScreenDestination)) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navController.popBackStack(SkylineScreenDestination, false)
                    } else {
                        navController.navigate(SkylineScreenDestination()) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }

                },
                icon = {
                    Icon(imageVector = Icons.Filled.Home, contentDescription = "Home Button")
                },
                unselectedContentColor = unselectedColor,
            )
            Tab(
                selected = selectedTab == 1,//currentDestination == SkylineScreenDestination,
                onClick = {
                    viewModel.getUnreadCount()
                    selectedTab = 1
                    if (navController.isRouteOnBackStack(SearchScreenScreenDestination)) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navController.popBackStack(SearchScreenScreenDestination, false)
                    } else {
                        navController.navigate(SearchScreenScreenDestination) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Button")
                },
                unselectedContentColor = unselectedColor,
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    viewModel.getUnreadCount()
                    selectedTab = 2
                    if (navController.isRouteOnBackStack(FeedListScreenDestination)) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navController.popBackStack(FeedListScreenDestination, false)
                    } else {
                        navController.navigate(FeedListScreenDestination) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.DynamicFeed,
                        contentDescription = "Feeds Button"
                    )
                },
                unselectedContentColor = unselectedColor,
            )
            Tab(
                selected = selectedTab == 3,
                onClick = {
                    viewModel.getUnreadCount()
                    selectedTab = 3
                    if (navController.isRouteOnBackStack(NotificationsScreenDestination)) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navController.popBackStack(NotificationsScreenDestination, false)
                    } else {
                        navController.navigate(NotificationsScreenDestination) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (unread.value > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(unread.value.toString(),
                                        modifier = Modifier.semantics {
                                            contentDescription = "$unread.value.toString() new notifications"
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = "Notifications Button"
                        )
                    }

                },
                unselectedContentColor = unselectedColor,
            )
            Tab(
                selected = selectedTab == 4,
                onClick = {
                    viewModel.getUnreadCount()
                    selectedTab = 4
                    if (navController.isRouteOnBackStack(MyProfileScreenDestination)) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navController.popBackStack(MyProfileScreenDestination, false)
                    } else {
                        navController.navigate(MyProfileScreenDestination) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (profilePic != null) {
                        profilePic.invoke(selectedTab == 4) {
                            if (navController.isRouteOnBackStack(MyProfileScreenDestination)) {
                                // When we click again on a bottom bar item and it was already selected
                                // we want to pop the back stack until the initial destination of this bottom bar item
                                navController.popBackStack(MyProfileScreenDestination, false)
                            } else {
                                navController.navigate(MyProfileScreenDestination) {
                                    popUpTo(NavGraphs.root) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile Button"
                        )
                    }
                },
                unselectedContentColor = unselectedColor,
            )
        }
        Spacer(
            Modifier.windowInsetsBottomHeight(
                WindowInsets.systemBars
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphoNavRail(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    profilePic: @Composable() ((Boolean,() -> Unit) -> Unit)? = null,
    viewModel: MainViewModel,
    ) {
    val currentDestination: Destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination
    val isCurrentDestOnBackStack = navController.isRouteOnBackStack(currentDestination)
    val unread = viewModel.unreadNotifications.collectAsStateWithLifecycle(initialValue = -1)

    LaunchedEffect(Unit) {
        viewModel.getUnreadCount()
    }

    NavigationRail(
        header = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    ) {

        Spacer(modifier = Modifier
            .height(20.dp)
            .weight(0.2F),)
        NavigationRailItem(selected = currentDestination == SkylineScreenDestination,
            onClick = {
                viewModel.getUnreadCount()
                if (navController.isRouteOnBackStack(SkylineScreenDestination)) {
                    // When we click again on a bottom bar item and it was already selected
                    // we want to pop the back stack until the initial destination of this bottom bar item
                    navController.popBackStack(SkylineScreenDestination, false)
                } else {
                    navController.navigate(SkylineScreenDestination()) {
                    popUpTo(NavGraphs.root) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            } },
            icon = {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home Button")
            },
            //label = {Text("Home")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(selected = false, //currentDestination == SkylineScreenDestination.invoke(),
            onClick = {
                viewModel.getUnreadCount()
                if (navController.isRouteOnBackStack(SkylineScreenDestination)) {
                    // When we click again on a bottom bar item and it was already selected
                    // we want to pop the back stack until the initial destination of this bottom bar item
                    navController.popBackStack(SkylineScreenDestination, false)
                } else {navController.navigate(SkylineScreenDestination()) {
                    popUpTo(NavGraphs.root) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }  },
            icon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Button")
            },
            //label = {Text("Search")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(selected = currentDestination == FeedListScreenDestination,
            onClick = {
                viewModel.getUnreadCount()
                if (navController.isRouteOnBackStack(FeedListScreenDestination)) {
                    // When we click again on a bottom bar item and it was already selected
                    // we want to pop the back stack until the initial destination of this bottom bar item
                    navController.popBackStack(FeedListScreenDestination, false)
                } else {
                    navController.navigate(FeedListScreenDestination) {
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }  },
            icon = {
                Icon(imageVector = Icons.Outlined.DynamicFeed, contentDescription = "Feeds Button")
            },
            //label = {Text("Feeds")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(selected = currentDestination == NotificationsScreenDestination,
            onClick = {
                viewModel.getUnreadCount()
                if (navController.isRouteOnBackStack(NotificationsScreenDestination)) {
                    // When we click again on a bottom bar item and it was already selected
                    // we want to pop the back stack until the initial destination of this bottom bar item
                    navController.popBackStack(NotificationsScreenDestination, false)
                } else {
                    navController.navigate(NotificationsScreenDestination) {
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }  },
            icon = {
                BadgedBox(
                    badge = {
                        if (unread.value > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(unread.value.toString(),
                                    modifier = Modifier.semantics {
                                        contentDescription = "$unread.value.toString() new notifications"
                                    }
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Notifications Button"
                    )
                }
            },
            //label = {Text("Notifications")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(
            selected = currentDestination == MyProfileScreenDestination,
            onClick = {
                viewModel.getUnreadCount()
                if (navController.isRouteOnBackStack(MyProfileScreenDestination)) {
                    // When we click again on a bottom bar item and it was already selected
                    // we want to pop the back stack until the initial destination of this bottom bar item
                    navController.popBackStack(MyProfileScreenDestination, false)
                } else {
                    navController.navigate(MyProfileScreenDestination) {
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }  },
            icon = {
                if (profilePic != null) {
                    profilePic.invoke(currentDestination == MyProfileScreenDestination) {
                        if (navController.isRouteOnBackStack(MyProfileScreenDestination)) {
                            // When we click again on a bottom bar item and it was already selected
                            // we want to pop the back stack until the initial destination of this bottom bar item
                            navController.popBackStack(MyProfileScreenDestination, false)
                        } else {
                            navController.navigate(MyProfileScreenDestination) {
                                popUpTo(NavGraphs.root) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile Button",
                    )
                }
            },
            //label = { Text("Profile") },
            alwaysShowLabel = false,
        )
        Spacer(modifier = Modifier
            .height(20.dp)
            .weight(0.1F),)
        FloatingActionButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Post")
        }
        Spacer(modifier = Modifier
            .height(20.dp)
            .weight(0.1F),)
    }

}