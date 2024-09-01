package com.morpho.app.ui.elements

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    if (toHide.isNotEmpty()) {
        TextButton(
            onClick = { hideContent = !hideContent },
            modifier = Modifier.fillMaxWidth(),
            shape = ButtonDefaults.textShape,
            colors = ButtonDefaults.elevatedButtonColors(),
            elevation = ButtonDefaults.filledTonalButtonElevation()
        ) {
            Icon(
                imageVector = reason?.icon ?: Icons.Default.Info,
                contentDescription = reason?.source?.description
            )
            DisableSelection {
                Text(
                    text = reason?.source?.name ?: "",
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .weight(0.3f)
            )
            DisableSelection {
                Text(
                    text = if (hideContent) {
                        "Show"
                    } else {
                        "Hide"
                    }
                )
            }

        }
    }
    if (!hideContent) {
        content()
    }
}