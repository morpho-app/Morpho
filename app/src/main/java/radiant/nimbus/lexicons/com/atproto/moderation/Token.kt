package com.atproto.moderation

import kotlinx.serialization.SerialName

public enum class Token {
  @SerialName("com.atproto.moderation.defs#reasonSpam")
  REASON_SPAM,
  @SerialName("com.atproto.moderation.defs#reasonViolation")
  REASON_VIOLATION,
  @SerialName("com.atproto.moderation.defs#reasonMisleading")
  REASON_MISLEADING,
  @SerialName("com.atproto.moderation.defs#reasonSexual")
  REASON_SEXUAL,
  @SerialName("com.atproto.moderation.defs#reasonRude")
  REASON_RUDE,
  @SerialName("com.atproto.moderation.defs#reasonOther")
  REASON_OTHER,
}
