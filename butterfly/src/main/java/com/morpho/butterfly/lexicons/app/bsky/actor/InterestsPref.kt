package app.bsky.actor

import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
//@SerialName("app.bsky.actor.defs#interetsPref")
data class InterestsPref(
    val tags: ReadOnlyList<String>,
)
