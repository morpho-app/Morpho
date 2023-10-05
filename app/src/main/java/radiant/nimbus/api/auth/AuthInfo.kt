package radiant.nimbus.api.auth

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.Handle

@Serializable
data class AuthInfo(
    val accessJwt: String,
    val refreshJwt: String,
    val handle: Handle,
    val did: Did,
)
