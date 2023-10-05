package com.atproto.admin

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
public sealed interface ActionViewDetailSubjectUnion {
  public class RepoViewSerializer : KSerializer<RepoView> by valueClassSerializer()

  @Serializable(with = RepoViewSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoView")
  public value class RepoView(
    public val `value`: com.atproto.admin.RepoView,
  ) : ActionViewDetailSubjectUnion

  public class RepoViewNotFoundSerializer : KSerializer<RepoViewNotFound> by valueClassSerializer()

  @Serializable(with = RepoViewNotFoundSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#repoViewNotFound")
  public value class RepoViewNotFound(
    public val `value`: com.atproto.admin.RepoViewNotFound,
  ) : ActionViewDetailSubjectUnion

  public class RecordViewSerializer : KSerializer<RecordView> by valueClassSerializer()

  @Serializable(with = RecordViewSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#recordView")
  public value class RecordView(
    public val `value`: com.atproto.admin.RecordView,
  ) : ActionViewDetailSubjectUnion

  public class RecordViewNotFoundSerializer : KSerializer<RecordViewNotFound> by
      valueClassSerializer()

  @Serializable(with = RecordViewNotFoundSerializer::class)
  @JvmInline
  @SerialName("com.atproto.admin.defs#recordViewNotFound")
  public value class RecordViewNotFound(
    public val `value`: com.atproto.admin.RecordViewNotFound,
  ) : ActionViewDetailSubjectUnion
}

@Serializable
public data class ActionViewDetail(
  public val id: Long,
  public val action: Token,
  /**
   * Indicates how long this action was meant to be in effect before automatically expiring.
   */
  public val durationInHours: Long? = null,
  public val subject: ActionViewDetailSubjectUnion,
  public val subjectBlobs: ReadOnlyList<BlobView>,
  public val createLabelVals: ReadOnlyList<String> = persistentListOf(),
  public val negateLabelVals: ReadOnlyList<String> = persistentListOf(),
  public val reason: String,
  public val createdBy: Did,
  public val createdAt: Timestamp,
  public val reversal: ActionReversal? = null,
  public val resolvedReports: ReadOnlyList<ReportView>,
)
