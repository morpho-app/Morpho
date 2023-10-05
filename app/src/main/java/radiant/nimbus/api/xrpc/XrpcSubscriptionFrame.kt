package radiant.nimbus.api.xrpc

import kotlinx.serialization.Serializable

@Serializable
internal data class XrpcSubscriptionFrame(
  val op: Int,
  val t: String?,
)
