package com.morpho.app.ui.settings

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem

@Composable
fun AccessibilitySettings(
    agent: MorphoAgent,
    distinguish: Boolean = true,
    modifier: Modifier = Modifier,
) {
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
                    mutableStateOf(false)
                    /// TODO: Get preferences
                }

                Switch(
                    checked = requireAltText,
                    onCheckedChange = {
                        requireAltText = it
                        /// TODO: Update preferences
                    }
                )
            }

            SettingsItem( text = AnnotatedString("Display larger alt text")) {
                var showLargerAltText by remember {
                    mutableStateOf(false)
                    /// TODO: Get preferences
                }

                Switch(
                    checked = showLargerAltText,
                    onCheckedChange = {
                        showLargerAltText = it
                        /// TODO: Update preferences
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
                    mutableStateOf(false)
                    /// TODO: Get preferences
                }

                Switch(
                    checked = disableAutoplay,
                    onCheckedChange = {
                        disableAutoplay = it
                        /// TODO: Update preferences
                    }
                )
            }

            SettingsItem( text = AnnotatedString("Reduce/remove animations")) {
                var reduceMotion by remember {
                    mutableStateOf(false)
                    /// TODO: Get preferences
                }

                Switch(
                    checked = reduceMotion,
                    onCheckedChange = {
                        reduceMotion = it
                        /// TODO: Update preferences
                    }
                )
            }
            SettingsItem( text = AnnotatedString("Disable haptic feedback")) {
                var disableHaptics by remember {
                    mutableStateOf(false)
                    /// TODO: Get preferences
                }

                Switch(
                    checked = disableHaptics,
                    onCheckedChange = {
                        disableHaptics = it
                        /// TODO: Update preferences
                    }
                )
            }
            SettingsItem( text = AnnotatedString("Simplify UI")) {
                var simpleUI by remember {
                    mutableStateOf(false)
                    /// TODO: Get preferences
                }

                Switch(
                    enabled = false,
                    checked = simpleUI,
                    onCheckedChange = {
                        simpleUI = it
                        /// TODO: Update preferences
                    }
                )
            }
        }
    }
}