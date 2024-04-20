package com.morpho.app.model.uidata

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uistate.FeedType
import com.morpho.app.util.validDid
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable


@Serializable
data class MorphoData<T: MorphoDataItem>(
    val title: String = "Home",
    val uri: AtUri = HOME_URI,
    val cursor: StateFlow<String?> = MutableStateFlow(null),
    val list: StateFlow<ImmutableList<T>> = MutableStateFlow(persistentListOf()),
) {
    companion object {
        fun profileUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile")
        }
        fun profilePostsUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile.posts")
        }
        fun profileRepliesUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile.replies")
        }

        fun profileMediaUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile.media")
        }

        fun profileLikesUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile.likes")
        }

        fun profileUserListsUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile.lists")
        }

        fun profileModServiceUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile.labelServices")
        }

        fun profileFeedsListUri(id: AtIdentifier): AtUri {
            return AtUri("at://app.morpho/${id}/profile.feeds")
        }

        val HOME_URI: AtUri = AtUri("at://app.morpho.home")
        val MY_PROFILE_URI: AtUri = AtUri("at://me/app.morpho.profile")
        val ProfilePostsUriRegex = Regex("at://(me|${Did.Regex.pattern}|${Handle.Regex.pattern})/app.morpho.profile.posts")
        val ProfileRepliesUriRegex = Regex("at://(me|${Did.Regex.pattern}|${Handle.Regex.pattern})/app.morpho.profile.replies")
        val ProfileMediaUriRegex = Regex("at://(me|${Did.Regex.pattern}|${Handle.Regex.pattern})/app.morpho.profile.media")
        val ProfileLikesUriRegex = Regex("at://(me|${Did.Regex.pattern}|${Handle.Regex.pattern})/app.morpho.profile.likes")
        val ProfileUserListsUriRegex = Regex("at://(me|${Did.Regex.pattern}|${Handle.Regex.pattern})/app.morpho.profile.lists")
        val ProfileModServiceUriRegex = Regex("at://(me|${Did.Regex.pattern}|${Handle.Regex.pattern})/app.morpho.profile.labelService")
        val ProfileFeedsListUriRegex = Regex("at://(me|${Did.Regex.pattern}|${Handle.Regex.pattern})/app.morpho.profile.feeds")
    }

    val isHome: Boolean
        get() = uri == HOME_URI

    val isProfileFeed: Boolean
        get() = uri.atUri.matches(ProfilePostsUriRegex) ||
                uri.atUri.matches(ProfileRepliesUriRegex) ||
                uri.atUri.matches(ProfileMediaUriRegex) ||
                uri.atUri.matches(ProfileLikesUriRegex) ||
                uri.atUri.matches(ProfileUserListsUriRegex) ||
                uri.atUri.matches(ProfileModServiceUriRegex) ||
                uri.atUri.matches(ProfileFeedsListUriRegex)
    val isMyProfile: Boolean
        get() = (isProfileFeed && uri.atUri.contains("me")) || (uri == MY_PROFILE_URI)

    val feedType: FeedType
        get() = when {
            isHome -> FeedType.HOME
            uri.atUri.matches(ProfilePostsUriRegex) -> FeedType.PROFILE_POSTS
            uri.atUri.matches(ProfileRepliesUriRegex) -> FeedType.PROFILE_REPLIES
            uri.atUri.matches(ProfileMediaUriRegex) -> FeedType.PROFILE_MEDIA
            uri.atUri.matches(ProfileLikesUriRegex) -> FeedType.PROFILE_LIKES
            else -> FeedType.OTHER
        }
}


val PROFILE_FEEDS_URI = Regex("at://app.morpho/(me|${validDid})/profile.feeds")
val PROFILE_LISTS_URI = Regex("at://app.morpho/(me|${validDid})/profile.lists")
val PROFILE_LABELS_URI = Regex("at://app.morpho/(me|${validDid})/profile.labels")