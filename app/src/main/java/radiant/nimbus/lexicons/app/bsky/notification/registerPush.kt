package app.bsky.notification

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did

@Serializable
public data class RegisterPushRequest(
  public val serviceDid: Did,
  public val token: String,
  public val platform: RegisterPushPlatform,
  public val appId: String,
)
