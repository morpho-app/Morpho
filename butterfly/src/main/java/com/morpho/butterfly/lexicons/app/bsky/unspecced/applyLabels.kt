package app.bsky.unspecced

import com.atproto.label.Label
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList
import io.ktor.resources.Resource

@Serializable
public data class ApplyLabelsRequest(
  public val labels: ReadOnlyList<Label>,
)

@Resource("/xrpc/app.bsky.unspecced.applyLabels")
class ApplyLabels(val labels: ReadOnlyList<Label>)
