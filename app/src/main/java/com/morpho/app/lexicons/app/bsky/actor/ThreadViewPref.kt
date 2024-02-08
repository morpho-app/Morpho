package app.bsky.actor

import kotlin.Boolean
import kotlinx.serialization.Serializable

@Serializable
public data class ThreadViewPref(
  /**
   * Sorting mode.
   */
  public val sort: Sort? = null,
  /**
   * Show followed users at the top of all replies.
   */
  public val prioritizeFollowedUsers: Boolean? = null,
)
