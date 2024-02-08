package app.bsky.embed

import app.bsky.feed.GeneratorView
import app.bsky.graph.ListView
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface RecordViewRecordUnion {
  public class ViewRecordSerializer : KSerializer<ViewRecord> by valueClassSerializer()

  @Serializable(with = ViewRecordSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.record#viewRecord")
  public value class ViewRecord(
    public val `value`: RecordViewRecord,
  ) : RecordViewRecordUnion

  public class ViewNotFoundSerializer : KSerializer<ViewNotFound> by valueClassSerializer()

  @Serializable(with = ViewNotFoundSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.record#viewNotFound")
  public value class ViewNotFound(
    public val `value`: RecordViewNotFound,
  ) : RecordViewRecordUnion

  public class ViewBlockedSerializer : KSerializer<ViewBlocked> by valueClassSerializer()

  @Serializable(with = ViewBlockedSerializer::class)
  @JvmInline
  @SerialName("app.bsky.embed.record#viewBlocked")
  public value class ViewBlocked(
    public val `value`: RecordViewBlocked,
  ) : RecordViewRecordUnion

  public class FeedGeneratorViewSerializer : KSerializer<FeedGeneratorView> by
      valueClassSerializer()

  @Serializable(with = FeedGeneratorViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#generatorView")
  public value class FeedGeneratorView(
    public val `value`: GeneratorView,
  ) : RecordViewRecordUnion

  public class GraphListViewSerializer : KSerializer<GraphListView> by valueClassSerializer()

  @Serializable(with = GraphListViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.graph.defs#listView")
  public value class GraphListView(
    public val `value`: ListView,
  ) : RecordViewRecordUnion
}

@Serializable
public data class RecordView(
  public val record: RecordViewRecordUnion,
)
