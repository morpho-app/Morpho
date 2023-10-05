package com.atproto.moderation

import com.atproto.admin.RepoRef
import com.atproto.repo.StrongRef
import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.Timestamp
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface CreateReportRequestSubjectUnion {
  public class AdminRepoRefSerializer : KSerializer<AdminRepoRef> by valueClassSerializer()

  @Serializable(with = AdminRepoRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoRef")
  public value class AdminRepoRef(
    public val `value`: RepoRef,
  ) : CreateReportRequestSubjectUnion

  public class RepoStrongRefSerializer : KSerializer<RepoStrongRef> by valueClassSerializer()

  @Serializable(with = RepoStrongRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.strongRef")
  public value class RepoStrongRef(
    public val `value`: StrongRef,
  ) : CreateReportRequestSubjectUnion
}

@Serializable
public sealed interface CreateReportResponseSubjectUnion {
  public class AdminRepoRefSerializer : KSerializer<AdminRepoRef> by valueClassSerializer()

  @Serializable(with = AdminRepoRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoRef")
  public value class AdminRepoRef(
    public val `value`: RepoRef,
  ) : CreateReportResponseSubjectUnion

  public class RepoStrongRefSerializer : KSerializer<RepoStrongRef> by valueClassSerializer()

  @Serializable(with = RepoStrongRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.strongRef")
  public value class RepoStrongRef(
    public val `value`: StrongRef,
  ) : CreateReportResponseSubjectUnion
}

@Serializable
public data class CreateReportRequest(
  public val reasonType: Token,
  public val reason: String? = null,
  public val subject: CreateReportRequestSubjectUnion,
)

@Serializable
public data class CreateReportResponse(
  public val id: Long,
  public val reasonType: Token,
  public val reason: String? = null,
  public val subject: CreateReportResponseSubjectUnion,
  public val reportedBy: Did,
  public val createdAt: Timestamp,
)
