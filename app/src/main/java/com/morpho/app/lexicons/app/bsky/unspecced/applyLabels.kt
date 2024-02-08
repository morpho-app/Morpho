package app.bsky.unspecced

import com.atproto.label.Label
import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class ApplyLabelsRequest(
  public val labels: ReadOnlyList<Label>,
)
