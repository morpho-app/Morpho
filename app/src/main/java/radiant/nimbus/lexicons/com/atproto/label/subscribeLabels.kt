package com.atproto.label

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface SubscribeLabelsMessageUnion {
  public class LabelsSerializer : KSerializer<Labels> by valueClassSerializer()

  @Serializable(with = LabelsSerializer::class)
  @JvmInline
  @SerialName("com.atproto.label.subscribeLabels#labels")
  public value class Labels(
    public val `value`: SubscribeLabelsLabels,
  ) : SubscribeLabelsMessageUnion

  public class InfoSerializer : KSerializer<Info> by valueClassSerializer()

  @Serializable(with = InfoSerializer::class)
  @JvmInline
  @SerialName("com.atproto.label.subscribeLabels#info")
  public value class Info(
    public val `value`: SubscribeLabelsInfo,
  ) : SubscribeLabelsMessageUnion
}

@Serializable
public data class SubscribeLabelsQueryParams(
  /**
   * The last known event to backfill from.
   */
  public val cursor: Long? = null,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("cursor" to cursor)
  }.toImmutableList()
}

public typealias SubscribeLabelsMessage = SubscribeLabelsMessageUnion
