package com.morpho.app.ui.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.morpho.app.data.DarkModeSetting
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem
import com.morpho.app.ui.theme.segmentedButtonEnd
import com.morpho.app.ui.theme.segmentedButtonMiddle
import com.morpho.app.ui.theme.segmentedButtonStart
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    distinguish: Boolean = false,
    topLevel: Boolean = true,
) {
    val morphoPrefs by agent.morphoPrefs.collectAsState(initial = agent.morphoPrefs.value)
    SettingsGroup(
        title = if(!topLevel) "Appearance" else "",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        SettingsItem( text = AnnotatedString("Mode")) {
            var darkMode by remember {
                mutableStateOf(morphoPrefs.darkMode ?: DarkModeSetting.SYSTEM)
            }
            SingleChoiceSegmentedButtonRow(
                modifier = it
            ) {
                SegmentedButton(
                    selected = darkMode == DarkModeSetting.SYSTEM,
                    onClick = {
                        darkMode = DarkModeSetting.SYSTEM
                        agent.setDarkMode(DarkModeSetting.SYSTEM)
                    },
                    shape = segmentedButtonStart.small,
                    label = { Text("System") },
                )
                SegmentedButton(
                    selected = darkMode == DarkModeSetting.LIGHT,
                    onClick = {
                        darkMode = DarkModeSetting.LIGHT
                        agent.setDarkMode(DarkModeSetting.LIGHT)
                    },
                    shape = segmentedButtonMiddle,
                    label = { Text("Light") },
                )
                SegmentedButton(
                    selected = darkMode == DarkModeSetting.DARK,
                    onClick = {
                        darkMode = DarkModeSetting.DARK
                        agent.setDarkMode(DarkModeSetting.DARK)
                    },
                    shape = segmentedButtonEnd.small,
                    label = { Text("Dark") },
                )

            }
        }

        SettingsItem(text = AnnotatedString("Interface Style")) {
            var tabbed by remember {
                mutableStateOf(morphoPrefs.tabbed ?: true)
            }
            SingleChoiceSegmentedButtonRow(
                modifier = it
            ) {
                SegmentedButton(
                    selected = tabbed,
                    enabled = false,
                    onClick = {
                        tabbed = true
                        // TODO: come back when the non-tabbed view is ready
                        agent.setDarkMode(DarkModeSetting.DARK)
                    },
                    shape = segmentedButtonStart.small,
                    label = { Text("System") },
                )
                SegmentedButton(
                    selected = !tabbed,
                    enabled = false,
                    onClick = {
                        tabbed = false
                        // TODO: come back when the non-tabbed view is ready
                        agent.setDarkMode(DarkModeSetting.LIGHT)
                    },
                    shape = segmentedButtonEnd.small,
                    label = { Text("Light") },
                )
            }
        }
    }
}