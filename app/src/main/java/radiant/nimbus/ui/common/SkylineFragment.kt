package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.SkylineItem
import radiant.nimbus.ui.theme.NimbusTheme

typealias OnPostClicked = (BskyPost) -> Unit



@Composable
fun SkylineFragment (
    postList: List<SkylineItem>,
    onItemClicked: OnPostClicked,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        state = listState
    ) {
        items(postList) { skylineItem ->
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(0.dp)
            ) {
                skylineItem.post?.let {
                    PostFragment(
                        modifier = Modifier.fillParentMaxWidth(),
                        post = it,
                        onItemClicked = onItemClicked,
                    )
                }
            }
        }
    }
}



@Composable
@Preview
fun PreviewSkyline() {
    NimbusTheme {
        ScreenBody(modifier = Modifier.height(1000.dp)) {
            var posts = mutableListOf<SkylineItem>()
            for (i in 1..10) {
                posts.add(SkylineItem(testPost))
            }
            SkylineFragment(postList = posts, {})
        }
    }

}
