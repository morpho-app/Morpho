package com.atproto.sync

import kotlin.ByteArray
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString

/**
 * A repo operation, ie a write of a single record. For creates and updates, cid is the record's CID
 * as of this operation. For deletes, it's null.
 */
@Serializable
public data class SubscribeReposRepoOp(
  public val action: SubscribeReposAction,
  public val path: String,
  @ByteString
  public val cid: ByteArray? = null,
)
