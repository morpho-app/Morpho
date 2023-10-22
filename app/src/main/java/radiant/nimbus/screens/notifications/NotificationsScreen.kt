package radiant.nimbus.screens.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.BskyPost
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.ui.notifications.NotificationsElement
import radiant.nimbus.ui.notifications.NotificationsFilterElement

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
    NotificationsView(
        viewModel, navigator,
        navBar = {mainViewModel.navBar?.let {it(5)}},
        getPost = { viewModel.getPost(it, mainViewModel.apiProvider) },
        mainButton = { onClicked ->
            OutlinedAvatar(url = mainViewModel.currentUser?.avatar.orEmpty(),
                modifier = Modifier.size(40.dp),
                onClicked = onClicked,
                outlineSize = 0.dp
            )
        },
        apiProvider = mainViewModel.apiProvider
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
    apiProvider: ApiProvider,
){
    var showSettings by remember { mutableStateOf(false)}
    var unread by remember { mutableStateOf(viewModel.state.numberUnread > 0)}
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(!listState.canScrollForward) {
        viewModel.getNotifications(apiProvider, viewModel.state.cursor)
    }
    val refreshState = rememberPullRefreshState(refreshing,
        {   viewModel.updateSeen(apiProvider)
            viewModel.getNotifications(apiProvider, null)
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
                    if(unread) {
                        TextButton(
                            onClick = {
                                viewModel.updateSeen(apiProvider)
                                unread = false
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
                        NotificationsFilterElement(viewModel = viewModel)
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
                                getPost = { getPost(it) }
                            )
                        }
                    } else {
                        NotificationsElement(
                            item = notifications.notificationsList[index],
                            showPost = viewModel.state.showPosts,
                            getPost = { getPost(it) }
                        )
                    }
                }
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


