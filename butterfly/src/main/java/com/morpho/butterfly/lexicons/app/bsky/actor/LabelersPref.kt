package app.bsky.actor

import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
//@SerialName("app.bsky.actor.defs#labelersPref")
public data class LabelersPref(
    public val labelers: ReadOnlyList<LabelerPrefItem>
)

@Serializable
//@SerialName("app.bsky.actor.defs#labelerPrefItem")
public data class LabelerPrefItem(
    public val did: Did
)