package app.bsky.actor

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class SavedFeedsPref(
  public val pinned: ReadOnlyList<AtUri>,
  public val saved: ReadOnlyList<AtUri>,
)
