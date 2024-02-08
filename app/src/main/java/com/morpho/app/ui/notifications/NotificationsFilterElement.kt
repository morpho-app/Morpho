package morpho.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import morpho.app.screens.notifications.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationsFilterElement(
    viewModel: NotificationsViewModel
) {
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        FilterChip(
            selected = viewModel.notificationsFilter.likes,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                        .copy(likes = !viewModel.notificationsFilter.likes)
            },
            label = {
                Text(text = "Likes")
            },
            leadingIcon = if (viewModel.notificationsFilter.likes) {
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
            selected = viewModel.notificationsFilter.reposts,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(reposts = !viewModel.notificationsFilter.reposts)
            },
            label = {
                Text(text = "Reposts")
            },
            leadingIcon = if (viewModel.notificationsFilter.reposts) {
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
            selected = viewModel.notificationsFilter.follows,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(follows = !viewModel.notificationsFilter.follows)
            },
            label = {
                Text(text = "Follows")
            },
            leadingIcon = if (viewModel.notificationsFilter.follows) {
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
            selected = viewModel.notificationsFilter.mentions,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(mentions = !viewModel.notificationsFilter.mentions)
            },
            label = {
                Text(text = "Mentions")
            },
            leadingIcon = if (viewModel.notificationsFilter.mentions) {
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
            selected = viewModel.notificationsFilter.quotes,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(quotes = !viewModel.notificationsFilter.quotes)
            },
            label = {
                Text(text = "Quotes")
            },
            leadingIcon = if (viewModel.notificationsFilter.quotes) {
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
            selected = viewModel.notificationsFilter.replies,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(replies = !viewModel.notificationsFilter.replies)
            },
            label = {
                Text(text = "Replies")
            },
            leadingIcon = if (viewModel.notificationsFilter.replies) {
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
            selected = viewModel.state.hideRead,
            onClick = {
                viewModel.toggleUnread()
            },
            label = {
                Text(text = "Hide if read")
            },
            leadingIcon = if (viewModel.state.hideRead) {
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