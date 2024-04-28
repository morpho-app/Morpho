package com.morpho.app.model.uidata

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import androidx.compose.ui.util.fastAny
import com.morpho.app.model.bluesky.MorphoDataFeed
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uistate.FeedType
import com.morpho.app.util.validDid
import com.morpho.butterfly.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

typealias AtCursor = String?

@Serializable
data class MorphoData<T: MorphoDataItem>(
    val title: String = "Home",
    val uri: AtUri = AtUri.HOME_URI,
    val cursor: AtCursor = null,
    val items: ImmutableList<T> = persistentListOf(),
    val query: JsonElement = JsonObject(emptyMap()),
) {
    companion object {


        fun <T : MorphoDataItem> concat(
            first: MorphoData<T>,
            last: MorphoData<T>,
            cursor: AtCursor = last.cursor
        ): MorphoData<T> {
            return MorphoData(
                items = (first.items union last.items).toPersistentList()
                    .sortedByDescending {
                        when (it) {
                            is MorphoDataItem.Post -> it.post.createdAt
                            is MorphoDataItem.Thread -> it.thread.post.createdAt
                            is MorphoDataItem.FeedInfo -> it.feed.indexedAt
                            is MorphoDataItem.ListInfo -> it.list.indexedAt
                            is MorphoDataItem.ModLabel -> Moment(Instant.DISTANT_PAST)
                            is MorphoDataItem.ProfileItem -> Moment(Instant.DISTANT_PAST)
                            is MorphoDataItem.LabelService -> it.service.indexedAt
                            else -> {
                                Moment(Instant.DISTANT_PAST)
                            }
                        }
                    }.toImmutableList(),
                cursor = cursor, title = first.title, uri = first.uri
            )
        }

        fun <T : MorphoDataItem> concat(
            first: MorphoData<T>,
            last: List<T>,
            cursor: AtCursor = first.cursor
        ): MorphoData<T> {
            return MorphoData(
                items = (first.items union last).toPersistentList()
                    .sortedByDescending {
                        when (it) {
                            is MorphoDataItem.Post -> it.post.createdAt
                            is MorphoDataItem.Thread -> it.thread.post.createdAt
                            is MorphoDataItem.FeedInfo -> it.feed.indexedAt
                            is MorphoDataItem.ListInfo -> it.list.indexedAt
                            is MorphoDataItem.ModLabel -> Moment(Instant.DISTANT_PAST)
                            is MorphoDataItem.ProfileItem -> Moment(Instant.DISTANT_PAST)
                            is MorphoDataItem.LabelService -> it.service.indexedAt
                            else -> {
                                Moment(Instant.DISTANT_PAST)
                            }
                        }
                    }.toImmutableList(),
                cursor = cursor, title = first.title, uri = first.uri
            )
        }

        fun <T : MorphoDataItem> concat(
            first: List<T>,
            last: MorphoData<T>,
            cursor: AtCursor = last.cursor
        ): MorphoData<T> {
            return MorphoData(
                items = (first union last.items).toPersistentList()
                    .sortedByDescending {
                        when (it) {
                            is MorphoDataItem.Post -> it.post.createdAt
                            is MorphoDataItem.Thread -> it.thread.post.createdAt
                            is MorphoDataItem.FeedInfo -> it.feed.indexedAt
                            is MorphoDataItem.ListInfo -> it.list.indexedAt
                            is MorphoDataItem.ModLabel -> Moment(Instant.DISTANT_PAST)
                            is MorphoDataItem.ProfileItem -> Moment(Instant.DISTANT_PAST)
                            is MorphoDataItem.LabelService -> it.service.indexedAt
                            else -> {
                                Moment(Instant.DISTANT_PAST)
                            }
                        }
                    }.toImmutableList(),
                cursor = cursor, title = last.title, uri = last.uri
            )
        }

    }

    val isHome: Boolean
        get() = uri == AtUri.HOME_URI

    val isProfileFeed: Boolean
        get() = uri.atUri.matches(AtUri.ProfilePostsUriRegex) ||
                uri.atUri.matches(AtUri.ProfileRepliesUriRegex) ||
                uri.atUri.matches(AtUri.ProfileMediaUriRegex) ||
                uri.atUri.matches(AtUri.ProfileLikesUriRegex) ||
                uri.atUri.matches(AtUri.ProfileUserListsUriRegex) ||
                uri.atUri.matches(AtUri.ProfileModServiceUriRegex) ||
                uri.atUri.matches(AtUri.ProfileFeedsListUriRegex)


    val isMyProfile: Boolean
        get() = (isProfileFeed && uri.atUri.contains("me")) || (uri == AtUri.MY_PROFILE_URI)

    val feedType: FeedType
        get() = when {
            isHome -> FeedType.HOME
            uri.atUri.matches(AtUri.ProfilePostsUriRegex) -> FeedType.PROFILE_POSTS
            uri.atUri.matches(AtUri.ProfileRepliesUriRegex) -> FeedType.PROFILE_REPLIES
            uri.atUri.matches(AtUri.ProfileMediaUriRegex) -> FeedType.PROFILE_MEDIA
            uri.atUri.matches(AtUri.ProfileLikesUriRegex) -> FeedType.PROFILE_LIKES
            uri.atUri.matches(AtUri.ProfileUserListsUriRegex) -> FeedType.PROFILE_USER_LISTS
            uri.atUri.matches(AtUri.ProfileModServiceUriRegex) -> FeedType.PROFILE_MOD_SERVICE
            uri.atUri.matches(AtUri.ProfileFeedsListUriRegex) -> FeedType.PROFILE_FEEDS_LIST
            else -> FeedType.OTHER
        }

    operator fun contains(cid: Cid): Boolean {
        return items.fastAny {
            when(it) {
                is MorphoDataItem.Post -> it.post.cid == cid
                is MorphoDataItem.Thread -> it.thread.contains(cid)
                is MorphoDataItem.FeedInfo -> it.feed.cid == cid
                is MorphoDataItem.ListInfo -> it.list.cid == cid
                is MorphoDataItem.ModLabel -> false
                is MorphoDataItem.ProfileItem -> false
                is MorphoDataItem.LabelService -> it.service.cid == cid
                else -> {false}
            }
        }
    }
}

fun MorphoDataFeed<MorphoDataItem>.toMorphoData(
    title: String = "",
    newUri: AtUri? = null
): MorphoData<MorphoDataItem> {
    return MorphoData(
        title = title,
        uri = newUri ?: uri,
        cursor = cursor,
        items = items
    )
}

fun AtUri.id(api:Butterfly): AtIdentifier {
    val idString = atUri.substringAfter("at://").split("/")[0]
    return if (idString == "me") api.id!! else {
        // TODO: make this resolve a handle to a DID
        if (idString.contains("did:")) Did(idString) else Handle(idString)
    }
}

val PROFILE_FEEDS_URI = Regex("at://app.morpho/(me|${validDid})/profile.feeds")
val PROFILE_LISTS_URI = Regex("at://app.morpho/(me|${validDid})/profile.lists")
val PROFILE_LABELS_URI = Regex("at://app.morpho/(me|${validDid})/profile.labels")