package app.bsky.notification

import kotlinx.serialization.SerialName

public enum class ListNotificationsReason {
  @SerialName("like")
  LIKE,
  @SerialName("repost")
  REPOST,
  @SerialName("follow")
  FOLLOW,
  @SerialName("mention")
  MENTION,
  @SerialName("reply")
  REPLY,
  @SerialName("quote")
  QUOTE,
}
