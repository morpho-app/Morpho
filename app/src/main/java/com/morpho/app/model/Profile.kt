package morpho.app.model

import androidx.annotation.Size
import app.bsky.actor.ProfileView
import app.bsky.actor.ProfileViewBasic
import app.bsky.actor.ProfileViewDetailed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle
import morpho.app.util.mapImmutable


enum class ProfileType {
    Basic,
    Standard,
    Detailed
}

@Serializable
sealed interface Profile {
    val did: Did
    val handle: Handle
    val displayName: String?
    val avatar: String?
    val mutedByMe: Boolean
    val followingMe: Boolean
    val followedByMe: Boolean
    val labels: ImmutableList<BskyLabel>
}


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
    override val labels: ImmutableList<BskyLabel>,
) : Profile

@Serializable
data class DetailedProfile(
    override val did: Did,// = Did(""),
    override val handle: Handle,// = Handle(""),
    override val displayName: String?,
    @Size(max = 2560) var description: String?,
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
    override val labels: ImmutableList<BskyLabel>,
) : Profile

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