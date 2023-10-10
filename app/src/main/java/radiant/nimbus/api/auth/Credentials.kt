package radiant.nimbus.api.auth

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Handle

@Serializable
data class Credentials constructor(
    val email: String?,
    val username: Handle,
    val password: String,
    val inviteCode: String?,
) {
  override fun toString(): String {
    return "Credentials(" +
        "email='$email', " +
        "username='$username', " +
        "password='███', " +
        "inviteCode='$inviteCode'" +
        ")"
  }
}
