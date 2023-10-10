package app.bsky.feed

import kotlinx.serialization.SerialName

public enum class GetAuthorFeedFilter {
  @SerialName("posts_with_replies")
  POSTS_WITH_REPLIES,
  @SerialName("posts_no_replies")
  POSTS_NO_REPLIES,
  @SerialName("posts_with_media")
  POSTS_WITH_MEDIA,
}
