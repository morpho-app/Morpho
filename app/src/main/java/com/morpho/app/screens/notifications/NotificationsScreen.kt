package morpho.app.screens.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import com.morpho.app.MainViewModel
import morpho.app.api.AtUri
import morpho.app.api.model.RecordUnion
import com.morpho.app.apiProvider
import morpho.app.components.ScreenBody
import morpho.app.extensions.activityViewModel
import morpho.app.model.BskyPost
import morpho.app.model.DraftPost
import morpho.app.screens.destinations.PostThreadScreenDestination
import morpho.app.screens.destinations.ProfileScreenDestination
import morpho.app.ui.common.BottomSheetPostComposer
import morpho.app.ui.common.ComposerRole
import morpho.app.ui.elements.OutlinedAvatar
import morpho.app.ui.notifications.NotificationsElement
import morpho.app.ui.notifications.NotificationsFilterElement

@Destination
@Composable
fun NotificationsScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    BackHandler {
        navigator.popBackStack()
    }
    viewModel.connectNotifications(mainViewModel.unreadNotifications)
    NotificationsView(
        viewModel, navigator,
        navBar = {mainViewModel.navBar?.let {it(5)}},
        getPost = { viewModel.getPost(it) },
        mainButton = { onClicked ->
            OutlinedAvatar(url = mainViewModel.currentUser?.avatar.orEmpty(),
                modifier = Modifier.size(40.dp),
                onClicked = onClicked,
                outlineSize = 0.dp
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun NotificationsView(
    viewModel: NotificationsViewModel,
    navigator: DestinationsNavigator,
    navBar: @Composable() () -> Unit = {},
    mainButton: @Composable() ((()->Unit) -> Unit)? = null,
    onButtonClicked: () -> Unit = {},
    getPost: suspend (AtUri) -> Deferred<BskyPost?>,
){
    var showSettings by remember { mutableStateOf(false)}
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    var hasUnread = remember { unreadCount > 0 }
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var repostClicked by remember { mutableStateOf(false)}
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false)}
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Probably pull this farther up,
    //      but this means if you don't explicitly cancel you don't lose the post
    var draft by remember{ mutableStateOf(DraftPost()) }
    val apiProvider = LocalContext.current.apiProvider

    LaunchedEffect(!listState.canScrollForward) {
        viewModel.getNotifications(viewModel.state.cursor)
    }
    val refreshState = rememberPullRefreshState(refreshing,
        {   viewModel.updateSeen()
            viewModel.getNotifications(null)
            if (viewModel.unreadCount.value > 0) hasUnread = true
        })
    ScreenBody(
        modifier = Modifier.fillMaxSize(),
        navBar = navBar,
        topContent = {
            TopAppBar(
                title = {
                    Text(text = " Notifications")
                },
                navigationIcon = {
                    Surface(
                        tonalElevation = 3.dp,
                        shadowElevation = 2.dp,
                        //modifier = Modifier.offset(y = (-5).dp),
                        shape = MaterialTheme.shapes.small.copy(
                            bottomStart = CornerSize(0.dp),
                            //topStart = CornerSize(0.dp),
                            topEnd = CornerSize(0.dp),
                        )
                    ) {
                        if (mainButton != null) {
                            mainButton(onButtonClicked)
                        } else {
                            IconButton(
                                onClick = onButtonClicked,
                                modifier = Modifier
                                    .padding(bottom = 5.dp, top = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .size(30.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if(hasUnread) {
                        TextButton(
                            onClick = {
                                viewModel.updateSeen()
                                hasUnread = false
                            },
                        ) {
                            Text(text = "Mark as Read")
                        }
                    }
                    IconToggleButton(
                        checked = showSettings,
                        onCheckedChange = {
                            showSettings = it
                            scope.launch { listState.animateScrollToItem(0) }
                                          },
                    ) {
                        if(showSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Hide Notifications Settings"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Show Notifications Settings"
                            )
                        }

                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
    ) { padding ->
        ConstraintLayout(
            Modifier
                .fillMaxSize()
        ) {
            val (notificationList, refreshIndicator) = createRefs()
            LazyColumn(
                state = listState,
                contentPadding = padding,
                modifier = Modifier
                    .pullRefresh(refreshState)
                    .constrainAs(notificationList) {
                        top.linkTo(parent.top)
                    },

                ) {
                if (showSettings) {
                    item {
                        Column {
                            HorizontalDivider(Modifier.fillMaxWidth(),thickness = Dp.Hairline)
                            NotificationsFilterElement(viewModel = viewModel)
                            HorizontalDivider(Modifier.fillMaxWidth(),thickness = Dp.Hairline)
                        }
                    }
                }
                items(
                    count = notifications.notificationsList.size,
                    key = {
                        notifications.notificationsList[it].hashCode()
                    },
                    contentType = {
                        notifications.notificationsList[it]
                    }
                ) { index ->
                    if (viewModel.state.hideRead) {
                        if (!notifications.notificationsList[index].isRead) {
                            NotificationsElement(
                                item = notifications.notificationsList[index],
                                showPost = viewModel.state.showPosts,
                                getPost = { getPost(it) },
                                onUnClicked = {type, rkey ->  apiProvider.deleteRecord(type, rkey)},
                                onAvatarClicked = {
                                    navigator.navigate(
                                        ProfileScreenDestination(it)
                                    )
                                },
                                onRepostClicked = {
                                    initialContent = it
                                    repostClicked = true
                                },
                                onReplyClicked = {
                                    initialContent = it
                                    composerRole = ComposerRole.Reply
                                    showComposer = true
                                },
                                onMenuClicked = { },
                                onLikeClicked = {
                                    apiProvider.createRecord(RecordUnion.Like(it))
                                },
                                onPostClicked = {
                                    navigator.navigate(PostThreadScreenDestination(it))
                                }
                            )
                        }
                    } else {
                        NotificationsElement(
                            item = notifications.notificationsList[index],
                            showPost = viewModel.state.showPosts,
                            getPost = { getPost(it) },
                            onUnClicked = {type, rkey ->  apiProvider.deleteRecord(type, rkey)},
                            onAvatarClicked = {
                                navigator.navigate(
                                    ProfileScreenDestination(it)
                                )
                            },
                            onRepostClicked = {
                                initialContent = it
                                repostClicked = true
                            },
                            onReplyClicked = {
                                initialContent = it
                                composerRole = ComposerRole.Reply
                                showComposer = true
                            },
                            onMenuClicked = { },
                            onLikeClicked = {
                                apiProvider.createRecord(RecordUnion.Like(it))
                            },
                            onPostClicked = {
                                navigator.navigate(PostThreadScreenDestination(it))
                            }
                        )
                    }
                }
            }
            if(showComposer) {
                BottomSheetPostComposer(
                    onDismissRequest = { showComposer = false },
                    sheetState = sheetState,
                    role = composerRole,
                    //modifier = Modifier.padding(insets),
                    initialContent = initialContent,
                    draft = draft,
                    onCancel = {
                        showComposer = false
                        draft = DraftPost()
                    },
                    onSend = {
                        apiProvider.createRecord(RecordUnion.MakePost(it))
                        showComposer = false
                    },
                    onUpdate = { draft = it }
                )

            }


            PullRefreshIndicator(refreshing, refreshState, Modifier.constrainAs(refreshIndicator) {
                top.linkTo(parent.top)
                centerHorizontallyTo(parent)
            }, backgroundColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary)
        }

    }
}

@Composable
@Preview(showBackground = true)
fun NotificationsPreview(){
    //NotificationsView()
}


