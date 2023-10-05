package app.bsky.notification

import kotlinx.serialization.SerialName

public enum class RegisterPushPlatform {
  @SerialName("ios")
  IOS,
  @SerialName("android")
  ANDROID,
  @SerialName("web")
  WEB,
}
