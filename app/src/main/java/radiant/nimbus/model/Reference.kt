package radiant.nimbus.model

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid

@Serializable
data class Reference(
    val uri: AtUri,
    val cid: Cid,
)