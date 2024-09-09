package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import com.morpho.app.CommonParcelable
import com.morpho.app.CommonParcelize
import com.morpho.app.CommonRawValue
import com.morpho.app.util.JavaSerializable
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
@CommonParcelize
sealed interface MorphoDataItem: CommonParcelable, JavaSerializable {

    @Immutable
    @Serializable
    @CommonParcelize
    sealed interface FeedItem: MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class Post(
        val post: @CommonRawValue BskyPost,
        val reason: @CommonRawValue BskyPostReason? = null,
    ): FeedItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class Thread(
        val thread: @CommonRawValue BskyPostThread,
        val reason: @CommonRawValue BskyPostReason? = null,
    ): FeedItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class FeedInfo(
        val feed: @CommonRawValue FeedGenerator,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class ProfileItem(
        val profile: @CommonRawValue Profile,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class ListInfo(
        val list: @CommonRawValue BskyList,
    ): MorphoDataItem


    @Immutable
    @Serializable
    @CommonParcelize
    data class ModLabel(
        val label: @CommonRawValue BskyLabelDefinition,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class LabelService(
        val service: @CommonRawValue BskyLabelService,
    ): MorphoDataItem

}
