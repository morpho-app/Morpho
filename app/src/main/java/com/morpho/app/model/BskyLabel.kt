package com.morpho.app.model

import app.bsky.actor.ContentLabelPref
import kotlinx.serialization.Serializable
import com.atproto.label.Label as AtProtoLabel

@Serializable
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