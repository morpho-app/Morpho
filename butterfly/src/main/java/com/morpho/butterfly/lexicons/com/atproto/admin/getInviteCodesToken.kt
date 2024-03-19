package com.atproto.admin

import kotlinx.serialization.SerialName

public enum class GetInviteCodesSort {
  @SerialName("recent")
  RECENT,
  @SerialName("usage")
  USAGE,
}
