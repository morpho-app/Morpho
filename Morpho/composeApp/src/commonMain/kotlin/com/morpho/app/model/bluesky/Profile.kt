package com.morpho.app.model.bluesky


import androidx.compose.runtime.Immutable
import app.bsky.actor.*
import com.morpho.app.model.uidata.Moment
import com.morpho.app.util.JavaSerializable
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class ProfileType {
    Basic,
    Detailed,
    Service
}

@Immutable
@Serializable
sealed interface Profile: JavaSerializable {
    val did: Did
    val handle: Handle
    val displayName: String?
    val avatar: String?
    val mutedByMe: Boolean
    val mutedByList: UserListBasic?
    val block: BlockRecord?
    val blockedBy: Boolean
    val blockingByList: UserListBasic?
    val following: FollowRecord?
    val followedBy: FollowRecord?
    val numKnownFollowers: Long
    val knownFollowers: List<Profile>
    @Serializable
    val labels: List<BskyLabel>
    val associated: ProfileAssociated?
    val createdAt: Moment?
    val indexedAt: Moment?
    val type: ProfileType
        get() = when (this) {
            is BasicProfile -> ProfileType.Basic
            is DetailedProfile -> ProfileType.Detailed
            is BskyLabelService -> ProfileType.Service
        }
    val blocking: Boolean
        get() = block != null
    val followingMe: Boolean
        get() = following != null
    val followedByMe: Boolean
        get() = followedBy != null
}

@Immutable
@Serializable
data class BlockRecord(val uri: AtUri)

@Immutable
@Serializable
data class FollowRecord(val uri: AtUri)

@Immutable
@Serializable
data class BasicProfile(
    override val did: Did,// = Did(""),
    override val handle: Handle,// = Handle(""),
    override val displayName: String?,
    override val avatar: String?,
    override val mutedByMe: Boolean,
    override val following: FollowRecord?,
    override val followedBy: FollowRecord?,
    @Serializable
    override val labels: List<BskyLabel>,
    override val mutedByList: UserListBasic?,
    override val block: BlockRecord?,
    override val blockedBy: Boolean,
    override val blockingByList: UserListBasic?,
    override val numKnownFollowers: Long,
    override val knownFollowers: List<Profile>,
    override val associated: ProfileAssociated?,
    override val createdAt: Moment?,

) : Profile {
    override val indexedAt: Moment? = null
}

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
    override val createdAt: Moment?,
    override val indexedAt: Moment?,
    override val mutedByMe: Boolean,
    override val following: FollowRecord?,
    override val followedBy: FollowRecord?,
    @Serializable
    override val labels: List<BskyLabel>,
    override val mutedByList: UserListBasic?,
    override val block: BlockRecord?,
    override val blockedBy: Boolean,
    override val blockingByList: UserListBasic?,
    override val numKnownFollowers: Long,
    override val knownFollowers: List<Profile>,
    override val associated: ProfileAssociated?,
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
            following = following,
            followedBy = followedBy,
            labels = labels,
            mutedByList = mutedByList,
            block = block,
            blockedBy = blockedBy,
            blockingByList = blockingByList,
            numKnownFollowers = numKnownFollowers,
            knownFollowers = knownFollowers,
            associated = associated,
            createdAt = createdAt,
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
    val createdAt: Moment?,
    val mutedByMe: Boolean,
    val following: FollowRecord?,
    val followedBy: FollowRecord?,
    @Serializable
    val labels: List<BskyLabel>,
    val mutedByList: UserListBasic?,
    val block: BlockRecord?,
    val blockedBy: Boolean,
    val blockingByList: UserListBasic?,
    val numKnownFollowers: Long,
    val knownFollowers: List<Profile>,
    val associated: ProfileAssociated?,
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
            following = following,
            followedBy = followedBy,
            labels = labels.toImmutableList(),
            mutedByList = mutedByList,
            block = block,
            blockedBy = blockedBy,
            blockingByList = blockingByList,
            numKnownFollowers = numKnownFollowers,
            knownFollowers = knownFollowers.toImmutableList(),
            associated = associated,
            createdAt = createdAt,
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
        following = viewer?.following?.let { FollowRecord(it) },
        followedBy = viewer?.followedBy?.let { FollowRecord(it) },
        labels = labels.mapImmutable { it.toLabel() },
        mutedByList = viewer?.mutedByList?.toList(),
        block = viewer?.blocking?.let { BlockRecord(it) },
        blockedBy = viewer?.blockedBy == true,
        blockingByList = viewer?.blockingByList?.toList(),
        numKnownFollowers = viewer?.knownFollowers?.count ?: 0,
        knownFollowers = viewer?.knownFollowers?.followers?.mapImmutable { it.toProfile() }?.toList() ?: listOf(),
        associated = associated,
        createdAt = createdAt?.let(::Moment),
    )
}

fun ProfileViewBasic.toProfile(): Profile {
    return BasicProfile(
        did = did,
        handle = handle,
        displayName = displayName,
        avatar = avatar,
        mutedByMe = viewer?.muted == true,
        following = viewer?.following?.let { FollowRecord(it) },
        followedBy = viewer?.followedBy?.let { FollowRecord(it) },
        labels = labels.mapImmutable { it.toLabel() },
        mutedByList = viewer?.mutedByList?.toList(),
        block = viewer?.blocking?.let { BlockRecord(it) },
        blockedBy = viewer?.blockedBy == true,
        blockingByList = viewer?.blockingByList?.toList(),
        numKnownFollowers = viewer?.knownFollowers?.count ?: 0,
        knownFollowers = viewer?.knownFollowers?.followers?.mapImmutable { it.toProfile() }?.toList() ?: listOf(),
        associated = associated,
        createdAt = createdAt?.let(::Moment),
    )
}

fun ProfileView.toProfile(): Profile {
    return BasicProfile(
        did = did,
        handle = handle,
        displayName = displayName,
        avatar = avatar,
        mutedByMe = viewer?.muted == true,
        following = viewer?.following?.let { FollowRecord(it) },
        followedBy = viewer?.followedBy?.let { FollowRecord(it) },
        labels = labels.mapImmutable { it.toLabel() },
        mutedByList = viewer?.mutedByList?.toList(),
        block = viewer?.blocking?.let { BlockRecord(it) },
        blockedBy = viewer?.blockedBy == true,
        blockingByList = viewer?.blockingByList?.toList(),
        numKnownFollowers = viewer?.knownFollowers?.count ?: 0,
        knownFollowers = viewer?.knownFollowers?.followers?.mapImmutable { it.toProfile() }?.toList() ?: listOf(),
        associated = associated,
        createdAt = createdAt?.let(::Moment),
    )
}

fun Profile.toProfileViewBasic(): ProfileViewBasic {
    return ProfileViewBasic(
        did = did,
        handle = handle,
        displayName = displayName,
        avatar = avatar,
        viewer = ViewerState(
            muted = mutedByMe,
            mutedByList = mutedByList?.toListVewBasic(),
            blockedBy = blockedBy,
            blockingByList = blockingByList?.toListVewBasic(),
            blocking = block?.uri,
            following = following?.uri,
            followedBy = followedBy?.uri,
            knownFollowers = KnownFollowers(
                count = numKnownFollowers,
                followers = knownFollowers.map { it.toProfileViewBasic() }.toImmutableList()
            )
        ),
        labels = labels.mapImmutable { it.toAtProtoLabel() },

    )
}