package com.atproto.admin

import com.atproto.label.Label
import com.atproto.server.InviteCode
import kotlin.Boolean
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import radiant.nimbus.api.Did
import radiant.nimbus.api.Handle
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class RepoViewDetail(
  public val did: Did,
  public val handle: Handle,
  public val email: String? = null,
  public val relatedRecords: ReadOnlyList<JsonElement>,
  public val indexedAt: Timestamp,
  public val moderation: ModerationDetail,
  public val labels: ReadOnlyList<Label> = persistentListOf(),
  public val invitedBy: InviteCode? = null,
  public val invites: ReadOnlyList<InviteCode> = persistentListOf(),
  public val invitesDisabled: Boolean? = null,
  public val inviteNote: String? = null,
)
