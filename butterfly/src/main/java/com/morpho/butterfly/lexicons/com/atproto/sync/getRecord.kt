package com.atproto.sync

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Nsid
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetRecordQuery(
  /**
   * The DID of the repo.
   */
  public val did: Did,
  public val collection: Nsid,
  public val rkey: String,
  /**
   * An optional past commit CID.
   */
  public val commit: Cid? = null,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("did" to did)
    add("collection" to collection)
    add("rkey" to rkey)
    add("commit" to commit)
  }.toImmutableList()
}
