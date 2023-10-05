package com.atproto.admin

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetRecordQueryParams(
  public val uri: AtUri,
  public val cid: Cid? = null,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("uri" to uri)
    add("cid" to cid)
  }.toImmutableList()
}

public typealias GetRecordResponse = RecordViewDetail
