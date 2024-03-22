package app.bsky.feed

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri

/**
 * Allow replies from actors on a list.
 */
@Serializable
public data class ThreadgateListRule(
  public val list: AtUri,
)
