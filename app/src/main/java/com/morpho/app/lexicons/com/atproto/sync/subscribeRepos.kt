package com.atproto.sync

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface SubscribeReposMessageUnion {
  public class CommitSerializer : KSerializer<Commit> by valueClassSerializer()

  @Serializable(with = CommitSerializer::class)
  @JvmInline
  @SerialName("com.atproto.sync.subscribeRepos#commit")
  public value class Commit(
    public val `value`: SubscribeReposCommit,
  ) : SubscribeReposMessageUnion

  public class HandleSerializer : KSerializer<Handle> by valueClassSerializer()

  @Serializable(with = HandleSerializer::class)
  @JvmInline
  @SerialName("com.atproto.sync.subscribeRepos#handle")
  public value class Handle(
    public val `value`: SubscribeReposHandle,
  ) : SubscribeReposMessageUnion

  public class MigrateSerializer : KSerializer<Migrate> by valueClassSerializer()

  @Serializable(with = MigrateSerializer::class)
  @JvmInline
  @SerialName("com.atproto.sync.subscribeRepos#migrate")
  public value class Migrate(
    public val `value`: SubscribeReposMigrate,
  ) : SubscribeReposMessageUnion

  public class TombstoneSerializer : KSerializer<Tombstone> by valueClassSerializer()

  @Serializable(with = TombstoneSerializer::class)
  @JvmInline
  @SerialName("com.atproto.sync.subscribeRepos#tombstone")
  public value class Tombstone(
    public val `value`: SubscribeReposTombstone,
  ) : SubscribeReposMessageUnion

  public class InfoSerializer : KSerializer<Info> by valueClassSerializer()

  @Serializable(with = InfoSerializer::class)
  @JvmInline
  @SerialName("com.atproto.sync.subscribeRepos#info")
  public value class Info(
    public val `value`: SubscribeReposInfo,
  ) : SubscribeReposMessageUnion
}

@Serializable
public data class SubscribeReposQueryParams(
  /**
   * The last known event to backfill from.
   */
  public val cursor: Long? = null,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("cursor" to cursor)
  }.toImmutableList()
}

public typealias SubscribeReposMessage = SubscribeReposMessageUnion
