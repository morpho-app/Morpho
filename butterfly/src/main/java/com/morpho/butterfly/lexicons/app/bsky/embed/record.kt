package app.bsky.embed

import com.atproto.repo.StrongRef
import kotlinx.serialization.Serializable

@Serializable
public data class Record(
  public val record: StrongRef,
)

@Serializable
public data class RecordMain(
  public val record: StrongRef,
)