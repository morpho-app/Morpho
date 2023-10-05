package radiant.nimbus.api

import radiant.nimbus.api.AtUri

data class BskyViewerState (
    val muted: Boolean?,
    val blockedBy: Boolean?,
    val blocking: AtUri?,
    val following: AtUri?,
    val followedBy: AtUri?
)