package app.bsky.actor

import app.bsky.actor.LabelerPrefItem
import com.morpho.butterfly.MutedWordTarget
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable
//@SerialName("app.bsky.actor.defs#mutedWordsPref")
public data class MutedWordsPref(
    public val items: ReadOnlyList<MutedWord>
)

@Serializable
//@SerialName("app.bsky.actor.defs#mutedWord")
public data class MutedWord(
    public val value: String,
    public val targets: ReadOnlyList<MutedWordTarget>
)

