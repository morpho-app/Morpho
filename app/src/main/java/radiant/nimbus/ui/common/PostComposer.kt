package radiant.nimbus.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.bsky.feed.Post
import radiant.nimbus.model.BskyPost

@Composable
fun PostComposer(
    modifier: Modifier = Modifier,
    initialContent: BskyPost? = null,
    onSend: (Post) -> Unit = {},
    onCancel: (Post) -> Unit = {}
) {

}