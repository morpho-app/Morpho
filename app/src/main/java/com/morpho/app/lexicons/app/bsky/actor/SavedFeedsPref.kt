package app.bsky.actor

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class SavedFeedsPref(
  public val pinned: ReadOnlyList<AtUri>,
  public val saved: ReadOnlyList<AtUri>,
)
