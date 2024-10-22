@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.intl.Locale
import app.bsky.actor.Visibility
import com.atproto.label.Blurs
import com.atproto.label.DefaultSetting
import com.atproto.label.Label
import com.atproto.label.LabelValueDefinition
import com.atproto.label.LabelValues
import com.atproto.label.SelfLabel
import com.atproto.label.Severity
import com.morpho.app.model.uidata.MaybeMomentParceler
import com.morpho.app.model.uidata.Moment
import com.morpho.app.model.uidata.MomentParceler
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString

@OptIn(ExperimentalSerializationApi::class)
@Parcelize
@Serializable
@Immutable
data class BskyLabel(
    val version: Long?,
    val creator: Did,
    val uri: AtUri,
    val cid: Cid?,
    val value: String,
    val overwritesPrevious: Boolean?,
    @TypeParceler<Moment, MomentParceler>()
    val createdTimestamp: Moment,
    @TypeParceler<Moment?, MaybeMomentParceler>()
    val expirationTimestamp: Moment?,
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @ByteString
    val signature: ByteArray?,
): Parcelable {
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

    fun getLabelValue(): LabelValues? {
        return when (value) {
            LabelValues.PORN.value -> LabelValues.PORN
            LabelValues.GORE.value -> LabelValues.GORE
            LabelValues.NSFL.value -> LabelValues.NSFL
            LabelValues.SEXUAL.value -> LabelValues.SEXUAL
            LabelValues.GRAPHIC_MEDIA.value -> LabelValues.GRAPHIC_MEDIA
            LabelValues.NUDITY.value -> LabelValues.NUDITY
            LabelValues.DOXXING.value -> LabelValues.DOXXING
            LabelValues.DMCA_VIOLATION.value -> LabelValues.DMCA_VIOLATION
            LabelValues.NO_PROMOTE.value -> LabelValues.NO_PROMOTE
            LabelValues.NO_UNAUTHENTICATED.value -> LabelValues.NO_UNAUTHENTICATED
            LabelValues.WARN.value -> LabelValues.WARN
            LabelValues.HIDE.value -> LabelValues.HIDE
            else -> null
        }
    }
}

@Immutable
@Serializable
enum class LabelSetting {
    @SerialName("ignore")
    IGNORE,
    @SerialName("warn")
    WARN,
    @SerialName("hide")
    HIDE,
    @SerialName("show")
    SHOW,
    @SerialName("inform")
    INFORM,
}

fun DefaultSetting.toLabelSetting(): LabelSetting {
    return when (this) {
        DefaultSetting.IGNORE -> LabelSetting.IGNORE
        DefaultSetting.WARN -> LabelSetting.WARN
        DefaultSetting.HIDE -> LabelSetting.HIDE
        DefaultSetting.SHOW -> LabelSetting.SHOW
        DefaultSetting.INFORM -> LabelSetting.INFORM
    }

}

fun Visibility.toLabelSetting(): LabelSetting {
    return when (this) {
        Visibility.SHOW -> LabelSetting.SHOW
        Visibility.WARN -> LabelSetting.WARN
        Visibility.HIDE -> LabelSetting.HIDE
        Visibility.IGNORE -> LabelSetting.IGNORE
        Visibility.INFORM -> LabelSetting.INFORM
    }
}

@Parcelize
@Serializable
@Immutable
data class BskyLabelDefinition(
    val identifier: String,
    val severity: Severity,
    val whatToHide: Blurs,
    val defaultSetting: LabelSetting?,
    val adultOnly: Boolean?,
    val localizedName: String,
    val localizedDescription: String,
    val allDescriptions: ImmutableMap<Language, LocalizedLabelDescription>
): Parcelable {
    fun getVisibility(): Visibility {
        return when(defaultSetting)  {
            LabelSetting.IGNORE -> Visibility.IGNORE
            LabelSetting.WARN -> Visibility.WARN
            LabelSetting.HIDE -> Visibility.HIDE
            LabelSetting.SHOW -> Visibility.SHOW
            LabelSetting.INFORM -> Visibility.INFORM
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
        whatToHide = blurs,
        defaultSetting = defaultSetting?.toLabelSetting(),
        adultOnly = adultOnly,
        localizedName = localizedDefString.name,
        localizedDescription = localizedDefString.description,
        allDescriptions = locales.associate { it.lang to LocalizedLabelDescription(it.name, it.description) }.toImmutableMap()
    )
}


@Parcelize
@Serializable
@Immutable
data class LocalizedLabelDescription(
    val localizedName: String,
    val localizedDescription: String,
): Parcelable

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