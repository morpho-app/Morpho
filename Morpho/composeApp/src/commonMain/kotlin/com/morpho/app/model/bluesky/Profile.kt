package com.morpho.app.model.bluesky


import androidx.compose.runtime.Immutable
import app.bsky.actor.ProfileView
import app.bsky.actor.ProfileViewBasic
import app.bsky.actor.ProfileViewDetailed
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable


enum class ProfileType {
    Basic,
    Detailed,
    Service
}

@Immutable
@Serializable
sealed interface Profile {
    val did: Did
    val handle: Handle
    val displayName: String?
    val avatar: String?
    val mutedByMe: Boolean
    val followingMe: Boolean
    val followedByMe: Boolean
    @Serializable
    val labels: List<BskyLabel>
    val type: ProfileType
        get() = when (this) {
            is BasicProfile -> ProfileType.Basic
            is DetailedProfile -> ProfileType.Detailed
            is BskyLabelService -> ProfileType.Service
        }
}


@Immutable
@Serializable
data class BasicProfile(
    override val did: Did,// = Did(""),
    override val handle: Handle,// = Handle(""),
    override val displayName: String?,
    override val avatar: String?,
    override val mutedByMe: Boolean,
    override val followingMe: Boolean,
    override val followedByMe: Boolean,
    @Serializable
    override val labels: List<BskyLabel>,
) : Profile

@Immutable
@Serializable
data class DetailedProfile(
    override val did: Did,// = Did(""),
    override val handle: Handle,// = Handle(""),
    override val displayName: String?,
    var description: String?,
    override val avatar: String?,
    val banner: String?,
    val followersCount: Long,
    val followsCount: Long,
    val postsCount: Long,
    val indexedAt: Moment?,
    override val mutedByMe: Boolean,
    override val followingMe: Boolean,
    override val followedByMe: Boolean,
    @Serializable
    override val labels: List<BskyLabel>,
) : Profile {
    fun toSerializableProfile(): SerializableProfile {
        return SerializableProfile(
            did = did,
            handle = handle,
            displayName = displayName,
            description = description,
            avatar = avatar,
            banner = banner ?: "",
            followersCount = followersCount,
            followsCount = followsCount,
            postsCount = postsCount,
            indexedAt = indexedAt,
            mutedByMe = mutedByMe,
            followingMe = followingMe,
            followedByMe = followedByMe,
            labels = labels,
        )
    }

}

@Immutable
@Serializable
data class SerializableProfile(
    val did: Did,// = Did(""),
    val handle: Handle,// = Handle(""),
    val displayName: String?,
    var description: String?,
    val avatar: String?,
    val banner: String?,
    val followersCount: Long,
    val followsCount: Long,
    val postsCount: Long,
    val indexedAt: Moment?,
    val mutedByMe: Boolean,
    val followingMe: Boolean,
    val followedByMe: Boolean,
    @Serializable
    val labels: List<BskyLabel>,
) {
    val type: ProfileType
        get() = ProfileType.Detailed
    fun toProfile(): DetailedProfile {
        return DetailedProfile(
            did = did,
            handle = handle,
            displayName = displayName,
            description = description,
            avatar = avatar,
            banner = banner,
            followersCount = followersCount,
            followsCount = followsCount,
            postsCount = postsCount,
            indexedAt = indexedAt,
            mutedByMe = mutedByMe,
            followingMe = followingMe,
            followedByMe = followedByMe,
            labels = labels.toImmutableList(),
        )
    }

}

fun ProfileViewDetailed.toProfile(): DetailedProfile {
    return DetailedProfile(
        did = did,
        handle = handle,
        displayName = displayName,
        description = description,
        avatar = avatar,
        banner = banner,
        followersCount = followersCount ?: 0,
        followsCount = followsCount ?: 0,
        postsCount = postsCount ?: 0,
        indexedAt = indexedAt?.let(::Moment),
        mutedByMe = viewer?.muted == true,
        followingMe = viewer?.followedBy != null,
        followedByMe = viewer?.following != null,
        labels = labels.mapImmutable { it.toLabel() },
    )
}

fun ProfileViewBasic.toProfile(): Profile {
    return BasicProfile(
        did = did,
        handle = handle,
        displayName = displayName,
        avatar = avatar,
        mutedByMe = viewer?.muted == true,
        followingMe = viewer?.followedBy != null,
        followedByMe = viewer?.following != null,
        labels = labels.mapImmutable { it.toLabel() },
    )
}

fun ProfileView.toProfile(): Profile {
    return BasicProfile(
        did = did,
        handle = handle,
        displayName = displayName,
        avatar = avatar,
        mutedByMe = viewer?.muted == true,
        followingMe = viewer?.followedBy != null,
        followedByMe = viewer?.following != null,
        labels = labels.mapImmutable { it.toLabel() },
    )
}