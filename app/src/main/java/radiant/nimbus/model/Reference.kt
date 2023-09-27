package radiant.nimbus.model

import kotlinx.serialization.Serializable
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid

@Serializable
data class Reference(
    val uri: AtUri,
    val cid: Cid,
)