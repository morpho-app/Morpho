@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.util.fastMap
import com.atproto.label.*
import com.morpho.app.model.uidata.Moment
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language
import kotlinx.collections.immutable.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString


@Serializable
@Immutable
data class BskyLabel @OptIn(ExperimentalSerializationApi::class) constructor(
    val version: Long?,
    val creator: Did,
    val uri: AtUri,
    val cid: Cid?,
    val value: String,
    val overwritesPrevious: Boolean?,
    val createdTimestamp: Moment,
    val expirationTimestamp: Moment?,
    @ByteString
    val signature: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BskyLabel

        if (version != other.version) return false
        if (creator != other.creator) return false
        if (uri != other.uri) return false
        if (cid != other.cid) return false
        if (value != other.value) return false
        if (overwritesPrevious != other.overwritesPrevious) return false
        if (createdTimestamp != other.createdTimestamp) return false
        if (expirationTimestamp != other.expirationTimestamp) return false
        if (signature != null) {
            if (other.signature == null) return false
            if (!signature.contentEquals(other.signature)) return false
        } else if (other.signature != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version?.hashCode() ?: 0
        result = 31 * result + creator.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + (cid?.hashCode() ?: 0)
        result = 31 * result + value.hashCode()
        result = 31 * result + (overwritesPrevious?.hashCode() ?: 0)
        result = 31 * result + createdTimestamp.hashCode()
        result = 31 * result + (expirationTimestamp?.hashCode() ?: 0)
        result = 31 * result + (signature?.contentHashCode() ?: 0)
        return result
    }
}

enum class LabelScope {
    Content,
    Media,
    None,
}

fun Blurs.toScope(): LabelScope {
    return when (this) {
        Blurs.CONTENT -> LabelScope.Content
        Blurs.MEDIA -> LabelScope.Media
        Blurs.NONE -> LabelScope.None
    }
}

enum class LabelAction {
    Blur,
    Alert,
    Inform,
    None
}

enum class LabelTarget {
    Account,
    Profile,
    Content
}

@Serializable
open class ModBehaviour(
    val profileList: LabelAction = LabelAction.None,
    val profileView: LabelAction = LabelAction.None,
    val avatar: LabelAction = LabelAction.None,
    val banner: LabelAction = LabelAction.None,
    val displayName: LabelAction = LabelAction.None,
    val contentList: LabelAction = LabelAction.None,
    val contentView: LabelAction = LabelAction.None,
    val contentMedia: LabelAction = LabelAction.None,
) {
    init {
        require(avatar != LabelAction.Inform)
        require(banner != LabelAction.Inform && banner != LabelAction.Alert)
        require(displayName != LabelAction.Inform && displayName != LabelAction.Alert)
        require(contentMedia != LabelAction.Inform && contentMedia != LabelAction.Alert)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ModBehaviour

        if (profileList != other.profileList) return false
        if (profileView != other.profileView) return false
        if (avatar != other.avatar) return false
        if (banner != other.banner) return false
        if (displayName != other.displayName) return false
        if (contentList != other.contentList) return false
        if (contentView != other.contentView) return false
        if (contentMedia != other.contentMedia) return false

        return true
    }

    override fun hashCode(): Int {
        var result = profileList.hashCode()
        result = 31 * result + profileView.hashCode()
        result = 31 * result + avatar.hashCode()
        result = 31 * result + banner.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + contentList.hashCode()
        result = 31 * result + contentView.hashCode()
        result = 31 * result + contentMedia.hashCode()
        return result
    }
}

@Serializable
data class ModBehaviours(
    val account: ModBehaviour = ModBehaviour(),
    val profile: ModBehaviour = ModBehaviour(),
    val content: ModBehaviour = ModBehaviour(),
) {
    fun forScope(scope: LabelScope, target: LabelTarget): ImmutableList<LabelAction> {
        return when (target) {
            LabelTarget.Account -> when (scope) {
                LabelScope.Content -> persistentListOf(
                    account.contentList, account.contentView, account.avatar,
                    account.banner, account.profileList, account.profileView,
                    account.displayName
                )
                LabelScope.Media -> persistentListOf(account.contentMedia, account.avatar, account.banner)
                LabelScope.None -> persistentListOf()
            }
            LabelTarget.Profile -> when (scope) {
                LabelScope.Content -> persistentListOf(profile.contentList, profile.contentView, profile.displayName)
                LabelScope.Media -> persistentListOf(profile.avatar, profile.banner, profile.contentMedia)
                LabelScope.None -> persistentListOf()
            }
            LabelTarget.Content -> when (scope) {
                LabelScope.Content -> persistentListOf(content.contentList, content.contentView)
                LabelScope.Media -> persistentListOf(
                    content.contentMedia,
                    content.avatar,
                    content.banner
                )

                LabelScope.None -> persistentListOf()
            }
        }
    }
}

