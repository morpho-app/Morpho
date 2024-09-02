@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.intl.Locale
import app.bsky.actor.Visibility
import com.atproto.label.*
import com.morpho.app.model.uidata.Moment
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.Clock
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

    fun getLabelValue(): LabelValue? {
        return when (value) {
            LabelValue.PORN.value -> LabelValue.PORN
            LabelValue.GORE.value -> LabelValue.GORE
            LabelValue.NSFL.value -> LabelValue.NSFL
            LabelValue.SEXUAL.value -> LabelValue.SEXUAL
            LabelValue.GRAPHIC_MEDIA.value -> LabelValue.GRAPHIC_MEDIA
            LabelValue.NUDITY.value -> LabelValue.NUDITY
            LabelValue.DOXXING.value -> LabelValue.DOXXING
            LabelValue.DMCA_VIOLATION.value -> LabelValue.DMCA_VIOLATION
            LabelValue.NO_PROMOTE.value -> LabelValue.NO_PROMOTE
            LabelValue.NO_UNAUTHENTICATED.value -> LabelValue.NO_UNAUTHENTICATED
            LabelValue.WARN.value -> LabelValue.WARN
            LabelValue.HIDE.value -> LabelValue.HIDE
            else -> null
        }
    }
}

@Serializable
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

@Serializable
enum class LabelAction {
    Blur,
    Alert,
    Inform,
    None
}

@Serializable
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
    fun forScope(scope: LabelScope, target: LabelTarget): List<LabelAction> {
        return when (target) {
            LabelTarget.Account -> when (scope) {
                LabelScope.Content -> listOf(
                    account.contentList, account.contentView, account.avatar,
                    account.banner, account.profileList, account.profileView,
                    account.displayName
                )
                LabelScope.Media -> listOf(account.contentMedia, account.avatar, account.banner)
                LabelScope.None -> listOf()
            }
            LabelTarget.Profile -> when (scope) {
                LabelScope.Content -> listOf(profile.contentList, profile.contentView, profile.displayName)
                LabelScope.Media -> listOf(profile.avatar, profile.banner, profile.contentMedia)
                LabelScope.None -> listOf()
            }
            LabelTarget.Content -> when (scope) {
                LabelScope.Content -> listOf(content.contentList, content.contentView)
                LabelScope.Media -> listOf(
                    content.contentMedia,
                    content.avatar,
                    content.banner
                )

                LabelScope.None -> listOf()
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

}


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

data object InappropriateMediaBehaviour: ModBehaviour(
    contentMedia = LabelAction.Blur,
)

data object InappropriateAvatarBehaviour: ModBehaviour(
    avatar = LabelAction.Blur,
)

data object InappropriateBannerBehaviour: ModBehaviour(
    banner = LabelAction.Blur,
)

data object InappropriateDisplayNameBehaviour: ModBehaviour(
    displayName = LabelAction.Blur,
)

val BlurAllMedia = ModBehaviours(
    content = InappropriateMediaBehaviour,
    profile = ModBehaviour(
        avatar = LabelAction.Blur,
        banner = LabelAction.Blur,
        contentMedia = LabelAction.Blur,
    ),
    account = ModBehaviour(
        avatar = LabelAction.Blur,
        banner = LabelAction.Blur,
        contentMedia = LabelAction.Blur,
    ),
)



data object NoopBehaviour: ModBehaviour()

@Serializable
enum class LabelValueDefFlag {
    NoOverride,
    Adult,
    Unauthed,
    NoSelf,
}

@Serializable
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

fun Visibility.toLabelSetting(): LabelSetting {
    return when (this) {
        Visibility.SHOW -> LabelSetting.IGNORE
        Visibility.WARN -> LabelSetting.WARN
        Visibility.HIDE -> LabelSetting.HIDE
        Visibility.IGNORE -> LabelSetting.IGNORE
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
) {
    fun getVisibility(): Visibility {
        return when(defaultSetting)  {
            LabelSetting.IGNORE -> Visibility.SHOW
            LabelSetting.WARN -> Visibility.WARN
            LabelSetting.HIDE -> Visibility.HIDE
            null -> Visibility.IGNORE
        }
    }
}


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

fun SelfLabel.toLabel(did: Did): BskyLabel {
    return BskyLabel(
        value = `val`,
        version = 0,
        creator = did,
        // TODO: this is a hack, make sure this doesn't go over the wire
        uri = AtUri("at://$did/selfLabel/${`val`}"),
        cid = null,
        overwritesPrevious = null,
        createdTimestamp = Moment(Clock.System.now()),
        expirationTimestamp = null,
        signature = null,
    )
}

fun BskyLabel.toSelfLabel(): SelfLabel {
    return SelfLabel(
        `val` = value
    )
}

fun BskyLabel.toAtProtoLabel(): com.atproto.label.Label {
    return Label(
        `val` = value,
        ver = version,
        src = creator,
        uri = uri,
        cid = cid,
        neg = overwritesPrevious,
        cts = createdTimestamp.instant,
        exp = expirationTimestamp?.instant,
        sig = signature,
    )
}