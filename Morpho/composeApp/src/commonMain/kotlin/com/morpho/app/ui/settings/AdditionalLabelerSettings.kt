package com.morpho.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.bsky.labeler.LabelerViewDetailed
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.bluesky.toLabelService
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.SettingsGroup
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

@Composable
fun AdditionalLabelerSettings(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    distinguish: Boolean = true,
    navigator: Navigator = LocalNavigator.currentOrThrow,
) {
    val labelers by agent.labelersDetailed.collectAsState(initial = listOf())
    val scope = rememberCoroutineScope()
    val onLabelerClicked: (LabelerViewDetailed) -> Unit = { labeler ->
        //TODO: open labeler
        scope.launch {
            val labelerProfile = labeler.toLabelService(agent)
        }
    }
    SettingsGroup(
        title = "Advanced labeler settings",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        labelers.forEach { labeler ->
            LabelerLink(
                labeler = labeler,
                onClick = { onLabelerClicked(labeler) },
                modifier = modifier
            )
        }

    }
}

@Composable
fun LabelerLink(
    labeler: LabelerViewDetailed,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.padding(6.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = modifier.clickable(onClick = onClick),
        ) {
            OutlinedAvatar(
                url = labeler.creator.avatar.orEmpty(),
                contentDescription = "Avatar for ${labeler.creator.displayName.orEmpty()}",
                size = 50.dp,
                avatarShape = AvatarShape.Rounded,
                modifier = modifier
                    .padding(6.dp)
            )
            Column {
                Text(
                    text = labeler.creator.displayName.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = modifier
                        .padding(6.dp)
                )
                Text(
                    text = labeler.creator.handle.handle,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = modifier
                        .padding(6.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Labeler",
                modifier = modifier
                    .padding(6.dp)
            )
        }
    }
}