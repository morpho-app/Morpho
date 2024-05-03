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
import androidx.compose.ui.util.fastAny
import app.bsky.actor.Visibility
import com.morpho.app.model.bluesky.HideReason
import com.morpho.app.model.bluesky.LabelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf


@Composable
fun ColumnScope.ContentHider(
    reasons: ImmutableList<HideReason> = persistentListOf(HideReason.SHOW),
    scope: LabelScope = LabelScope.NONE,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var hideContent by remember { mutableStateOf(
        reasons.fastAny { it.visibility == Visibility.HIDE  || it.visibility == Visibility.WARN } &&
                reasons.fastAny { scope != LabelScope.NONE && it.scope == scope }
    ) }
    val reasonText = reasons.filter{it.scope == scope}
        .firstOrNull { it.visibility == Visibility.WARN || it.visibility == Visibility.HIDE }
        ?.label ?: "Unknown reason"
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