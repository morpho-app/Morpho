package morpho.app.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Mildly dirty fake type union for stuff on skylines
 * Also provides a bit of useful functionality for expanding threads in-place
 */

//public data class SkylineItem(
//    var post: BskyPost? = null,
//    var thread: BskyPostThread? = null,
//    val reason: BskyPostReason? = null,
//) {
//}

@Immutable
@Serializable
sealed interface SkylineItem {
    @Immutable
    @Serializable
    data class PostItem(
        val post: BskyPost,
        val reason: BskyPostReason? = null,
    ): SkylineItem

    @Immutable
    @Serializable
    data class ThreadItem(
        val thread: BskyPostThread,
        val reason: BskyPostReason? = null,
    ): SkylineItem
}