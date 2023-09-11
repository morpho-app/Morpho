package radiant.nimbus.api.auth

import kotlinx.serialization.Serializable
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle

@Serializable
data class AuthInfo(
  val accessJwt: String,
  val refreshJwt: String,
  val handle: Handle,
  val did: Did,
)
