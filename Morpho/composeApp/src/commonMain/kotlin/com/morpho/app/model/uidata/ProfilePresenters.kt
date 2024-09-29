package com.morpho.app.model.uidata

import app.bsky.feed.GetActorFeedsQuery
import app.bsky.graph.GetListsQuery
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.bluesky.AuthorFilter
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.bluesky.FeedDescriptor
import com.morpho.app.model.bluesky.toLabelService
import com.morpho.app.model.bluesky.toProfile
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ListsOrFeeds
import com.morpho.butterfly.Did
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

class MyProfilePresenter(
    val profileState: ContentCardState.MyProfile,
): Presenter<Event>() {


    val postsPresenter = FeedPresenter<FeedEvent>(
        descriptor = FeedDescriptor.Author(profileState.profile.did, AuthorFilter.PostsNoReplies)
    )

    val postRepliesPresenter = FeedPresenter<FeedEvent>(
        descriptor = FeedDescriptor.Author(profileState.profile.did, AuthorFilter.PostsWithReplies)
    )
    val mediaPresenter = FeedPresenter<FeedEvent>(
        descriptor = FeedDescriptor.Author(profileState.profile.did, AuthorFilter.PostsWithMedia)
    )
    val likesPresenter = FeedPresenter<FeedEvent>(
        descriptor = FeedDescriptor.Likes(profileState.profile.did)
    )
    val listsPresenter = UserListPresenter(profileState.profile.did)
    val feedsPresenter = UserFeedsPresenter(profileState.profile.did)

    init {
        presenterScope.launch {
            profileState.posts.updates.emitAll(
                postsPresenter.produceUpdates(merge(profileState.events, profileState.posts.events)))
        }
        presenterScope.launch {
            profileState.postReplies.updates.emitAll(
                postRepliesPresenter.produceUpdates(merge(profileState.events, profileState.postReplies.events)))
        }
        presenterScope.launch {
            profileState.media.updates.emitAll(mediaPresenter
                .produceUpdates(merge(profileState.events, profileState.media.events)))
        }
        presenterScope.launch {
            profileState.likes.updates.emitAll(likesPresenter
                .produceUpdates(merge(profileState.events, profileState.likes.events)))
        }
        if(profileState.lists != null) presenterScope.launch {
            profileState.lists.updates.emitAll(listsPresenter
                .produceUpdates(merge(profileState.events, profileState.lists.events)))
        }
        if(profileState.feeds != null) presenterScope.launch {
            profileState.feeds.updates.emitAll(feedsPresenter
                .produceUpdates(merge(profileState.events, profileState.feeds.events)))
        }
    }

    companion object {
        val log = logging("ProfilePresenter")
        suspend fun initialize(
            agent: MorphoAgent,
            myProfile: DetailedProfile? = null,
        ): ContentCardState.MyProfile? {
            val id = agent.id ?: return null
            val profile = myProfile ?: agent.getProfile(id).getOrNull()?.toProfile() ?: return null
            val hasFeeds = agent.api
                .getActorFeeds(GetActorFeedsQuery(id, 1, null)).getOrNull()?.feeds?.isNotEmpty()
                ?: false
            val hasLists = agent.api
                .getLists(GetListsQuery(id, 1, null)).getOrNull()?.lists?.isNotEmpty() ?: false
            val maybeLabeler = agent.getLabelers(listOf(profile.did))
                .getOrNull()?.firstOrNull()?.toLabelService(agent)

            return ContentCardState.MyProfile(
                profile = profile,
                lists = if (hasLists) ContentCardState.ProfileList(
                    profile = profile,
                    ListsOrFeeds.Lists,
                ) else null,
                feeds = if (hasFeeds) ContentCardState.ProfileList(
                    profile = profile,
                    ListsOrFeeds.Feeds,
                ) else null,
                labeler = if (maybeLabeler != null) ContentCardState.ProfileLabeler(
                    profile = maybeLabeler,
                    uri = maybeLabeler.uri,
                ) else null,
            )
        }
        suspend fun create(
            agent: MorphoAgent,
            profile: DetailedProfile? = null,
        ): MyProfilePresenter? {
            val state = initialize(agent, profile) ?: return null
            return MyProfilePresenter(state)
        }
    }


    override fun <E : Event> produceUpdates(events: Flow<E>): Flow<UIUpdate> {
        val did = profileState.profile.did
        val combined = merge(events, profileState.events)
        return combined.map { event ->
            when (event) {

                is Event.ComposePost -> UIUpdate.OpenComposer(event.post, event.role)

                else -> {
                    log.d { "Unhandled event: $event" }
                    UIUpdate.NoOp
                }
            }
        } as Flow<UIUpdate>
    }
}

