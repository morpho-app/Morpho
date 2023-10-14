package radiant.nimbus.ui.common

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.components.NavBarLocation
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.appCurrentDestinationAsState
import radiant.nimbus.screens.destinations.BottomSheetScreenDestination
import radiant.nimbus.screens.destinations.Destination
import radiant.nimbus.screens.destinations.FeedDiscoveryScreenDestination
import radiant.nimbus.screens.destinations.FeedListScreenDestination
import radiant.nimbus.screens.destinations.LoginScreenDestination
import radiant.nimbus.screens.destinations.MyProfileScreenDestination
import radiant.nimbus.screens.destinations.NotificationsScreenDestination
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.screens.destinations.SearchScreenScreenDestination
import radiant.nimbus.screens.destinations.SettingsScreenDestination
import radiant.nimbus.screens.destinations.SkylineScreenDestination
import radiant.nimbus.screens.destinations.TemplateScreenDestination
import radiant.nimbus.screens.startAppDestination


@Composable
fun NimbusNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    location: NavBarLocation = NavBarLocation.BottomFull,
    profilePic: (@Composable (Boolean, () -> Unit) -> Unit)? = null,
    selected: Int = 0,
    actor: AtIdentifier? = null,
) {
    if (location == NavBarLocation.BottomFull || location == NavBarLocation.BottomPartial) {
        NimbusBottomNavBar(
            navController = navController,
            modifier = modifier,
            selected,
            profilePic
        )
    } else {
        NimbusNavRail(
            navController = navController,
            modifier = modifier,
            profilePic
        )
    }
}

@Composable
fun NimbusBottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    selected: Int,
    profilePic: @Composable() ((Boolean, () -> Unit) -> Unit)? = null,
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
            selectedTabIndex = selectedTab,
            modifier = Modifier.clip(
                MaterialTheme.shapes.medium.copy(
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                )
            )
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
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
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Notifications Button"
                    )
                },
                unselectedContentColor = unselectedColor,
            )
            Tab(
                selected = selectedTab == 4,
                onClick = {
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

@Composable
fun NimbusNavRail(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    profilePic: @Composable() ((Boolean,() -> Unit) -> Unit)? = null,
    ) {
    val currentDestination: Destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination
    val isCurrentDestOnBackStack = navController.isRouteOnBackStack(currentDestination)

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
                Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = "Notifications Button")
            },
            //label = {Text("Notifications")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(
            selected = currentDestination == MyProfileScreenDestination,
            onClick = {
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