package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Type union for different types of data items that can be displayed in the app.
 * Can use interface directly or use subclasses for more specific types where needed.
 * Would like to figure out how to specify only a subset of types are used in a given context.
 * This would help keep "when" statements from scenario where we want
 *      e.g. PostItems and ThreadItems from needing to handle all possible subtypes.
 */
@Immutable
@Serializable
sealed interface MorphoDataItem {

    sealed interface FeedItem: MorphoDataItem

    @Immutable
    @Serializable
    data class Post(
        val post: BskyPost,
        val reason: BskyPostReason? = null,
    ): FeedItem

    @Immutable
    @Serializable
    data class Thread(
        val thread: BskyPostThread,
        val reason: BskyPostReason? = null,
    ): FeedItem

    @Immutable
    @Serializable
    data class FeedInfo(
        val feed: FeedGenerator,
    ): MorphoDataItem

    @Immutable
    @Serializable
    data class ProfileItem(
        val profile: Profile,
    ): MorphoDataItem

    @Immutable
    @Serializable
    data class ListInfo(
        val list: BskyList,
    ): MorphoDataItem


    @Immutable
    @Serializable
    data class ModLabel(
        val label: BskyLabelDefinition,
    ): MorphoDataItem

    @Immutable
    @Serializable
    data class LabelService(
        val service: BskyLabelService,
    ): MorphoDataItem

}
