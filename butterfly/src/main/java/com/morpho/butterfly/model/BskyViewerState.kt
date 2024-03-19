package com.morpho.butterfly.model

import com.morpho.butterfly.AtUri

data class BskyViewerState (
    val muted: Boolean?,
    val blockedBy: Boolean?,
    val blocking: AtUri?,
    val following: AtUri?,
    val followedBy: AtUri?
)