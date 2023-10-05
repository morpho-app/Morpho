package app.bsky.unspecced

import com.atproto.label.Label
import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class ApplyLabelsRequest(
  public val labels: ReadOnlyList<Label>,
)
