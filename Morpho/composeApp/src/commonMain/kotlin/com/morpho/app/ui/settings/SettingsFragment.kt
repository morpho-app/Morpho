package com.morpho.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.ImagesearchRoller
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.data.MorphoAgent
import com.morpho.app.screens.settings.AccessibilitySettingsScreen
import com.morpho.app.screens.settings.AppearanceSettingsScreen
import com.morpho.app.screens.settings.FeedSettingsScreen
import com.morpho.app.screens.settings.LanguageSettingsScreen
import com.morpho.app.screens.settings.ModerationSettingsScreen
import com.morpho.app.screens.settings.NotificationsSettingsScreen
import com.morpho.app.screens.settings.ThreadSettingsScreen
import com.morpho.app.ui.elements.SettingsItem
import org.koin.compose.getKoin

@Composable
fun SettingsFragment(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    navigator: Navigator = LocalNavigator.currentOrThrow,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().then(modifier)

    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Basics",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Surface(
            elevation = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Column {
                SettingsItem(
                    description = AnnotatedString("Accessibility"),
                    modifier = Modifier.clickable {
                        navigator.push(AccessibilitySettingsScreen)
                    }.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Accessibility, contentDescription = "Accessibility")
                }
                SettingsItem(
                    description = AnnotatedString("Appearance"),
                    modifier = Modifier.clickable {
                        navigator.push(AppearanceSettingsScreen)
                    }
                ) {
                    Icon(Icons.Default.ImagesearchRoller, contentDescription = "Appearance")
                }
                SettingsItem(
                    description = AnnotatedString("Languages"),
                    modifier = Modifier.clickable {
                        navigator.push(LanguageSettingsScreen)
                    }
                ) {
                    Icon(Icons.Default.Translate, contentDescription = "Languages")
                }

                SettingsItem(
                    description = AnnotatedString("Moderation"),
                    modifier = Modifier.clickable {
                        navigator.push(ModerationSettingsScreen)
                    }
                ) {
                    Icon(Icons.Default.BackHand, contentDescription = "Moderation")
                }
                SettingsItem(
                    description = AnnotatedString("Notifications filtering"),
                    modifier = Modifier.clickable {
                        navigator.push(NotificationsSettingsScreen)
                    }
                ) {
                    Icon(Icons.Default.FilterAlt, contentDescription = "Notifications")
                }
                SettingsItem(
                    description = AnnotatedString("Following Feed Preferences"),
                    modifier = Modifier.clickable {
                        navigator.push(FeedSettingsScreen)
                    }
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Following Feed Preferences")
                }

                SettingsItem(
                    description = AnnotatedString("Thread Preferences"),
                    modifier = Modifier.clickable {
                        navigator.push(ThreadSettingsScreen)
                    }
                ) {
                    Icon(Icons.Default.Forum, contentDescription = "Thread Preferences")
                }
                SettingsItem(
                    description = AnnotatedString("My Saved Feeds"),
                    modifier = Modifier.clickable {  }
                ) {
                    Icon(Icons.Default.RssFeed, contentDescription = "My Saved Feeds")
                }

            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Advanced",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Surface(
            elevation = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        ){

        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = { },
            colors = ButtonDefaults.elevatedButtonColors(),
            modifier = Modifier.padding(vertical = 8.dp).padding(horizontal = 12.dp),
            shape = RectangleShape,
        ) {
            Text("System Log",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
            )
        }
        val version = com.morpho.app.BuildKonfig.versionString
        Text(
            "Version $version",
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}