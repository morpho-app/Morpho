package com.morpho.app.ui.elements


import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.morpho.butterfly.Language
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.ui.common.sharePost
import com.morpho.app.util.openBrowser


enum class MenuOptions(val text: String) {
    Translate("Translate"),
    Share("Share"),
    MuteThread("Mute Thread"),
    ReportPost( "Report Post"),
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
    reportCallback: () -> Unit = {},
) {
    when(options) {
        MenuOptions.Translate -> {
            openBrowser("https://translate.google.com/?sl=auto&tl=${language}&text=${post.text}")
        }
        MenuOptions.Share -> { sharePost(post) }
        MenuOptions.MuteThread -> {
            /* TODO: come back to this button when notification backend is a thing */
        }
        MenuOptions.ReportPost -> {}
    }
}