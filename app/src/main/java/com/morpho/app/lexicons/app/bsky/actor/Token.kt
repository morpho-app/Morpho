package app.bsky.actor

import kotlinx.serialization.SerialName

public enum class Visibility {
  @SerialName("show")
  SHOW,
  @SerialName("warn")
  WARN,
  @SerialName("hide")
  HIDE,
}

public enum class Sort {
  @SerialName("oldest")
  OLDEST,
  @SerialName("newest")
  NEWEST,
  @SerialName("most-likes")
  `MOST-LIKES`,
  @SerialName("random")
  RANDOM,
}
