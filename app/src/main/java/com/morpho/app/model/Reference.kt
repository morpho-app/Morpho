package morpho.app.model

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.Cid

@Serializable
data class Reference(
    val uri: AtUri,
    val cid: Cid,
)