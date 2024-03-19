package com.atproto.moderation

import com.atproto.admin.RepoRef
import com.atproto.repo.StrongRef
import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface ReportRequestSubject {
  public class AdminRepoRefSerializer : KSerializer<AdminRepoRef> by valueClassSerializer()

  @Serializable(with = AdminRepoRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoRef")
  public value class AdminRepoRef(
    public val `value`: RepoRef,
  ) : ReportRequestSubject

  public class RepoStrongRefSerializer : KSerializer<RepoStrongRef> by valueClassSerializer()

  @Serializable(with = RepoStrongRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.strongRef")
  public value class RepoStrongRef(
    public val `value`: StrongRef,
  ) : ReportRequestSubject
}

@Serializable
public sealed interface ReportResponseSubject {
  public class AdminRepoRefSerializer : KSerializer<AdminRepoRef> by valueClassSerializer()

  @Serializable(with = AdminRepoRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoRef")
  public value class AdminRepoRef(
    public val `value`: RepoRef,
  ) : ReportResponseSubject

  public class RepoStrongRefSerializer : KSerializer<RepoStrongRef> by valueClassSerializer()

  @Serializable(with = RepoStrongRefSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.strongRef")
  public value class RepoStrongRef(
    public val `value`: StrongRef,
  ) : ReportResponseSubject
}

@Serializable
public data class CreateReportRequest(
  public val reasonType: Token,
  public val reason: String? = null,
  public val subject: ReportRequestSubject,
)

@Serializable
public data class CreateReportResponse(
  public val id: Long,
  public val reasonType: Token,
  public val reason: String? = null,
  public val subject: ReportResponseSubject,
  public val reportedBy: Did,
  public val createdAt: Timestamp,
)
