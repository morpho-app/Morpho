package com.morpho.app.ui.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import com.morpho.app.model.bluesky.LabelAction
import com.morpho.app.model.bluesky.LabelScope
import com.morpho.app.model.uidata.ContentHandling


@Composable
public fun ContentHider(
    reasons: List<ContentHandling> = listOf(),
    scope: LabelScope,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    val scopedBehaviours = reasons.filter { it.scope == scope }
    val toHide = scopedBehaviours.fastFilter { it.action == LabelAction.Blur || it.action == LabelAction.Alert }
    var hideContent by remember {
        mutableStateOf(
            toHide.isNotEmpty()
        )
    }

    val reason = toHide.firstOrNull()
    val degrees by animateFloatAsState(if (!hideContent) -90f else 90f)
    Column {
        if (toHide.isNotEmpty()) {
            Row(modifier = if (hideContent) Modifier.clip(MaterialTheme.shapes.small)
                    .clickable { hideContent = !hideContent }.fillMaxWidth().padding(12.dp)
                else Modifier
                    .clip(MaterialTheme.shapes.small.copy(bottomEnd = CornerSize(0.dp), bottomStart = CornerSize(0.dp)))
                    .clickable { hideContent = !hideContent }.fillMaxWidth().padding(12.dp)
                , horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = reason?.icon?.icon?: Icons.Default.Info,
                        contentDescription = reason?.source?.description,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(reason?.source?.name ?: "",
                         style = MaterialTheme.typography.labelLarge,
                         color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DisableSelection {
                    Image(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.rotate(degrees),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                }
            }
        }
        AnimatedVisibility(
            visible = !hideContent,
            enter = expandVertically(
                spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntSize.VisibilityThreshold
                )
            ),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}