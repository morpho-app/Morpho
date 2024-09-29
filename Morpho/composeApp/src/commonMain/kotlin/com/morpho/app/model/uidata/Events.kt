package com.morpho.app.model.uidata

import app.bsky.actor.*
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.AuthorFilter
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.FeedDescriptor
import com.morpho.app.model.bluesky.FeedSourceInfo
import com.morpho.app.model.uistate.ListsOrFeeds
import com.morpho.app.ui.common.ComposerRole
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
sealed interface Event {
    data class UpdateSeenNotifications(
        val seenAt: Timestamp = Clock.System.now()
    ): Event

    data class ComposePost(
        val post: BskyPost,
        val role: ComposerRole = ComposerRole.StandalonePost,
    ): Event, PostEvent
}
sealed interface ModerationEvent: Event

sealed interface LoadEvent: Event
sealed interface ListEvent: Event

sealed interface FeedEvent: Event {
    val uri: AtUri?
    data class Load(
        val descriptor: FeedDescriptor,
    ): FeedEvent, LoadEvent {
        override val uri: AtUri = when(descriptor) {
            is FeedDescriptor.Author -> when(descriptor.filter) {
                AuthorFilter.PostsNoReplies -> AtUri.profilePostsUri(descriptor.did)
                AuthorFilter.PostsWithReplies -> AtUri.profileRepliesUri(descriptor.did)
                AuthorFilter.PostsAuthorThreads -> AtUri.profileRepliesUri(descriptor.did)
                AuthorFilter.PostsWithMedia -> AtUri.profileMediaUri(descriptor.did)
            }
            is FeedDescriptor.FeedGen -> descriptor.uri
            is FeedDescriptor.Likes -> AtUri.profileLikesUri(descriptor.did)
            is FeedDescriptor.List -> descriptor.uri
            is FeedDescriptor.Home -> AtUri.HOME_URI
        }
    }

    data class LoadLists(
        val actor: AtIdentifier,
        val listsOrFeeds: ListsOrFeeds,
    ): FeedEvent, LoadEvent, ListEvent {
        override val uri: AtUri = AtUri.profileUserListsUri(actor)
    }

    data class LoadFeed(
        val actor: AtIdentifier,
        val filter: AuthorFilter?,
    ): FeedEvent, LoadEvent, ListEvent {
        override val uri: AtUri = when(filter) {
            AuthorFilter.PostsNoReplies -> AtUri.profilePostsUri(actor)
            AuthorFilter.PostsWithReplies -> AtUri.profileRepliesUri(actor)
            AuthorFilter.PostsAuthorThreads -> AtUri.profileRepliesUri(actor)
            AuthorFilter.PostsWithMedia -> AtUri.profileMediaUri(actor)
            null -> AtUri.profileLikesUri(actor)
        }
    }

    data class LoadSaved(
        val info: SavedFeed,
    ): FeedEvent, LoadEvent {
        override val uri: AtUri = AtUri(info.value)
    }

    data class LoadHydrated(
        val info: FeedSourceInfo,
    ): FeedEvent, LoadEvent {
        override val uri: AtUri = info.uri
    }

    data class Peek(
        val info: FeedSourceInfo
    ): FeedEvent {
        override val uri: AtUri = info.uri
    }


}

sealed interface PageEvent: Event
sealed interface ListPageEvent: PageEvent

sealed interface FeedPageEvent: PageEvent {
    data class LikeFeed(val like: StrongRef): FeedPageEvent, LikeEvent
    data class UnlikeFeed(val uri: AtUri): FeedPageEvent, LikeEvent
    data class Save(val info: SavedFeed): FeedPageEvent, PrefsEvent
    data class UnSave(val id: String): FeedPageEvent, PrefsEvent
}

sealed interface CuratedListPageEvent: ListPageEvent {
    data class Pin(val info: SavedFeed): CuratedListPageEvent, PrefsEvent
    data class Unpin(val id: String): CuratedListPageEvent, PrefsEvent
}

