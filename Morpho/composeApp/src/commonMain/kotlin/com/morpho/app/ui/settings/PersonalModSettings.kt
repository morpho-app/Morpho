package com.morpho.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ReduceCapacity
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem
import org.koin.compose.getKoin

@Composable
fun PersonalModSettings(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    distinguish: Boolean = true,
) {
    SettingsGroup(
        title = "Moderation tools",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        SettingsItem(
            description = AnnotatedString("Muted words and tags"),
            modifier = Modifier.clickable {

            }
        ){
            Icon(
                Icons.Default.FilterAlt,
                contentDescription = "Filter",
            )
        }

        SettingsItem(
            description = AnnotatedString("Moderation lists"),
            modifier = Modifier.clickable {

            }
        ){
            Icon(
                Icons.Default.ReduceCapacity,
                contentDescription = "People",
            )
        }

        SettingsItem(
            description = AnnotatedString("Muted accounts"),
            modifier = Modifier.clickable {

            }
        ) {
            Icon(
                Icons.Default.VisibilityOff,
                contentDescription = "Mute/Hide",
            )
        }

        SettingsItem(
            description = AnnotatedString("Blocked accounts"),
            modifier = Modifier.clickable {

            }
        ){
            Icon(
                Icons.Default.Block,
                contentDescription = "Block",
            )
        }
    }
}