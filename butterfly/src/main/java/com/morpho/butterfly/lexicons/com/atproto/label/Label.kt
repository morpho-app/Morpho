package com.atproto.label

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Uri
import com.morpho.butterfly.model.Timestamp

/**
 * Metadata tag on an atproto resource (eg, repo or record)
 */
@Serializable
public data class Label(
  /**
   * DID of the actor who created this label
   */
  public val src: Did,
  /**
   * AT URI of the record, repository (account), or other resource which this label applies to
   */
  public val uri: Uri,
  /**
   * optionally, CID specifying the specific version of 'uri' resource this label applies to
   */
  public val cid: Cid? = null,
  /**
   * the short string name of the value or type of this label
   */
  public val `val`: String,
  /**
   * if true, this is a negation label, overwriting a previous label
   */
  public val neg: Boolean? = null,
  /**
   * timestamp when this label was created
   */
  public val cts: Timestamp,
) {
  init {
    require(`val`.count() <= 128) {
      "val.count() must be <= 128, but was ${`val`.count()}"
    }
  }
}
