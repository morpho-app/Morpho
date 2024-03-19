package com.atproto.admin

import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetModerationReportsQuery(
  public val subject: String? = null,
  public val ignoreSubjects: ReadOnlyList<String> = persistentListOf(),
  /**
   * Get all reports that were actioned by a specific moderator
   */
  public val actionedBy: Did? = null,
  /**
   * Filter reports made by one or more DIDs
   */
  public val reporters: ReadOnlyList<String> = persistentListOf(),
  public val resolved: Boolean? = null,
  public val actionType: String? = null,
  public val limit: Long? = 50,
  public val cursor: String? = null,
  /**
   * Reverse the order of the returned records? when true, returns reports in chronological order
   */
  public val reverse: Boolean? = null,
) {
  init {
    require(limit == null || limit >= 1) {
      "limit must be >= 1, but was $limit"
    }
    require(limit == null || limit <= 100) {
      "limit must be <= 100, but was $limit"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("subject" to subject)
    ignoreSubjects.forEach {
      add("ignoreSubjects" to it)
    }
    add("actionedBy" to actionedBy)
    reporters.forEach {
      add("reporters" to it)
    }
    add("resolved" to resolved)
    add("actionType" to actionType)
    add("limit" to limit)
    add("cursor" to cursor)
    add("reverse" to reverse)
  }.toImmutableList()
}

@Serializable
public data class GetModerationReportsResponse(
  public val cursor: String? = null,
  public val reports: ReadOnlyList<ReportView>,
)
