package com.morpho.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.SettingsGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.morpho.app.screens.base.tabbed.MyProfileTab
import com.morpho.app.screens.login.LoginScreen
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.butterfly.Did
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

@Composable
fun UserManagement(
    navigator: Navigator = LocalNavigator.currentOrThrow,
    sm: TabbedMainScreenModel = navigator.koinNavigatorScreenModel<TabbedMainScreenModel>(),
    profiles: Flow<List<DetailedProfile>> = sm.agent.getAccounts(),
    myProfile: DetailedProfile? = null,
    modifier: Modifier = Modifier,
    distinguish: Boolean = false,
    topLevel: Boolean = false,
) {
    val users = profiles.collectAsState(initial = if(myProfile != null) listOf(myProfile) else listOf())
    val loggedInUser = remember { users.value.firstOrNull { it.did == sm.agent.id } }
    val otherUsers = remember { users.value.filter { it.did != sm.agent.id } }
    val mainNav = remember {
        when (navigator.level) {
            0 -> navigator
            else -> navigator.parent!!
        }
    }
    val rootNav = LocalTabNavigator.current
    val menuOptionClicked: (AccountMenuOption, Did) -> Unit = remember {
        { option, did ->
            when(option) {
                AccountMenuOption.RemoveAccount -> {
                    sm.agent.removeAccount(did)
                    if(sm.agent.id == did) {
                        sm.logout()
                        mainNav.popUntilRoot()
                        rootNav.current = LoginScreen
                    }
                }
                AccountMenuOption.LogOut -> {
                    sm.logout()
                    mainNav.popUntilRoot()
                    rootNav.current = LoginScreen
                }
            }
        }
    }
    SettingsGroup(
        title = if(!topLevel) "Account" else "",
        modifier = modifier.fillMaxWidth(),
        distinguish = distinguish,
    ) {
        if(loggedInUser != null) {
            Text(
                text = "Logged in as ${loggedInUser.displayName ?: loggedInUser.handle.handle}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp)
            )
            AccountItem(
                profile = loggedInUser,
                onClick = {
                    mainNav.push(MyProfileTab)
                },
                onMenuClicked = menuOptionClicked
            )
        }
        Text(
            text = "Other accounts",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )
        otherUsers.forEach { profile ->
            AccountItem(
                profile = profile,
                onClick = { sm.switchUser(profile.did) },
                onMenuClicked = menuOptionClicked
            )
        }
    }

}

@Composable
fun AccountItem(
    profile: DetailedProfile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onMenuClicked: (AccountMenuOption, Did) -> Unit = { _, _ -> },
) {
    Row(
        modifier = modifier.padding(12.dp).fillMaxWidth().clickable { onClick() }
    ) {
        OutlinedAvatar(
            url = profile.avatar.orEmpty(),
            contentDescription = "Avatar for ${profile.displayName}",
            size = 50.dp,
            avatarShape = AvatarShape.Rounded,
            outlineColor = MaterialTheme.colorScheme.background,
        )
        Column(
            Modifier.padding(horizontal = 12.dp).weight(1f)
        ) {
            val name = profile.displayName ?: profile.handle.handle
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${profile.handle}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        var showMenu by remember { mutableStateOf(false) }
        IconButton(
            onClick = {
                showMenu = !showMenu
            }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
            )
            DisableSelection {
                AccountMenu(expanded = showMenu, onItemClicked = {
                    showMenu = false
                    onMenuClicked(it, profile.did)
                }, onDismissRequest = {
                    showMenu = false
                })
            }
        }


    }
}

enum class AccountMenuOption(val value: String) {
    RemoveAccount("Remove Account"),
    LogOut("Log Out"),
}

@Composable
fun AccountMenu(
    expanded : Boolean = false,
    onItemClicked: (AccountMenuOption) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    DropdownMenu(
        expanded = expanded, onDismissRequest = {onDismissRequest()},
        modifier = Modifier.background(
            MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            RoundedCornerShape(2.dp)
        )
    ) {
        AccountMenuOption.entries.forEach {
            DropdownMenuItem(text = { Text(it.value) }, colors = MenuDefaults.itemColors().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
            ), onClick = { onItemClicked(it) })
        }
    }
}