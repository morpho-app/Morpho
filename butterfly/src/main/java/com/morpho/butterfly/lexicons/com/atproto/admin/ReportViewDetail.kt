package com.atproto.admin

import com.atproto.moderation.Token
import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface ReportViewDetailSubjectUnion {
  public class RepoViewSerializer : KSerializer<RepoView> by valueClassSerializer()

  @Serializable(with = RepoViewSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoView")
  public value class RepoView(
    public val `value`: com.atproto.admin.RepoView,
  ) : ReportViewDetailSubjectUnion

  public class RepoViewNotFoundSerializer : KSerializer<RepoViewNotFound> by valueClassSerializer()

  @Serializable(with = RepoViewNotFoundSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoViewNotFound")
  public value class RepoViewNotFound(
    public val `value`: com.atproto.admin.RepoViewNotFound,
  ) : ReportViewDetailSubjectUnion

  public class RecordViewSerializer : KSerializer<RecordView> by valueClassSerializer()

  @Serializable(with = RecordViewSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#recordView")
  public value class RecordView(
    public val `value`: com.atproto.admin.RecordView,
  ) : ReportViewDetailSubjectUnion

  public class RecordViewNotFoundSerializer : KSerializer<RecordViewNotFound> by
      valueClassSerializer()

  @Serializable(with = RecordViewNotFoundSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#recordViewNotFound")
  public value class RecordViewNotFound(
    public val `value`: com.atproto.admin.RecordViewNotFound,
  ) : ReportViewDetailSubjectUnion
}

@Serializable
public data class ReportViewDetail(
  public val id: Long,
  public val reasonType: Token,
  public val reason: String? = null,
  public val subject: ReportViewDetailSubjectUnion,
  public val reportedBy: Did,
  public val createdAt: Timestamp,
  public val resolvedByActions: ReadOnlyList<ActionView>,
)
