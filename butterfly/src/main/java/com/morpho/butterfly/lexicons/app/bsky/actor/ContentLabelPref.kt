package app.bsky.actor

import com.morpho.butterfly.Did
import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class ContentLabelPref(
  public val labelerDid: Did? = null,
  public val label: String,
  public val visibility: Visibility,
)
