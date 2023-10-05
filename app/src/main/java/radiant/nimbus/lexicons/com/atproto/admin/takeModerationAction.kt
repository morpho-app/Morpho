package com.atproto.admin

import com.atproto.repo.StrongRef
import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface TakeModerationActionRequestSubjectUnion {
  public class AdminRepoRefSerializer : KSerializer<AdminRepoRef> by valueClassSerializer()

  @Serializable(with = AdminRepoRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoRef")
  public value class AdminRepoRef(
    public val `value`: RepoRef,
  ) : TakeModerationActionRequestSubjectUnion

  public class RepoStrongRefSerializer : KSerializer<RepoStrongRef> by valueClassSerializer()

  @Serializable(with = RepoStrongRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.strongRef")
  public value class RepoStrongRef(
    public val `value`: StrongRef,
  ) : TakeModerationActionRequestSubjectUnion
}

@Serializable
public data class TakeModerationActionRequest(
  public val action: String,
  public val subject: TakeModerationActionRequestSubjectUnion,
  public val subjectBlobCids: ReadOnlyList<Cid> = persistentListOf(),
  public val createLabelVals: ReadOnlyList<String> = persistentListOf(),
  public val negateLabelVals: ReadOnlyList<String> = persistentListOf(),
  public val reason: String,
  /**
   * Indicates how long this action was meant to be in effect before automatically expiring.
   */
  public val durationInHours: Long? = null,
  public val createdBy: Did,
)

public typealias TakeModerationActionResponse = ActionView