@Immutable
@Serializable
open class DescribedBehaviours(
    val behaviours: ModBehaviours,
    val label: String,
    val description: String,
){
    fun describeAction(scope: LabelScope, target: LabelTarget) : ImmutableList<DescribedAction> {
        return behaviours.forScope(scope, target).fastMap { DescribedAction(it, label, description) }.toImmutableList()
    }
}

@Immutable
@Serializable
data class DescribedAction(
    val action: LabelAction,
    val label: String,
    val description: String,
)

data object MutePersonDescribed: DescribedBehaviours(
    behaviours = ModBehaviours(
        account = MuteBehaviour,
        profile = MuteBehaviour,
        content = MuteBehaviour,
    ),
    label = "Mute",
    description = "You have muted this person",
)

data object NoDescribed: DescribedBehaviours(
    behaviours = ModBehaviours(
        account = NoopBehaviour,
        profile = NoopBehaviour,
        content = NoopBehaviour,
    ),
    label = "No",
    description = "No action taken",
)

@Serializable
data object BlockBehaviour: ModBehaviour(
    profileList = LabelAction.Blur,
    profileView = LabelAction.Blur,
    avatar = LabelAction.Blur,
    banner = LabelAction.Blur,
    contentList = LabelAction.Blur,
    contentView = LabelAction.Blur,
)

data object MuteBehaviour: ModBehaviour(
    profileList = LabelAction.Inform,
    profileView = LabelAction.Alert,
    contentList = LabelAction.Blur,
    contentView = LabelAction.Inform,
)

data object MuteWordBehaviour: ModBehaviour(
    contentList = LabelAction.Blur,
    contentView = LabelAction.Blur,
)

data object HideBehaviour: ModBehaviour(
    contentList = LabelAction.Blur,
    contentView = LabelAction.Blur,
)

data object NoopBehaviour: ModBehaviour()

enum class LabelValueDefFlag {
    NoOverride,
    Adult,
    Unauthed,
    NoSelf,
}

enum class LabelSetting {
    @SerialName("ignore")
    IGNORE,
    @SerialName("warn")
    WARN,
    @SerialName("hide")
    HIDE,
}

fun DefaultSetting.toLabelSetting(): LabelSetting {
    return when (this) {
        DefaultSetting.IGNORE -> LabelSetting.IGNORE
        DefaultSetting.WARN -> LabelSetting.WARN
        DefaultSetting.HIDE -> LabelSetting.HIDE
    }

}

@Serializable
@Immutable
data class BskyLabelDefinition(
    val identifier: String,
    val severity: Severity,
    val whatToHide: LabelScope,
    val defaultSetting: LabelSetting?,
    val adultOnly: Boolean?,
    val localizedName: String,
    val localizedDescription: String,
    val allDescriptions: ImmutableMap<Language, LocalizedLabelDescription>
)


fun LabelValueDefinition.toModLabelDef() :BskyLabelDefinition {
    var localizedDefString = locales.firstOrNull { it.lang == Language(Locale.current.language) }
    if (localizedDefString == null) {
        // fall back to english if the current language is not available
        localizedDefString = locales.firstOrNull { it.lang == Language("en") }
    }
    if (localizedDefString == null) {
        // fall back to whatever language we DO have if English isn't available
        localizedDefString = locales.first()
    }
    return BskyLabelDefinition(
        identifier = identifier,
        severity = severity,
        whatToHide = blurs.toScope(),
        defaultSetting = defaultSetting?.toLabelSetting(),
        adultOnly = adultOnly,
        localizedName = localizedDefString.name,
        localizedDescription = localizedDefString.description,
        allDescriptions = locales.associate { it.lang to LocalizedLabelDescription(it.name, it.description) }.toImmutableMap()
    )
}


@Serializable
@Immutable
data class LocalizedLabelDescription(
    val localizedName: String,
    val localizedDescription: String,
)

@Suppress("unused")
fun Label.toLabel(): BskyLabel {
    return BskyLabel(
        value = `val`,
        version = ver,
        creator = src,
        uri = uri,
        cid = cid,
        overwritesPrevious = neg,
        createdTimestamp = Moment(cts),
        expirationTimestamp = exp?.let { Moment(it) },
        signature = sig,
    )
}
