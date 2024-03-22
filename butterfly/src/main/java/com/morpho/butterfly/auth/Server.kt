package com.morpho.butterfly.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Server {
  val host: String

  @Serializable
  @SerialName("bluesky-social")
  object BlueskySocial : Server {
    override val host: String = "https://bsky.social"
  }

  @Serializable
  @SerialName("bluesky-appview")
  object BlueskyAppview : Server {
    override val host: String = "https://public.api.bsky.app"
  }

  @Serializable
  @SerialName("custom-server")
  data class CustomServer(
    override val host: String,
  ) : Server
}
