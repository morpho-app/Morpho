package app.bsky.notification

import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class UpdateSeenRequest(
  public val seenAt: Timestamp,
)
