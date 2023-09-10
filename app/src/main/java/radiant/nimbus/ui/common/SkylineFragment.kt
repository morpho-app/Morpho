package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.bsky.feed.FeedViewPost
import kotlinx.collections.immutable.PersistentList
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.toPost
import radiant.nimbus.ui.theme.NimbusTheme

@Composable
fun SkylineFragment (
    postList: PersistentList<FeedViewPost>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        postList.forEach { 
            PostFragment(post = it.toPost())
        }
    }
}


@Composable
@Preview
fun PreviewSkyline() {
    NimbusTheme {
        ScreenBody(modifier = Modifier.fillMaxSize()) {
            var posts = mutableListOf<FeedViewPost>()
            for (i in 1..10) {
                //posts.add(testPost)
            }
            //SkylineFragment(postList = posts)
        }
    }

}
