package app.bsky.unspecced

import com.atproto.label.Label
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class ApplyLabelsRequest(
  public val labels: ReadOnlyList<Label>,
)

