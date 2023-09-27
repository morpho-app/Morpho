package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.SkylineItem
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.theme.NimbusTheme

typealias OnPostClicked = (BskyPost) -> Unit

class SkylineFragmentState(

)

@Composable
fun SkylineFragment (
    postList: List<SkylineItem>,
    onItemClicked: OnPostClicked,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    //val mainViewModel:MainViewModel = activityViewModel()
    LazyColumn(
        modifier = modifier,
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        state = listState
    ) {
        items(postList) { skylineItem ->
            if( skylineItem.post != null) {
                val post = skylineItem.post
                var threadLoaded = remember { mutableStateOf(false) }
                if (post != null) {
                    if (post.reply != null) {
                        LaunchedEffect(Unit) {
                            //skylineItem.postToThread(mainViewModel.apiProvider, depth = 1).join()
                            //threadLoaded.value = true
                        }
                        when (threadLoaded.value) {
                            true -> skylineItem.thread?.let {
                                SkylineThreadFragment(
                                    thread = it,
                                    modifier = Modifier.fillParentMaxWidth(),
                                )
                            }
                            false -> PostFragment(
                                modifier = Modifier.fillParentMaxWidth(),
                                post = post,
                                onItemClicked = onItemClicked,
                            )
                        }
                    } else {
                        PostFragment(
                            modifier = Modifier.fillParentMaxWidth(),
                            post = post,
                            onItemClicked = onItemClicked,
                        )
                    }
                }
            }
            if (skylineItem.thread != null) {

            }
        }
    }
}


@Composable
fun SkylineThreadFragment(
    thread: BskyPostThread,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        LazyColumn(
            modifier = modifier,
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            state = listState
        ) {
            items(thread.parents) {
                if( it.hashCode() == thread.parents[0].hashCode()) {
                    ThreadItem(item = it, indentLevel = 0, role = PostFragmentRole.ThreadBranchStart)
                } else {
                    ThreadItem(item = it, indentLevel = 1, role = PostFragmentRole.ThreadBranchMiddle)
                }
            }
            item {
                ThreadItem(item = ThreadPost.ViewablePost(thread.post), indentLevel = 1, role = PostFragmentRole.ThreadBranchEnd)
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
