package morpho.app.api

import morpho.app.api.AtUri

data class BskyViewerState (
    val muted: Boolean?,
    val blockedBy: Boolean?,
    val blocking: AtUri?,
    val following: AtUri?,
    val followedBy: AtUri?
)