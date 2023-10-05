package com.atproto.server

import kotlin.Any
import kotlin.Boolean
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetAccountInviteCodesQueryParams(
  public val includeUsed: Boolean? = true,
  public val createAvailable: Boolean? = true,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("includeUsed" to includeUsed)
    add("createAvailable" to createAvailable)
  }.toImmutableList()
}

@Serializable
public data class GetAccountInviteCodesResponse(
  public val codes: ReadOnlyList<InviteCode>,
)
