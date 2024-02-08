@file:Suppress("PropertyName")

package app.bsky.actor

import kotlinx.serialization.Serializable

@Serializable
public data class FeedViewPref(
  /**
   * The URI of the feed, or an identifier which describes the feed.
   */
  public val feed: String,
  /**
   * Hide replies in the feed.
   */
  public val hideReplies: Boolean? = null,
  /**
   * Hide replies in the feed if they are not by followed users.
   */
  public val hideRepliesByUnfollowed: Boolean? = null,
  /**
   * Hide replies in the feed if they do not have this number of likes.
   */
  public val hideRepliesByLikeCount: Long? = null,
  /**
   * Hide reposts in the feed.
   */
  public val hideReposts: Boolean? = null,
  /**
   * Hide quote posts in the feed.
   */
  public val hideQuotePosts: Boolean? = null,

  public val lab_mergeFeedEnabled: Boolean? = null
)
