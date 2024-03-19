package com.atproto.label

import kotlin.Long
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class SubscribeLabelsLabels(
  public val seq: Long,
  public val labels: ReadOnlyList<Label>,
)
