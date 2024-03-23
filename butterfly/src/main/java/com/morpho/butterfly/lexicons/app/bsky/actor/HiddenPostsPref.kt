package app.bsky.actor

import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
//@SerialName("app.bsky.actor.defs#hiddenPostsPref")
public data class HiddenPostsPref(
    val items: ReadOnlyList<AtUri>
)
