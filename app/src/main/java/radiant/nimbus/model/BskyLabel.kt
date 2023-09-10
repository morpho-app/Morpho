package radiant.nimbus.model

import kotlinx.serialization.Serializable
import com.atproto.label.Label as AtProtoLabel

@Serializable
data class BskyLabel(
    val value: String,
)

fun AtProtoLabel.toLabel(): BskyLabel {
    return BskyLabel(
        value = `val`,
    )
}