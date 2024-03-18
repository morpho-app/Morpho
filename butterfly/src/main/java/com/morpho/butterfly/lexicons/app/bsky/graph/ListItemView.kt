package app.bsky.graph

import app.bsky.actor.ProfileView
import kotlinx.serialization.Serializable

@Serializable
public data class ListItemView(
  public val subject: ProfileView,
)
