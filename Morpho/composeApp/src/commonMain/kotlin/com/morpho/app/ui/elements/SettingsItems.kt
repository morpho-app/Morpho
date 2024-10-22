package com.morpho.app.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
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
        shape = if(distinguish) MaterialTheme.shapes.small else RectangleShape,
    ) {
        if(title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.Start)
            )
            HorizontalDivider(Modifier.padding(bottom = 4.dp))
        }
        content()
    }
}



@Composable
fun SettingsItem(
    text: AnnotatedString? = null,
    description: AnnotatedString? = null,
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable (Modifier) -> Unit,
){
    Surface(
        modifier = modifier.fillMaxWidth().padding(vertical = spacing),
        shape = if(spacing > 0.dp) MaterialTheme.shapes.small else RectangleShape,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        tonalElevation = 2.dp,
    ) {
        if(text != null && description == null) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(horizontal = 12.dp)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.Start)
                )
                content(Modifier.padding(start = 12.dp, end = 12.dp))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
            ) {
                if(description != null && text != null) {
                    content(Modifier.padding(horizontal = 12.dp))
                    VerticalDivider(Modifier.height(40.dp))
                    Column(
                        modifier = Modifier
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                                .align(Alignment.Start)
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.Start)
                        )
                    }

                } else {
                    content(Modifier.padding(horizontal = 12.dp))
                    Text(
                        text = description?: AnnotatedString(""),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(12.dp)
                    )
                }

            }
        }
    }

}