package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.ContentLabelPref
import com.atproto.label.Blurs
import com.atproto.label.DefaultSetting
import com.atproto.label.LabelValueDefinition
import com.atproto.label.Severity
import com.morpho.app.model.uidata.Moment
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import com.atproto.label.Label as AtProtoLabel

@Serializable
@Immutable
data class BskyLabel(
    val value: String,
) {
    override fun equals(other: Any?): Boolean {
        return when(other) {
            is ContentLabelPref -> {
                value == other.label
            }
            is String -> {
                value == other
            }
            else -> {
                this.hashCode() == other.hashCode()
            }
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

fun AtProtoLabel.toLabel(): BskyLabel {
    return BskyLabel(
        value = `val`,
    )
}

@Suppress("unused")
fun AtProtoLabel.toModLabel(): BskyModLabel {
    return BskyModLabel(
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


@Serializable
@Immutable
data class BskyModLabel @OptIn(ExperimentalSerializationApi::class) constructor(
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

        other as BskyModLabel

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

@Serializable
@Immutable
data class BskyModLabelDefinition(
    val identifier: String,
    val severity: Severity,
    val whatToHide: Blurs,
    val defaultSetting: DefaultSetting?,
    val adultOnly: Boolean?,
    val localizedName: String,
    val localizedDescription: String,
    val allDescriptions: ImmutableMap<Language, LocalizedLabelDescription>
)


fun LabelValueDefinition.toModLabelDef() :BskyModLabelDefinition {
    // currently just going to return the English name as the localizedName
    val localizedDefString = locales.first { it.lang == Language("en") }
    return BskyModLabelDefinition(
        identifier = identifier,
        severity = severity,
        whatToHide = blurs,
        defaultSetting = defaultSetting,
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