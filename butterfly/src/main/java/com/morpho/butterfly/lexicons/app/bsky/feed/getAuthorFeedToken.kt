package app.bsky.feed

import kotlinx.serialization.SerialName

public enum class GetAuthorFeedFilter(val n: String) {
  @SerialName("posts_with_replies")
  POSTS_WITH_REPLIES("posts_with_replies"),
  @SerialName("posts_no_replies")
  POSTS_NO_REPLIES("posts_no_replies"),
  @SerialName("posts_with_media")
  POSTS_WITH_MEDIA("posts_with_media"),
}
