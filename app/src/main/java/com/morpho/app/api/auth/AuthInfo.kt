package morpho.app.api.auth

import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle

@Serializable
data class AuthInfo(
    val accessJwt: String,
    val refreshJwt: String,
    val handle: Handle,
    val did: Did,
)
