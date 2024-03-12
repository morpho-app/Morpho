package morpho.app.ui.elements

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import morpho.app.api.Language
import morpho.app.model.BskyPost


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
    ctx: Context,
    post: BskyPost,
    language: Language = Language("en"),
    reportCallback: () -> Unit = {},
) {
    when(options) {
        MenuOptions.Translate -> {
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://translate.google.com/?sl=${language}&tl=en&text=${post.text}&op=translate")
            )
            ctx.startActivity(urlIntent)
        }
        MenuOptions.Share -> {
            val postUrl = post.uri.atUri
                .replaceBefore("post", "https://bsky.app/profile/${post.author.handle}/")
            val shareIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, postUrl)
                putExtra(Intent.EXTRA_TITLE, "Post by ${post.author.displayName.orEmpty()} ${post.author.handle}")

                data = Uri.parse(post.author.avatar.orEmpty())
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }, null)

            ctx.startActivity(shareIntent)
        }
        MenuOptions.MuteThread -> {
            /* TODO: come back to this button when notification backend is a thing */
        }
        MenuOptions.ReportPost -> {}
    }
}