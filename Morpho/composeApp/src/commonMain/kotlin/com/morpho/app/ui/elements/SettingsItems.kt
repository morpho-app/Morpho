package com.morpho.app.ui.elements

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp


@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    distinguish: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        colors = if (distinguish) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            )
        } else {
            CardDefaults.cardColors(containerColor = Color.Transparent)
        },
        elevation = if (distinguish) CardDefaults.elevatedCardElevation(4.dp)
        else  CardDefaults.elevatedCardElevation(0.dp) ,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.Start)
        )
        content()
    }
}



@Composable
fun ColumnScope.SettingsItem(
    text: AnnotatedString? = null,
    description: AnnotatedString? = null,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
){
    if(text != null && description == null) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.Start)
            )
            content(Modifier.padding(start = 12.dp, end = 12.dp))
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if(description != null && text != null) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Start)
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Start)
                    )
                }
                content(Modifier.padding(horizontal = 12.dp))
            } else {
                content(Modifier.padding(horizontal = 12.dp))
                Text(
                    text = description?: AnnotatedString(""),
                    modifier = Modifier
                        .padding(12.dp)
                )
            }

        }
    }

}