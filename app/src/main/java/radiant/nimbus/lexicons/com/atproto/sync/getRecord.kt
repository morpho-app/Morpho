package com.atproto.sync

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Did
import radiant.nimbus.api.Nsid
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetRecordQueryParams(
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
