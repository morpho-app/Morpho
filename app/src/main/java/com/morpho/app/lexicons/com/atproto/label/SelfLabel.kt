package com.atproto.label

import kotlin.String
import kotlinx.serialization.Serializable

/**
 * Metadata tag on an atproto record, published by the author within the record. Note -- schemas
 * should use #selfLabels, not #selfLabel.
 */
@Serializable
public data class SelfLabel(
  /**
   * the short string name of the value or type of this label
   */
  public val `val`: String,
) {
  init {
    require(`val`.count() <= 128) {
      "val.count() must be <= 128, but was ${`val`.count()}"
    }
  }
}
