package radiant.nimbus.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atproto.repo.StrongRef
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.SkylineItem
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.ui.post.testPost
import radiant.nimbus.ui.theme.NimbusTheme

typealias OnPostClicked = (AtUri) -> Unit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkylineFragment (
    navigator: DestinationsNavigator,
    postFlow: StateFlow<Skyline>,
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked,
    onProfileClicked: (AtIdentifier) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    onPostButtonClicked: () -> Unit = {},
    refresh: (String?) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isProfileFeed: Boolean = false,
) {
    val postList by postFlow.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if(listState.firstVisibleItemIndex == 0 && !isProfileFeed) listState.animateScrollToItem(0, 50)
    }
    LaunchedEffect(!listState.canScrollForward) {
        refresh(postList.cursor)
    }
    val scrolledDownBy by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    ConstraintLayout(
        modifier = if(isProfileFeed) {
            Modifier.fillMaxWidth().systemBarsPadding()
        }   else {
            Modifier
            .fillMaxSize()
            .systemBarsPadding()
        },
    ) {
        val (scrollButton, postButton, skyline) = createRefs()
        val leftGuideline = createGuidelineFromStart(40.dp)
        val rightGuideline = createGuidelineFromEnd(40.dp)
        val buttonGuideline = createGuidelineFromBottom(100.dp)
        LazyColumn(
            modifier = modifier.constrainAs(skyline) {
                 top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            contentPadding = if(isProfileFeed) {
                contentPadding
            }   else {
                PaddingValues(
                    bottom = contentPadding.calculateBottomPadding(),
                    top = WindowInsets.safeContent.only(WindowInsetsSides.Top).asPaddingValues()
                        .calculateTopPadding()
                )
            },
            verticalArrangement = Arrangement.Top,
            state = listState
        ) {
            if(!isProfileFeed) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .weight(0.4f)
                        )
                        IconButton(
                            onClick = { refresh(null) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh, contentDescription = "Refresh",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        TextButton(
                            onClick = { /*TODO*/ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                            //.weight(0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings, contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Text(text = "Feed settings", Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
            items(postList.posts) { skylineItem ->
                if (skylineItem.post != null) {
                    val post = skylineItem.post
                    val thread = skylineItem.thread
                    if (thread != null) {
                        SkylineThreadFragment(
                            thread = thread,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            onItemClicked = onItemClicked,
                            onProfileClicked = onProfileClicked,
                            onUnClicked = onUnClicked,
                            onRepostClicked = onRepostClicked,
                            onReplyClicked = onReplyClicked,
                            onMenuClicked = onMenuClicked,
                            onLikeClicked = onLikeClicked,
                        )
                    } else if (post != null) {
                        PostFragment(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            post = post,
                            onItemClicked = onItemClicked,
                            onProfileClicked = onProfileClicked,
                            elevate = true,
                            onUnClicked = onUnClicked,
                            onRepostClicked = onRepostClicked,
                            onReplyClicked = onReplyClicked,
                            onMenuClicked = onMenuClicked,
                            onLikeClicked = onLikeClicked,
                        )
                    }
                }

            }
        }
        // TODO: Rework the layout to go from the bottom, using constraints
        if (scrolledDownBy > 5) {
            OutlinedIconButton(
                onClick = {
                    coroutineScope.launch {
                        launch { refresh(null) }
                        if (scrolledDownBy > 20) {
                            listState.scrollToItem(0)
                        } else {
                            listState.animateScrollToItem(0)
                        }
                    }
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        1.dp
                    ).copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .size(50.dp)
                    .constrainAs(scrollButton) {
                        centerAround(buttonGuideline)
                        centerAround(leftGuideline)
                    }
            ) {
                if (scrolledDownBy > 20) {
                    Icon(
                        Icons.Default.KeyboardDoubleArrowUp,
                        "Scroll to top",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(5.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "Scroll to top",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(5.dp)
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { onPostButtonClicked() },
            modifier = Modifier
                .constrainAs(postButton) {
                    centerAround(buttonGuideline)
                    centerAround(rightGuideline)
                }
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Post a thing!",
                )
        }
    }

}


@Composable
@Preview
fun PreviewSkyline() {
    NimbusTheme {
        ScreenBody(modifier = Modifier.height(1000.dp)) {
            val posts = mutableListOf<SkylineItem>()
            for (i in 1..10) {
                posts.add(SkylineItem(testPost))
            }
            val postsFlow = MutableStateFlow(Skyline(posts, null))
            //SkylineFragment(postFlow = postsFlow.asStateFlow(), {})
        }
    }

}
