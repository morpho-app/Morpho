package com.atproto.label

import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

/**
 * Metadata tags on an atproto record, published by the author within the record.
 */
@Serializable
public data class SelfLabels(
  public val values: ReadOnlyList<SelfLabel>,
) {
  init {
    require(values.count() <= 10) {
      "values.count() must be <= 10, but was ${values.count()}"
    }
  }
}
