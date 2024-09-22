package com.morpho.app.ui.settings

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
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
) {
    val morphoPrefs = agent.morphoPrefs.value
    SettingsGroup(
        title = "Accessibility",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        SettingsGroup(
            title = "Alt Text",
            distinguish = true,
        ) {
            SettingsItem( text = AnnotatedString("Require Alt Text")) {
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
                    }
                )
            }

            SettingsItem( text = AnnotatedString("Display larger alt text")) {
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
                    }
                )
            }
        }

        SettingsGroup(
            title = "Sensory",
            distinguish = true,
        ) {
            SettingsItem( text = AnnotatedString("Disable autoplay for media")) {
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
                    }
                )
            }

            SettingsItem( text = AnnotatedString("Reduce/remove animations")) {
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
                    }
                )
            }
            SettingsItem( text = AnnotatedString("Disable haptic feedback")) {
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
                    }
                )
            }
            SettingsItem( text = AnnotatedString("Simplify UI")) {
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
                    }
                )
            }
        }
    }
}