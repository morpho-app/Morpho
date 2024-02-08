package morpho.app.api.response

import kotlinx.serialization.Serializable

@Serializable
data class AtpErrorDescription(
  val error: String?,
  val message: String?,
)
