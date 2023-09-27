package radiant.nimbus.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NimbusNavBar(
    modifier: Modifier = Modifier,
    profilePic: (@Composable () -> Unit)? = null,
) {
    NavigationBar(
        
    ) {
        NavigationBarItem(selected = true,
            onClick = { /*TODO*/ },
            icon = {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home Button")
            },
            //label = {Text("Home")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(selected = false,
            onClick = { /*TODO*/ },
            icon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Button")
            },
            //label = {Text("Search")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(selected = false,
            onClick = { /*TODO*/ },
            icon = {
                Icon(imageVector = Icons.Outlined.DynamicFeed, contentDescription = "Feeds Button")
            },
            //label = {Text("Feeds")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(selected = false,
            onClick = { /*TODO*/ },
            icon = {
                Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = "Notifications Button")
            },
            //label = {Text("Notifications")},
            alwaysShowLabel = false,
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = {
                if (profilePic != null) {
                    profilePic.invoke()
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