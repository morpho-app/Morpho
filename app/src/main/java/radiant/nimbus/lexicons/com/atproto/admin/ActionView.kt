package com.atproto.admin

import com.atproto.repo.StrongRef
import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.model.Timestamp
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface ActionViewSubjectUnion {
  public class RepoRefSerializer : KSerializer<RepoRef> by valueClassSerializer()

  @Serializable(with = RepoRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoRef")
  public value class RepoRef(
    public val `value`: com.atproto.admin.RepoRef,
  ) : ActionViewSubjectUnion

  public class RepoStrongRefSerializer : KSerializer<RepoStrongRef> by valueClassSerializer()

  @Serializable(with = RepoStrongRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.strongRef")
  public value class RepoStrongRef(
    public val `value`: StrongRef,
  ) : ActionViewSubjectUnion
}

@Serializable
public data class ActionView(
  public val id: Long,
  public val action: Token,
  /**
   * Indicates how long this action was meant to be in effect before automatically expiring.
   */
  public val durationInHours: Long? = null,
  public val subject: ActionViewSubjectUnion,
  public val subjectBlobCids: ReadOnlyList<String>,
  public val createLabelVals: ReadOnlyList<String> = persistentListOf(),
  public val negateLabelVals: ReadOnlyList<String> = persistentListOf(),
  public val reason: String,
  public val createdBy: Did,
  public val createdAt: Timestamp,
  public val reversal: ActionReversal? = null,
  public val resolvedReportIds: ReadOnlyList<Long>,
)
