package com.morpho.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
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
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .clickable(onClick = onClick).padding(12.dp),
        ) {
            OutlinedAvatar(
                url = labeler.creator.avatar.orEmpty(),
                contentDescription = "Avatar for ${labeler.creator.displayName.orEmpty()}",
                size = 50.dp,
                avatarShape = AvatarShape.Rounded,
            )
            Column(
                Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = labeler.creator.displayName.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "@${labeler.creator.handle.handle}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.width(6.dp).weight(1F))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Labeler",
            )
        }
    }
}