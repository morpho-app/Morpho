package com.morpho.app.ui.common


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
inline fun RepostQueryDialog(
    crossinline onDismissRequest: () -> Unit = {},
    crossinline onRepost: () -> Unit = {},
    crossinline onQuotePost: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true,
        ),
    ) {
        BackHandler {
            onDismissRequest()
        }
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.padding(36.dp)
        ) {
            TextButton(
                onClick = {
                    onRepost()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Repeat, contentDescription = null)
                Text(text = "Repost")
            }
            TextButton(
                onClick = {
                    onQuotePost()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.FormatQuote, contentDescription = null)
                Text(text = "Quote Post")
            }

            Button(
                onClick = { onDismissRequest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

