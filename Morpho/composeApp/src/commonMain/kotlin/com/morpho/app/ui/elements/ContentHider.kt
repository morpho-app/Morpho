package com.morpho.app.ui.elements

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastFlatMap
import com.morpho.app.model.bluesky.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList


@Composable
fun ColumnScope.ContentHider(
    reasons: ImmutableList<DescribedBehaviours> = persistentListOf(),
    scope: LabelScope = LabelScope.None,
    target: LabelTarget = LabelTarget.Content,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scopedBehaviours: ImmutableList<DescribedAction> = reasons.fastFlatMap {
        it.describeAction(scope, target)
    }.toImmutableList()
    val toHide = scopedBehaviours.fastFilter { it.action == LabelAction.Blur || it.action == LabelAction.Alert }
    var hideContent by remember { mutableStateOf(
        toHide.isNotEmpty()
    ) }
    val reasonText = scopedBehaviours.fastFirstOrNull {
        it.action == LabelAction.Alert || it.action == LabelAction.Blur || it.action == LabelAction.Inform
    }?.label ?: "Unknown reason"
    TextButton(
        onClick = { hideContent = !hideContent },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null
        )
        Text(
            text = reasonText,

        )
        Spacer(modifier = Modifier
            .width(1.dp)
            .weight(0.3f))
        Text(
            text = if(hideContent) { "Show" } else { "Hide" }
        )
    }
    if(!hideContent) {
        content()
    }

}