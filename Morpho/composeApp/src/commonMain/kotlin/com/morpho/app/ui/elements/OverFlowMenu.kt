package com.morpho.app.ui.elements


import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.ui.common.sharePost
import com.morpho.app.util.ClipboardManager
import com.morpho.app.util.json
import com.morpho.app.util.openBrowser
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Language


enum class MenuOptions(val text: String) {
    Translate("Translate"),
    Share("Share"),
    MuteThread("Mute Thread"),
    ReportPost( "Report Post"),
    CopyText("Copy Text"),
    CopyJson("Copy JSON"),
}
@Composable
inline fun PostMenu(
    expanded : Boolean = false,
    crossinline onItemClicked: (MenuOptions) -> Unit = {},
    crossinline onDismissRequest: () -> Unit = {},
) {
    DropdownMenu(expanded = expanded, onDismissRequest = {onDismissRequest()}) {
        MenuOptions.entries.forEach {
            DropdownMenuItem(text = {Text(it.text)}, onClick = { onItemClicked(it) })
        }
    }    
}

inline fun doMenuOperation(
    options: MenuOptions,
    post: BskyPost,
    language: Language = Language("en"),
    reportCallback: (AtUri) -> Unit = {},
    muteCallback: (AtUri) -> Unit = {},
    clipboardManager: ClipboardManager,
) {
    when(options) {
        MenuOptions.Translate -> run {
            openBrowser("https://translate.google.com/?sl=auto&tl=${language}&text=${post.text}&op=translate")
        }
        MenuOptions.Share -> { sharePost(post) }
        MenuOptions.MuteThread -> {
            muteCallback(post.uri)
            /* TODO: come back to this button when notification backend is a thing */
        }
        MenuOptions.ReportPost -> {
            reportCallback(post.uri)
        }
        MenuOptions.CopyText -> {
            clipboardManager.copyToClipboard(post.text)
        }
        MenuOptions.CopyJson -> {
            clipboardManager.copyToClipboard(json.encodeToString(BskyPost.serializer(), post))
        }
    }
}