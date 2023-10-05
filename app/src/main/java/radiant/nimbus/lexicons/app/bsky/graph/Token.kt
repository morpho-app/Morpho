package app.bsky.graph

import kotlinx.serialization.SerialName

public enum class Token {
  @SerialName("app.bsky.graph.defs#modlist")
  MODLIST,
  @SerialName("app.bsky.graph.defs#curatelist")
  CURATELIST,
}