sealed interface ModListPageEvent: ListPageEvent {
    data class MuteList(val list: AtUri): ModListPageEvent, ModerationEvent
    data class UnmuteList(val uri: AtUri): ModListPageEvent, ModerationEvent
    data class BlockList(val list: AtUri): ModListPageEvent, ModerationEvent
    data class UnblockList(val uri: AtUri): ModListPageEvent, ModerationEvent
}

sealed interface PrefsEvent: Event {
    data class MuteWord(val word: MutedWord): PrefsEvent
    data class UnMuteWord(val word: MutedWord): PrefsEvent
    data class SetThreadViewPref(val pref: ThreadViewPref): PrefsEvent
    data class SetFeedViewPref(val feed: String, val feedViewPref: FeedViewPref): PrefsEvent
}

sealed interface ListDataEvent: Event {
    data class LoadActor(
        val actor: AtIdentifier
    ): ListDataEvent, LoadEvent

    data class LoadFromPost(
        val post: AtUri
    ): ListDataEvent, LoadEvent
}

sealed interface SearchEvent: Event {
    val query: String?

    data class Actors(
        val term: String? = null,
        override val query: String? = null,
    ): SearchEvent

    data class ActorsTypeahead(
        val term: String? = null,
        override val query: String? = null,
    ): SearchEvent

    data class Posts(
        override val query: String? = null,
    ): SearchEvent
}

// Unsure about some of these, maybe events should only be repeatable things?
sealed interface LikeEvent: Event
sealed interface ThreadEvent: Event

sealed interface PostEvent: Event {
    data class Reply(val post: BskyPost): PostEvent
    data class Quote(val post: BskyPost): PostEvent


    data class LikePost(val like: StrongRef): PostEvent, LikeEvent
    data class UnlikePost(val uri: AtUri): PostEvent, LikeEvent
    data class Repost(val repost: StrongRef): PostEvent
    data class DeleteRepost(val uri: AtUri): PostEvent

    data class Hide(val uri: AtUri): PostEvent, PrefsEvent, ModerationEvent
    data class Unhide(val uri: AtUri): PostEvent, PrefsEvent, ModerationEvent

    data class LoadThread(val post: AtUri): PostEvent, LoadEvent, ThreadEvent
    data class ReportPost(val subject: StrongRef): PostEvent, ModerationEvent
}

sealed interface LabelerEvent: Event {
    data class LikeLabeler(val like: StrongRef): LabelerEvent, LikeEvent
    data class UnlikeLabeler(val uri: AtUri): LabelerEvent, LikeEvent
    data class Subscribe(val did: Did): LabelerEvent, PrefsEvent, ModerationEvent
    data class Unsubscribe(val did: Did): LabelerEvent, PrefsEvent, ModerationEvent

    data class SetLabelPref(
        val label: String,
        val value: Visibility,
        val labeler: Did,
    ): LabelerEvent, PrefsEvent, ModerationEvent
}

sealed interface MyProfileEvent: ProfileEvent {
    data object EnterEditing: MyProfileEvent
    data object ExitEditing: MyProfileEvent
}

sealed interface ProfileEditEvent: MyProfileEvent {
    data class SetDisplayName(val name: String): ProfileEditEvent
    data class SetDescription(val description: String): ProfileEditEvent
    data class SetAvatar(val avatar: PlatformFile): ProfileEditEvent
    data class SetBanner(val banner: PlatformFile): ProfileEditEvent
}

sealed interface ActorEvent: Event {

}

sealed interface ProfileEvent: ActorEvent {
    data class Follow(val subject: Did): ProfileEvent
    data class Unfollow(val uri: AtUri): ProfileEvent

    data class Mute(val subject: Did): ProfileEvent, PrefsEvent, ModerationEvent
    data class Unmute(val subject: Did): ProfileEvent, PrefsEvent, ModerationEvent

    data class Block(val subject: Did): ProfileEvent, ModerationEvent
    data class Unblock(val uri: AtUri): ProfileEvent, ModerationEvent

    data class ReportAccount(val subject: Did): ProfileEvent, ModerationEvent
}