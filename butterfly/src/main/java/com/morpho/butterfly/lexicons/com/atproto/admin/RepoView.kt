package com.atproto.admin

import com.atproto.server.InviteCode
import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class RepoView(
  public val did: Did,
  public val handle: Handle,
  public val email: String? = null,
  public val relatedRecords: ReadOnlyList<JsonElement>,
  public val indexedAt: Timestamp,
  public val moderation: Moderation,
  public val invitedBy: InviteCode? = null,
  public val invitesDisabled: Boolean? = null,
  public val inviteNote: String? = null,
)
