package com.morpho.app.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.morpho.butterfly.AtUri

val atUriSaver: Saver<AtUri, *> = listSaver(
    save = { listOf(it.atUri)},
    restore = {
        AtUri(it.first())
    }
)