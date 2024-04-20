package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import com.morpho.butterfly.AtUri
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface ContentCardMapEntry {
    val uri: AtUri
    val title: String

    @Immutable
    @Serializable
    data class Skyline(
        override val uri: AtUri,
        override val title: String = uri.atUri,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class Feed(
        override val uri: AtUri,
        override val title: String = uri.atUri,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class PostThread(
        override val uri: AtUri,
        override val title: String = uri.atUri,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class UserList(
        override val uri: AtUri,
        override val title: String = uri.atUri,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class FeedList(
        override val uri: AtUri,
        override val title: String = uri.atUri,
    ) : ContentCardMapEntry

    @Immutable
    @Serializable
    data class ServiceList(
        override val uri: AtUri,
        override val title: String = uri.atUri,
    ) : ContentCardMapEntry
}