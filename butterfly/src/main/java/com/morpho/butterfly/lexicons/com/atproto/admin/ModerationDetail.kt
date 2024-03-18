package com.atproto.admin

import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class ModerationDetail(
  public val currentAction: ActionViewCurrent? = null,
  public val actions: ReadOnlyList<ActionView>,
  public val reports: ReadOnlyList<ReportView>,
)
