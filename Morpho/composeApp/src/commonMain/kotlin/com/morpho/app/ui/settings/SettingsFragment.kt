package com.morpho.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsItem
import org.koin.compose.getKoin

@Composable
fun SettingsFragment(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
) {
    Column {
        Text("Basics")
        Surface(
            elevation = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Column {
                SettingsItem(
                    description = AnnotatedString("Accessibility"),
                    modifier = Modifier.clickable {  }
                ) {
                    Icon(Icons.Default.Accessibility, contentDescription = "Accessibility")
                }
                SettingsItem(
                    description = AnnotatedString("Appearance"),
                    modifier = Modifier.clickable {  }
                ) {
                    Icon(Icons.Default.ImagesearchRoller, contentDescription = "Appearance")
                }
                SettingsItem(
                    description = AnnotatedString("Languages"),
                    modifier = Modifier.clickable {  }
                ) {
                    Icon(Icons.Default.Translate, contentDescription = "Languages")
                }

                SettingsItem(
                    description = AnnotatedString("Moderation"),
                    modifier = Modifier.clickable {  }
                ) {
                    Icon(Icons.Default.BackHand, contentDescription = "Moderation")
                }
                SettingsItem(
                    description = AnnotatedString("Notifications filtering"),
                    modifier = Modifier.clickable {  }
                ) {
                    Icon(Icons.Default.FilterAlt, contentDescription = "Notifications")
                }
                SettingsItem(
                    description = AnnotatedString("Following Feed Preferences"),
                    modifier = Modifier.clickable {  }
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Following Feed Preferences")
                }

                SettingsItem(
                    description = AnnotatedString("Thread Preferences"),
                    modifier = Modifier.clickable {  }
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
        Text("Advanced")
        Surface(
            elevation = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        ){

        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = { },
            modifier = Modifier.padding(vertical = 8.dp),
            shape = RectangleShape,
        ) {
            Text("System Log")
        }
        val version = com.morpho.app.BuildKonfig.versionString
        Text("Version $version")
    }
}