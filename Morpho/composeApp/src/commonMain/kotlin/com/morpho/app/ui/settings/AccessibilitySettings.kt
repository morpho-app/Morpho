package com.morpho.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.morpho.app.data.AccessibilityPreferences
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem
import org.koin.compose.getKoin

@Composable
fun AccessibilitySettings(
    agent: MorphoAgent = getKoin().get(),
    distinguish: Boolean = true,
    modifier: Modifier = Modifier,
    topLevel: Boolean = true,
) {
    val morphoPrefs = agent.morphoPrefs.value
    SettingsGroup(
        title = if(!topLevel) "Accessibility" else "",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        SettingsGroup(
            title = "Alt Text",
            distinguish = true,
            modifier = Modifier.padding(8.dp),
        ) {
            SettingsItem( description = AnnotatedString("Require Alt Text")) { mod ->
                var requireAltText by remember {
                    mutableStateOf(morphoPrefs.accessibility?.requireAltText ?: false)
                }

                Switch(
                    checked = requireAltText,
                    onCheckedChange = {
                        requireAltText = it
                        agent.setAccessibilityPrefs(
                            AccessibilityPreferences.toUpdate(requireAltText = requireAltText)
                        )
                    },
                    modifier = mod
                )
            }

            SettingsItem( description = AnnotatedString("Display larger alt text")) { mod ->
                var showLargerAltText by remember {
                    mutableStateOf(morphoPrefs.accessibility?.displayLargerAltBadge ?: false)
                }

                Switch(
                    checked = showLargerAltText,
                    onCheckedChange = {
                        showLargerAltText = it
                        agent.setAccessibilityPrefs(
                            AccessibilityPreferences.toUpdate(displayLargerAltBadge = showLargerAltText)
                        )
                    },
                    modifier = mod
                )
            }
        }

        SettingsGroup(
            title = "Sensory",
            distinguish = true,
            modifier = Modifier.padding(8.dp),
        ) {
            SettingsItem( description = AnnotatedString("Disable autoplay for media")) { mod ->
                var disableAutoplay by remember {
                    mutableStateOf(morphoPrefs.accessibility?.disableAutoplay ?: false)
                }

                Switch(
                    checked = disableAutoplay,
                    onCheckedChange = {
                        disableAutoplay = it
                        agent.setAccessibilityPrefs(
                            AccessibilityPreferences.toUpdate(disableAutoplay = disableAutoplay)
                        )
                    },
                    modifier = mod
                )
            }

            SettingsItem( description = AnnotatedString("Reduce/remove animations")) { mod ->
                var reduceMotion by remember {
                    mutableStateOf(morphoPrefs.accessibility?.reduceMotion ?: false)
                }

                Switch(
                    checked = reduceMotion,
                    onCheckedChange = {
                        reduceMotion = it
                        agent.setAccessibilityPrefs(
                            AccessibilityPreferences.toUpdate(reduceMotion = reduceMotion)
                        )
                    },
                    modifier = mod
                )
            }
            SettingsItem( description = AnnotatedString("Disable haptic feedback")) { mod ->
                var disableHaptics by remember {
                    mutableStateOf(morphoPrefs.accessibility?.disableHaptics ?: false)
                }

                Switch(
                    checked = disableHaptics,
                    onCheckedChange = {
                        disableHaptics = it
                        agent.setAccessibilityPrefs(
                            AccessibilityPreferences.toUpdate(disableHaptics = disableHaptics)
                        )
                    },
                    modifier = mod
                )
            }
            SettingsItem( description = AnnotatedString("Simplify UI")) { mod ->
                var simpleUI by remember {
                    mutableStateOf(morphoPrefs.accessibility?.simpleUI ?: false)
                }

                Switch(
                    enabled = false,
                    checked = simpleUI,
                    onCheckedChange = {
                        simpleUI = it
                        agent.setAccessibilityPrefs(
                            AccessibilityPreferences.toUpdate(simpleUI = simpleUI)
                        )
                    },
                    modifier = mod
                )
            }
        }
    }
}