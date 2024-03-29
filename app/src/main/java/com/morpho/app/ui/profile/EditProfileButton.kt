package com.morpho.app.ui.profile

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditProfileButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.labelLarge,
                fontSize = MaterialTheme.typography.labelLarge
                    .fontSize.times(0.9)
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
        },
        onClick = onClick,
        shape = ButtonDefaults.filledTonalShape,
        modifier = modifier
            .heightIn(min = 30.dp, max = 48.dp)
    )
}