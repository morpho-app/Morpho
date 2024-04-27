package com.morpho.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morpho.app.model.uistate.NotificationsFilterState
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationsFilterElement(
    filterState: StateFlow<NotificationsFilterState>,
    onFilterClicked: (NotificationsFilterState) -> Unit
) {
    val filter by filterState.collectAsState()
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        FilterChip(
            selected = filter.showLikes,
            onClick = {
                onFilterClicked(filter.copy(showLikes = !filter.showLikes))
            },
            label = {
                Text(text = "Likes")
            },
            leadingIcon = if (filter.showLikes) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } },
            modifier = Modifier.padding(start = 8.dp).padding(vertical = 0.dp)
        )
        FilterChip(
            selected = filter.showReposts,
            onClick = {
                onFilterClicked(filter.copy(showReposts = !filter.showReposts))
            },
            label = {
                Text(text = "Reposts")
            },
            leadingIcon = if (filter.showReposts) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } },
            modifier = Modifier.padding(start = 8.dp).padding(vertical = 0.dp)
        )
        FilterChip(
            selected = filter.showFollows,
            onClick = {
                onFilterClicked(filter.copy(showFollows = !filter.showFollows))
            },
            label = {
                Text(text = "Follows")
            },
            leadingIcon = if (filter.showFollows) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } },
            modifier = Modifier.padding(start = 8.dp).padding(vertical = 0.dp)
        )
        FilterChip(
            selected = filter.showMentions,
            onClick = {
                onFilterClicked(filter.copy(showMentions = !filter.showMentions))
            },
            label = {
                Text(text = "Mentions")
            },
            leadingIcon = if (filter.showMentions) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } },
            modifier = Modifier.padding(start = 8.dp).padding(vertical = 0.dp)

        )
        FilterChip(
            selected = filter.showQuotes,
            onClick = {
                onFilterClicked(filter.copy(showQuotes = !filter.showQuotes))
            },
            label = {
                Text(text = "Quotes")
            },
            leadingIcon = if (filter.showQuotes) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } },
            modifier = Modifier.padding(start = 8.dp).padding(vertical = 0.dp)

        )
        FilterChip(
            selected = filter.showReplies,
            onClick = {
                onFilterClicked(filter.copy(showReplies = !filter.showReplies))
            },
            label = {
                Text(text = "Replies")
            },
            leadingIcon = if (filter.showReplies) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } },
            modifier = Modifier.padding(start = 8.dp).padding(vertical = 0.dp)

        )
        FilterChip(
            selected = !filter.showAlreadyRead,
            onClick = {
                onFilterClicked(filter.copy(showAlreadyRead = !filter.showAlreadyRead))
            },
            label = {
                Text(text = "Hide if read")
            },
            leadingIcon = if (!filter.showAlreadyRead) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } },
            modifier = Modifier.padding(start = 8.dp).padding(vertical = 0.dp)
        )
    }
}
