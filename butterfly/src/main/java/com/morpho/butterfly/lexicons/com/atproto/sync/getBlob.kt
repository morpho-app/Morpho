package com.atproto.sync

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetBlobQuery(
  /**
   * The DID of the repo.
   */
  public val did: Did,
  /**
   * The CID of the blob to fetch
   */
  public val cid: Cid,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("did" to did)
    add("cid" to cid)
  }.toImmutableList()
}
