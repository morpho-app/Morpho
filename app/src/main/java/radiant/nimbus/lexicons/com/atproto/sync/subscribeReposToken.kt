package com.atproto.sync

import kotlinx.serialization.SerialName

public enum class SubscribeReposName {
  @SerialName("OutdatedCursor")
  OUTDATED_CURSOR,
}

public enum class SubscribeReposAction {
  @SerialName("create")
  CREATE,
  @SerialName("update")
  UPDATE,
  @SerialName("delete")
  DELETE,
}