class ProfilePresenter(
    val profileState: ContentCardState.FullProfile,
): Presenter<Event>() {


    val postsPresenter = FeedPresenter<FeedEvent>(
        descriptor = FeedDescriptor.Author(profileState.profile.did, AuthorFilter.PostsNoReplies)
    )

    val postRepliesPresenter = FeedPresenter<FeedEvent>(
        descriptor = FeedDescriptor.Author(profileState.profile.did, AuthorFilter.PostsWithReplies)
    )
    val mediaPresenter = FeedPresenter<FeedEvent>(
        descriptor = FeedDescriptor.Author(profileState.profile.did, AuthorFilter.PostsWithMedia)
    )
    val listsPresenter = UserListPresenter(profileState.profile.did)
    val feedsPresenter = UserFeedsPresenter(profileState.profile.did)

    init {
        presenterScope.launch {
            profileState.posts.updates.emitAll(
                postsPresenter.produceUpdates(merge(profileState.events, profileState.posts.events)))
        }
        presenterScope.launch {
            profileState.postReplies.updates.emitAll(
                postRepliesPresenter.produceUpdates(merge(profileState.events, profileState.postReplies.events)))
        }
        presenterScope.launch {
            profileState.media.updates.emitAll(mediaPresenter
                .produceUpdates(merge(profileState.events, profileState.media.events)))
        }
        if(profileState.lists != null) presenterScope.launch {
            profileState.lists.updates.emitAll(listsPresenter
                .produceUpdates(merge(profileState.events, profileState.lists.events)))
        }
        if(profileState.feeds != null) presenterScope.launch {
            profileState.feeds.updates.emitAll(feedsPresenter
                .produceUpdates(merge(profileState.events, profileState.feeds.events)))
        }
    }
    companion object {
        val log = logging("ProfilePresenter")
        suspend fun initialize(
            agent: MorphoAgent,
            actor: Did,
            actorProfile: DetailedProfile? = null,
        ): ContentCardState.FullProfile? {
            val profile = actorProfile ?: agent.getProfile(actor).getOrNull()?.toProfile() ?: return null
            val hasFeeds = agent.api
                .getActorFeeds(GetActorFeedsQuery(actor, 1, null)).getOrNull()?.feeds?.isNotEmpty()
                ?: false
            val hasLists = agent.api
                .getLists(GetListsQuery(actor, 1, null)).getOrNull()?.lists?.isNotEmpty() ?: false
            val maybeLabeler = agent.getLabelers(listOf(profile.did))
                .getOrNull()?.firstOrNull()?.toLabelService(agent)

            return ContentCardState.FullProfile(
                profile = profile,
                lists = if (hasLists) ContentCardState.ProfileList(
                    profile = profile,
                    ListsOrFeeds.Lists,
                ) else null,
                feeds = if (hasFeeds) ContentCardState.ProfileList(
                    profile = profile,
                    ListsOrFeeds.Feeds,
                ) else null,
                labeler = if (maybeLabeler != null) ContentCardState.ProfileLabeler(
                    profile = maybeLabeler,
                    uri = maybeLabeler.uri,
                ) else null,
            )
        }
        suspend fun create(
            agent: MorphoAgent,
            actor: Did,
            actorProfile: DetailedProfile? = null,
        ): ProfilePresenter? {
            val state = initialize(agent, actor, actorProfile) ?: return null
            return ProfilePresenter(state)
        }
    }


    override fun <E : Event> produceUpdates(events: Flow<E>): Flow<UIUpdate> {
        val did = profileState.profile.did
        val combined = merge(events, profileState.events)
        return combined.map { event ->
            when (event) {
                is ProfileEvent.Block -> if(did == event.subject) {
                    agent.block(event.subject)
                    ActorUpdate.Blocked
                } else UIUpdate.NoOp
                is ProfileEvent.Follow -> if(did == event.subject) {
                    agent.follow(event.subject)
                    ActorUpdate.Followed
                } else UIUpdate.NoOp
                is ProfileEvent.Mute -> if(did == event.subject) {
                    agent.mute(event.subject)
                    ActorUpdate.Muted
                } else UIUpdate.NoOp
                is ProfileEvent.ReportAccount -> if(did == event.subject) {
                    ActorUpdate.Reported
                } else UIUpdate.NoOp
                is ProfileEvent.Unblock -> if(profileState.profile.block?.uri == event.uri) {
                    agent.unblock(event.uri)
                    ActorUpdate.Unblocked
                } else UIUpdate.NoOp
                is ProfileEvent.Unfollow -> if(profileState.profile.following?.uri == event.uri) {
                    agent.deleteFollow(event.uri)
                    ActorUpdate.Unfollowed
                } else UIUpdate.NoOp
                is ProfileEvent.Unmute -> if(profileState.profile.mutedByMe) {
                    agent.unmute(event.subject)
                    ActorUpdate.Unmuted
                } else UIUpdate.NoOp
                is Event.ComposePost -> UIUpdate.OpenComposer(event.post, event.role)
                is LabelerEvent.LikeLabeler -> {
                    agent.like(event.like)
                    ActorUpdate.Liked
                }
                is LabelerEvent.SetLabelPref -> {
                    // TODO: update labeler
                    UIUpdate.NoOp
                }
                is LabelerEvent.Subscribe -> {
                    agent.addLabeler(event.did)
                    UIUpdate.NoOp
                }
                is LabelerEvent.UnlikeLabeler -> if (profileState.labeler?.profile?.likeUri == event.uri) {
                    agent.deleteLike(event.uri)
                    ActorUpdate.Unliked
                } else UIUpdate.NoOp
                is LabelerEvent.Unsubscribe -> {
                    agent.removeLabeler(event.did)
                    UIUpdate.NoOp
                }
                else -> {
                    log.d { "Unhandled event: $event" }
                    UIUpdate.NoOp
                }
            }
        } as Flow<UIUpdate>
    }
}