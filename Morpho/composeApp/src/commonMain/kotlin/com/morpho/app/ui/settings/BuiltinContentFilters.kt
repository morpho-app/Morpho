package com.morpho.app.ui.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import app.bsky.actor.Visibility
import com.atproto.label.Severity
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem
import com.morpho.app.ui.theme.segmentedButtonEnd
import com.morpho.app.ui.theme.segmentedButtonMiddle
import com.morpho.app.ui.theme.segmentedButtonStart
import com.morpho.butterfly.InterpretedLabelDefinition
import com.morpho.butterfly.localize
import org.koin.compose.getKoin

@Composable
fun BuiltinContentFilters(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    distinguish: Boolean = true,
) {
    var adultContentEnabled by remember {
        mutableStateOf(agent.prefs.modPrefs.adultContentEnabled)
    }

    var modPrefs by remember {
        mutableStateOf(agent.prefs.modPrefs)
    }

    SettingsGroup(
        title = "Content filters",
        modifier = modifier,
        distinguish = distinguish,
    ) {

        SettingsItem(
            description = AnnotatedString("Enable adult content")
        ) {

            Switch(
                checked = adultContentEnabled,
                onCheckedChange = {
                    adultContentEnabled = it
                    agent.toggleAdultContent(it)
                }
            )
        }

        if(adultContentEnabled) {
            BuiltinContentFilterSelector(
                labelDefinition =  com.morpho.butterfly.Porn.localize(agent.myLanguage.value),
                initialFilter = modPrefs.labels[com.morpho.butterfly.Porn.identifier] ?:
                    com.morpho.butterfly.Porn.defaultSetting,
                onSelected = { visibility ->
                    modPrefs = modPrefs.copy(
                        labels = modPrefs.labels.toMutableMap().apply {
                            this[com.morpho.butterfly.Porn.identifier] = visibility
                        }
                    )
                    agent.setContentLabelPref(com.morpho.butterfly.Porn.identifier, visibility)
                }
            )
            BuiltinContentFilterSelector(
                labelDefinition =  com.morpho.butterfly.NSFW.localize(agent.myLanguage.value),
                initialFilter = modPrefs.labels[com.morpho.butterfly.NSFW.identifier] ?:
                com.morpho.butterfly.NSFW.defaultSetting,
                onSelected = { visibility ->
                    modPrefs = modPrefs.copy(
                        labels = modPrefs.labels.toMutableMap().apply {
                            this[com.morpho.butterfly.NSFW.identifier] = visibility
                        }
                    )
                    agent.setContentLabelPref(com.morpho.butterfly.NSFW.identifier, visibility)
                }
            )
            BuiltinContentFilterSelector(
                labelDefinition =  com.morpho.butterfly.GraphicMedia.localize(agent.myLanguage.value),
                initialFilter = modPrefs.labels[com.morpho.butterfly.GraphicMedia.identifier] ?:
                com.morpho.butterfly.GraphicMedia.defaultSetting,
                onSelected = { visibility ->
                    modPrefs = modPrefs.copy(
                        labels = modPrefs.labels.toMutableMap().apply {
                            this[com.morpho.butterfly.GraphicMedia.identifier] = visibility
                        }
                    )
                    agent.setContentLabelPref(com.morpho.butterfly.GraphicMedia.identifier, visibility)
                }
            )
        }
        BuiltinContentFilterSelector(
            labelDefinition =  com.morpho.butterfly.Nudity.localize(agent.myLanguage.value),
            initialFilter = modPrefs.labels[com.morpho.butterfly.Nudity.identifier] ?:
            com.morpho.butterfly.Nudity.defaultSetting,
            onSelected = { visibility ->
                modPrefs = modPrefs.copy(
                    labels = modPrefs.labels.toMutableMap().apply {
                        this[com.morpho.butterfly.Nudity.identifier] = visibility
                    }
                )
                agent.setContentLabelPref(com.morpho.butterfly.Nudity.identifier, visibility)
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.BuiltinContentFilterSelector(

    labelDefinition: InterpretedLabelDefinition,
    initialFilter: Visibility,
    onSelected: (Visibility) -> Unit,
    modifier: Modifier = Modifier,
) {
    var setting by remember { mutableStateOf(initialFilter) }
    val text = buildAnnotatedString {
        pushStyle(MaterialTheme.typography.titleSmall.toSpanStyle().copy(
            color = MaterialTheme.colorScheme.onSurface
        ))
        append("${labelDefinition.localizedName}\n")
        pop()
        pushStyle(MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ))
        append(labelDefinition.localizedDescription)
        pop()

        toAnnotatedString()
    }

    SettingsItem(
        text = text,
        modifier = modifier
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = it
        ) {
            SegmentedButton(
                selected = setting == Visibility.SHOW || setting == Visibility.IGNORE,
                onClick = {
                    setting = Visibility.SHOW
                    onSelected(Visibility.SHOW)
                },
                shape = segmentedButtonStart.small,
                label = { Text(text = "Show") }
            )
            SegmentedButton(
                selected = setting == Visibility.WARN,
                onClick = {
                    setting = Visibility.WARN
                    onSelected(Visibility.WARN)
                },
                shape = segmentedButtonMiddle,
                label = { Text(text = "Warn") }
            )
            SegmentedButton(
                selected = setting == Visibility.HIDE,
                onClick = {
                    setting = Visibility.HIDE
                    onSelected(Visibility.HIDE)
                },
                shape = segmentedButtonEnd.small,
                label = { Text(text = "Hide") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentLabelSelector(
    labelItem: MorphoDataItem.ModLabel,
    onSelected: (Visibility) -> Unit,
    modifier: Modifier = Modifier,
) {

    var setting by remember { mutableStateOf(labelItem.setting) }
    val label = labelItem.label
    val onItemClicked: (Visibility) -> Unit = remember { onSelected }
    val text = buildAnnotatedString {
        pushStyle(MaterialTheme.typography.titleSmall.toSpanStyle().copy(
            color = MaterialTheme.colorScheme.onSurface
        ))
        append("${label.localizedName}\n")
        pop()
        pushStyle(MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ))
        append(label.localizedDescription)
        pop()

        toAnnotatedString()
    }
    val middleButtonText = remember {
        if(label.severity == Severity.INFORM) {
            "Show badge"
        } else {
            "Warn"
        }
    }

    SettingsItem(
        text = text,
        modifier = modifier.padding(horizontal = 8.dp),
        spacing = 8.dp,
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = it
        ) {
            SegmentedButton(
                selected = setting == Visibility.SHOW || setting == Visibility.IGNORE,
                onClick = {
                    setting = Visibility.SHOW
                    onItemClicked(Visibility.SHOW)
                },
                shape = segmentedButtonStart.small,
                label = { Text(text = "Show") }
            )
            SegmentedButton(
                selected = setting == Visibility.WARN || setting == Visibility.INFORM,
                onClick = {
                    if(label.severity == Severity.INFORM) {
                        setting = Visibility.INFORM
                        onItemClicked(Visibility.INFORM)
                    } else {
                        setting = Visibility.WARN
                        onItemClicked(Visibility.WARN)
                    }
                },
                shape = segmentedButtonMiddle,
                label = { Text(text = middleButtonText) }
            )
            SegmentedButton(
                selected = setting == Visibility.HIDE,
                onClick = {
                    setting = Visibility.HIDE
                    onItemClicked(Visibility.HIDE)
                },
                shape = segmentedButtonEnd.small,
                label = { Text(text = "Hide") }
            )
        }
    }

}