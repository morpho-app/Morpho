package radiant.nimbus.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import radiant.nimbus.api.AtUri

val atUriSaver: Saver<AtUri, *> = listSaver(
    save = { listOf(it.atUri)},
    restore = {
        AtUri(it.first())
    }
)