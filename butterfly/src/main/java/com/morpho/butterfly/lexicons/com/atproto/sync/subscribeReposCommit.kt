package com.atproto.sync

import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp
import kotlinx.serialization.ExperimentalSerializationApi

@Serializable
public data class SubscribeReposCommit @OptIn(ExperimentalSerializationApi::class) constructor(
  public val seq: Long,
  public val rebase: Boolean,
  public val tooBig: Boolean,
  public val repo: Did,
  @ByteString
  public val commit: ByteArray,
  @ByteString
  public val prev: ByteArray? = null,
  /**
   * The rev of the emitted commit
   */
  public val rev: String,
  /**
   * The rev of the last emitted commit from this repo
   */
  public val since: String? = null,
  /**
   * CAR file containing relevant blocks
   */
  @ByteString
  public val blocks: ByteArray,
  public val ops: ReadOnlyList<SubscribeReposRepoOp>,
  @ByteString
  public val blobs: ReadOnlyList<ByteArray>,
  public val time: Timestamp,
) {
  init {
    require(blocks.count() <= 1_000_000) {
      "blocks.count() must be <= 1_000_000, but was ${blocks.count()}"
    }
    require(ops.count() <= 200) {
      "ops.count() must be <= 200, but was ${ops.count()}"
    }
  }
}
