package com.atproto.admin

import kotlinx.serialization.SerialName

public enum class Token {
  @SerialName("com.atproto.admin.defs#takedown")
  TAKEDOWN,
  @SerialName("com.atproto.admin.defs#flag")
  FLAG,
  @SerialName("com.atproto.admin.defs#acknowledge")
  ACKNOWLEDGE,
  @SerialName("com.atproto.admin.defs#escalate")
  ESCALATE,
}
