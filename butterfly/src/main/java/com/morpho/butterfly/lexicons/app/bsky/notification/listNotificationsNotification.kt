package app.bsky.notification

import app.bsky.actor.ProfileView
import com.atproto.label.Label
import kotlin.Boolean
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class ListNotificationsNotification(
  public val uri: AtUri,
  public val cid: Cid,
  public val author: ProfileView,
  /**
   * Expected values are 'like', 'repost', 'follow', 'mention', 'reply', and 'quote'.
   */
  public val reason: ListNotificationsReason,
  public val reasonSubject: AtUri? = null,
  public val record: JsonElement,
  public val isRead: Boolean,
  public val indexedAt: Timestamp,
  public val labels: ReadOnlyList<Label> = persistentListOf(),
)
