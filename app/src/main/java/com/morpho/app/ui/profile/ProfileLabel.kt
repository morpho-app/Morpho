package com.morpho.app.ui.profile

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morpho.app.model.BskyLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileLabel(
    modifier: Modifier = Modifier,
    label: BskyLabel
) {
    InputChip(
        selected = true,
        onClick = { /*TODO*/ },
        label = {
            Text(
                text = label.value,
                style = MaterialTheme.typography.labelSmall,
                modifier = modifier
            )
        },
        modifier = Modifier
            .heightIn(min = 20.dp, max = 48.dp)
            .padding(horizontal = 6.dp)

    )
}