package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.components.NavBarLocation
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.appCurrentDestinationAsState
import radiant.nimbus.screens.destinations.Destination
import radiant.nimbus.screens.destinations.FeedListScreenDestination
import radiant.nimbus.screens.destinations.MyProfileScreenDestination
import radiant.nimbus.screens.destinations.NotificationsScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.screens.destinations.SkylineScreenDestination
import radiant.nimbus.screens.startAppDestination


@Composable
fun NimbusNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    location: NavBarLocation = NavBarLocation.BottomFull,
    profilePic: (@Composable (() -> Unit) -> Unit)? = null,
    actor: AtIdentifier? = null,
) {
    if (location == NavBarLocation.BottomFull || location == NavBarLocation.BottomPartial) {
        NimbusBottomNavBar(
            navController = navController,
            modifier = modifier,
            profilePic, actor
        )
    } else {
        NimbusNavRail(
            navController = navController,
            modifier = modifier,
            profilePic, actor
        )
    }
}

@Composable
fun NimbusBottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    profilePic: @Composable() ((() -> Unit) -> Unit)? = null,
    actor: AtIdentifier? = null,

    ) {
    val currentDestination: Destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination

    NavigationBar(
        
    ) {
        NavigationBarItem(selected = currentDestination == SkylineScreenDestination,
            onClick = { navController.navigate(SkylineScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            } },
            icon = {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home Button")
            },
            //label = {Text("Home")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(selected = false,//currentDestination == SkylineScreenDestination,
            onClick = { navController.navigate(SkylineScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Button")
            },
            //label = {Text("Search")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(selected = currentDestination == FeedListScreenDestination,
            onClick = { navController.navigate(FeedListScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                Icon(imageVector = Icons.Outlined.DynamicFeed, contentDescription = "Feeds Button")
            },
            //label = {Text("Feeds")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(selected = currentDestination == NotificationsScreenDestination,
            onClick = { navController.navigate(NotificationsScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = "Notifications Button")
            },
            //label = {Text("Notifications")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(
            selected = currentDestination == MyProfileScreenDestination,
            onClick = { navController.navigate(MyProfileScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                if (profilePic != null) {
                    profilePic.invoke {
                        navController.navigate(MyProfileScreenDestination) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile Button"
                    )
                }
            },
            //label = { Text("Profile") },
            alwaysShowLabel = false,
        )
    }
}

@Composable
fun NimbusNavRail(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    profilePic: @Composable() ((() -> Unit) -> Unit)? = null,
    actor: AtIdentifier? = null,

    ) {
    val currentDestination: Destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination

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
            onClick = { navController.navigate(SkylineScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            } },
            icon = {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home Button")
            },
            //label = {Text("Home")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(selected = false, //currentDestination == SkylineScreenDestination.invoke(),
            onClick = { navController.navigate(SkylineScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Button")
            },
            //label = {Text("Search")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(selected = currentDestination == FeedListScreenDestination,
            onClick = { navController.navigate(FeedListScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                Icon(imageVector = Icons.Outlined.DynamicFeed, contentDescription = "Feeds Button")
            },
            //label = {Text("Feeds")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(selected = currentDestination == NotificationsScreenDestination,
            onClick = { navController.navigate(NotificationsScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = "Notifications Button")
            },
            //label = {Text("Notifications")},
            alwaysShowLabel = false,
        )
        NavigationRailItem(
            selected = currentDestination == MyProfileScreenDestination,
            onClick = { navController.navigate(MyProfileScreenDestination) {
                popUpTo(NavGraphs.root) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }  },
            icon = {
                if (profilePic != null) {
                    profilePic.invoke {
                        navController.navigate(ProfileScreenDestination(actor)) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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