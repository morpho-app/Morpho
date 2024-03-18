package app.bsky.notification

import kotlinx.serialization.Serializable
import morpho.app.api.model.Timestamp

@Serializable
public data class UpdateSeenRequest(
  public val seenAt: Timestamp,
)
