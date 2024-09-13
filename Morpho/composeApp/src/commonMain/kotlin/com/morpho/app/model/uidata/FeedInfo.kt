package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import com.morpho.app.model.bluesky.FeedGenerator
import com.morpho.app.model.bluesky.UserList
import com.morpho.butterfly.AtUri
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Immutable
@Serializable
data class FeedInfo(
    val uri: AtUri,
    val name: String,
    val description: String? = null,
    val avatar: String? = null,
    val feed: FeedGenerator? = null,
    val list: UserList? = null,
): Parcelable