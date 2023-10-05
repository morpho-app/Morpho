package com.atproto.admin

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetModerationReportQueryParams(
  public val id: Long,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("id" to id)
  }.toImmutableList()
}

public typealias GetModerationReportResponse = ReportViewDetail
