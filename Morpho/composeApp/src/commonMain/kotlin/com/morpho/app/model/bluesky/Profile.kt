package com.morpho.app.model.bluesky


import androidx.compose.runtime.Immutable
import app.bsky.actor.*
import com.morpho.app.model.uidata.MaybeMomentParceler
import com.morpho.app.model.uidata.Moment
import com.morpho.app.model.uidata.MomentParceler
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class ProfileType {
    Basic,
    Detailed,
    Service
}

@Parcelize
@Immutable
@Serializable
sealed interface Profile: Parcelable {
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
    val associated: BskyProfileAssociated?
    @TypeParceler<Moment, MomentParceler>()
    val createdAt: Moment?
    @TypeParceler<Moment, MomentParceler>()
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

@Parcelize
@Immutable
@Serializable
data class BlockRecord(val uri: AtUri): Parcelable

@Parcelize
@Immutable
@Serializable
data class FollowRecord(val uri: AtUri): Parcelable

@Parcelize
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
    override val associated: BskyProfileAssociated?,
    @TypeParceler<Moment?, MaybeMomentParceler>()
    override val createdAt: Moment?,

    ) : Profile, Parcelable{
    @TypeParceler<Moment?, MaybeMomentParceler>()
    override val indexedAt: Moment? = null
}

@Parcelize
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
    @TypeParceler<Moment?, MaybeMomentParceler>()
    override val createdAt: Moment?,
    @TypeParceler<Moment?, MaybeMomentParceler>()
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
    override val associated: BskyProfileAssociated?,
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

@Parcelize
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
    @TypeParceler<Moment?, MaybeMomentParceler>()
    val indexedAt: Moment?,
    @TypeParceler<Moment?, MaybeMomentParceler>()
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
    val associated: BskyProfileAssociated?,
): Parcelable {
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
        associated = associated?.toBskyProfileAssociated(),
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
        associated = associated?.toBskyProfileAssociated(),
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
        associated = associated?.toBskyProfileAssociated(),
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

fun ProfileAssociated.toBskyProfileAssociated(): BskyProfileAssociated {
    return BskyProfileAssociated(
        lists = this.lists,
        feedGens = this.feedGens,
        labeler = this.labeler,
        starterPacks = this.starterPacks,
        chat = this.chat?.toProfileAssociatedChat()
    )
}
@Immutable
@Parcelize
@Serializable
public data class BskyProfileAssociated(
    public val lists: Long? = null,
    public val feedGens: Long? = null,
    public val labeler: Boolean? = null,
    public val starterPacks: Long? = null,
    public val chat: BskyProfileAssociatedChat? = null,
): Parcelable

fun ProfileAssociatedChat.toProfileAssociatedChat(): BskyProfileAssociatedChat {
    return BskyProfileAssociatedChat(
        allowIncoming = this.allowIncoming
    )
}

@Parcelize
@Immutable
@Serializable
public data class BskyProfileAssociatedChat(
    public val allowIncoming: AllowIncoming,
): Parcelable