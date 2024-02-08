package app.bsky.feed

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

/**
 * Allow replies from actors on a list.
 */
@Serializable
public data class ThreadgateListRule(
  public val list: AtUri,
)
