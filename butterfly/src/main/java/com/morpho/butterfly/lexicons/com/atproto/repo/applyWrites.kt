package com.atproto.repo

import kotlin.Boolean
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Cid
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface WritesUnion {
  public class CreateSerializer : KSerializer<Create> by valueClassSerializer()

  @Serializable(with = CreateSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.applyWrites#create")
  public value class Create(
    public val `value`: ApplyWritesCreate,
  ) : WritesUnion

  public class UpdateSerializer : KSerializer<Update> by valueClassSerializer()

  @Serializable(with = UpdateSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.applyWrites#update")
  public value class Update(
    public val `value`: ApplyWritesUpdate,
  ) : WritesUnion

  public class DeleteSerializer : KSerializer<Delete> by valueClassSerializer()

  @Serializable(with = DeleteSerializer::class)
  @JvmInline
  @SerialName("com.atproto.repo.applyWrites#delete")
  public value class Delete(
    public val `value`: ApplyWritesDelete,
  ) : WritesUnion
}

@Serializable
public data class ApplyWritesRequest(
  /**
   * The handle or DID of the repo.
   */
  public val repo: AtIdentifier,
  /**
   * Validate the records?
   */
  public val validate: Boolean? = true,
  public val writes: ReadOnlyList<WritesUnion>,
  public val swapCommit: Cid? = null,
)
