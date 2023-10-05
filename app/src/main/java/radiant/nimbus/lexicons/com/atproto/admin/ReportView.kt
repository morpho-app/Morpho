package com.atproto.admin

import com.atproto.moderation.Token
import com.atproto.repo.StrongRef
import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.model.Timestamp
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface ReportViewSubjectUnion {
  public class RepoRefSerializer : KSerializer<RepoRef> by valueClassSerializer()

  @Serializable(with = RepoRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoRef")
  public value class RepoRef(
    public val `value`: com.atproto.admin.RepoRef,
  ) : ReportViewSubjectUnion

  public class RepoStrongRefSerializer : KSerializer<RepoStrongRef> by valueClassSerializer()

  @Serializable(with = RepoStrongRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.strongRef")
  public value class RepoStrongRef(
    public val `value`: StrongRef,
  ) : ReportViewSubjectUnion
}

@Serializable
public data class ReportView(
  public val id: Long,
  public val reasonType: Token,
  public val reason: String? = null,
  public val subjectRepoHandle: String? = null,
  public val subject: ReportViewSubjectUnion,
  public val reportedBy: Did,
  public val createdAt: Timestamp,
  public val resolvedByActionIds: ReadOnlyList<Long>,
)
