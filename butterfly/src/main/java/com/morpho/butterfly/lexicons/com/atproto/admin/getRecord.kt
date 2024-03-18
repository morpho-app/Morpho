package com.atproto.admin

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.model.ReadOnlyList

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
