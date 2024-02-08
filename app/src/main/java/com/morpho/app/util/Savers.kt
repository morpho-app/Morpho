package morpho.app.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import morpho.app.api.AtUri

val atUriSaver: Saver<AtUri, *> = listSaver(
    save = { listOf(it.atUri)},
    restore = {
        AtUri(it.first())
    }
)