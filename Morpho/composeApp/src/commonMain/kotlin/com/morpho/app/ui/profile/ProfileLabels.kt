package com.morpho.app.ui.profile

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.morpho.app.model.bluesky.BskyLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileLabels(
    modifier: Modifier = Modifier,
    labels: List<BskyLabel>
) {
    FlowRow(
        modifier = modifier
    ) {
        labels.forEach {
            ProfileLabel(
                label = it,
                modifier = modifier

            )
        }
    }
}