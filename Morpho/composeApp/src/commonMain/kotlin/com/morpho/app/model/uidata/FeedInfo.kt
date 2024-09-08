package com.morpho.app.model.uidata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.morpho.app.model.bluesky.FeedGenerator
import com.morpho.app.model.bluesky.UserList
import com.morpho.butterfly.AtUri

@Immutable
data class FeedInfo(
    val uri: AtUri,
    val name: String,
    val description: String? = null,
    val avatar: String? = null,
    val icon: ImageVector = Icons.Default.RssFeed,
    val feed: FeedGenerator? = null,
    val list: UserList? = null,
)