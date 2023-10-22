package radiant.nimbus.screens.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.bsky.notification.ListNotificationsReason
import app.bsky.notification.ListNotificationsReason.FOLLOW
import app.bsky.notification.ListNotificationsReason.LIKE
import app.bsky.notification.ListNotificationsReason.MENTION
import app.bsky.notification.ListNotificationsReason.QUOTE
import app.bsky.notification.ListNotificationsReason.REPLY
import app.bsky.notification.ListNotificationsReason.REPOST
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Did
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.BskyNotification
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.NotificationsListItem
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.util.getFormattedDateTimeSince
import kotlin.math.min

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


@Composable
fun NotificationsElement(
    item: NotificationsListItem,
    showPost: Boolean = true,
    getPost: suspend (AtUri) -> Deferred<BskyPost?>,
) {
    var expand by remember { mutableStateOf(showPost) }
    var post: BskyPost? by remember { mutableStateOf(null)}
    val delta = remember { getFormattedDateTimeSince(item.notifications.first().indexedAt) }
    LaunchedEffect(expand) {
        when(val notif = item.notifications.first()) {
            is BskyNotification.Like -> post = getPost(notif.subject.uri).await()
            is BskyNotification.Follow -> {}
            is BskyNotification.Post -> {
                post = notif.post
                launch{
                    post = getPost(notif.uri).await()
                }
            }
            is BskyNotification.Repost -> post = getPost(notif.subject.uri).await()
            is BskyNotification.Unknown -> {
                if(notif.reasonSubject != null) {
                    post = getPost(notif.reasonSubject!!).await()
                }
            }
        }
    }
    val firstName = remember { if (item.notifications.first().author.displayName?.isNotEmpty() == true) {
            item.notifications.first().author.displayName.orEmpty()
        } else {
            item.notifications.first().author.handle.handle
        }}
    val number = remember { item.notifications.size }
    Row(
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ReasonIcon(reason = item.reason,
                Modifier.padding(top = 8.dp)
            )
            if(post != null) {
                if(expand) {
                    Spacer(Modifier.height(16.dp))
                } else {
                    Spacer(Modifier.height(4.dp))
                }
                IconToggleButton(
                    checked = expand,
                    onCheckedChange = {
                        expand = it
                    },
                ) {
                    if(expand) {
                        Icon(imageVector = Icons.Default.ExpandLess, contentDescription = "Hide Post")
                    } else {
                        Icon(imageVector = Icons.Default.ExpandMore, contentDescription = "Show Post")
                    }
                }
            }
        }

        Column(
            Modifier
                .padding(end = 4.dp)
                .wrapContentSize(Alignment.TopStart)
        ) {
            NotificationAvatarList(
                item = item,
            )
            NotificationText(reason = item.reason, number = number, name = firstName, delta = delta)
            if (expand && post != null) {
                // TODO: maybe do a more compact variant
                PostFragment(
                    post = post!!, elevate = true,
                )
            }
        }

    }
}

@Composable
fun ReasonIcon(
    reason: ListNotificationsReason,
    modifier: Modifier = Modifier
) {
    when(reason) {
        LIKE -> Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Like", modifier = modifier)
        REPOST -> Icon(imageVector = Icons.Default.Repeat, contentDescription = "Repost", modifier = modifier)
        FOLLOW -> Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Follow", modifier = modifier)
        MENTION -> Icon(imageVector = Icons.Default.Notifications, contentDescription = "Mention", modifier = modifier)
        REPLY -> Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Reply", modifier = modifier)
        QUOTE -> Icon(imageVector = Icons.Default.Repeat, contentDescription = "Quote", modifier = modifier)
    }
}

@Composable
fun NotificationText(
    reason: ListNotificationsReason,
    number: Int,
    name: String,
    delta: String,
    modifier: Modifier = Modifier,
) {
    val text = if (reason != REPLY && reason != QUOTE) {
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append(name)
            }
            if (number > 1) {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = MaterialTheme.typography.labelLarge.fontSize
                    )
                ) {
                    append(" and ")
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.labelLarge.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("${number - 1} other${if (number > 2) "s" else ""}")
                }
            }
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                )
            ) {
                when(reason) {
                    LIKE -> append(" liked your post  $delta")
                    REPOST -> append(" reposted your post  $delta")
                    FOLLOW -> append(" followed you  $delta")
                    MENTION -> append(" mentioned you  $delta")
                    else -> {}
                }
            }
        }
    } else null
    if (text != null) {
        Text(
            text = text,
            maxLines = 2,
            softWrap = true,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun NotificationAvatarList(
    item: NotificationsListItem,
    modifier: Modifier = Modifier,
    onClicked: (Did) -> Unit = {},
) {
    var expand by remember { mutableStateOf(false) }
    Column(
        Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if(!expand) {
                if (item.notifications.size > 1) {
                    item.notifications.subList(0, min(6, item.notifications.size-1)).forEach {
                        OutlinedAvatar(
                            url = it.author.avatar.orEmpty(),
                            onClicked = { onClicked(it.author.did) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                } else {
                    OutlinedAvatar(
                        url = item.notifications.first().author.avatar.orEmpty(),
                        onClicked = { onClicked(item.notifications.first().author.did) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            if (item.notifications.size > 1) {
                Spacer(modifier = Modifier.width(1.dp).weight(0.1f))
                IconToggleButton(
                    checked = expand,
                    onCheckedChange = {
                        expand = it
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .padding(end = 8.dp)
                ) {
                    if(expand) {
                        Icon(imageVector = Icons.Default.ExpandLess, contentDescription = "Hide Details")
                    } else {
                        Icon(imageVector = Icons.Default.ExpandMore, contentDescription = "Show Details")
                    }

                }
            }
        }
        if(expand) {
            item.notifications.forEach {
                Row(Modifier.padding(vertical = 2.dp)) {
                    OutlinedAvatar(
                        url = it.author.avatar.orEmpty(),
                        onClicked = { onClicked(it.author.did) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.2f),
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                if(it.author.displayName != null) append( "${it.author.displayName} ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.0f)
                                )
                            ) {
                                append("@${it.author.handle}")
                            }
                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(4.dp)
                            .alignByBaseline(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationsFilterElement(
    viewModel: NotificationsViewModel
) {
    FlowRow(

    ) {
        FilterChip(
            selected = viewModel.notificationsFilter.likes,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter.copy(likes = !viewModel.notificationsFilter.likes)
            },
            label = {
                Text(text = "Likes")
            },
            leadingIcon = if (viewModel.notificationsFilter.likes) {
                { Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                ) }
            } else null
        )
        FilterChip(
            selected = viewModel.notificationsFilter.reposts,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(likes = !viewModel.notificationsFilter.reposts)
            },
            label = {
                Text(text = "Reposts")
            },
            leadingIcon = if (viewModel.notificationsFilter.reposts) {
                { Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                ) }
            } else null
        )
        FilterChip(
            selected = viewModel.notificationsFilter.follows,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(likes = !viewModel.notificationsFilter.follows)
            },
            label = {
                Text(text = "Follows")
            },
            leadingIcon = if (viewModel.notificationsFilter.follows) {
                { Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                ) }
            } else null
        )
        FilterChip(
            selected = viewModel.notificationsFilter.mentions,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(likes = !viewModel.notificationsFilter.mentions)
            },
            label = {
                Text(text = "Mentions")
            },
            leadingIcon = if (viewModel.notificationsFilter.mentions) {
                { Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                ) }
            } else null

        )
        FilterChip(
            selected = viewModel.notificationsFilter.quotes,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(likes = !viewModel.notificationsFilter.quotes)
            },
            label = {
                Text(text = "Quotes")
            },
            leadingIcon = if (viewModel.notificationsFilter.quotes) {
                { Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                ) }
            } else null

        )
        FilterChip(
            selected = viewModel.notificationsFilter.replies,
            onClick = {
                viewModel.notificationsFilter = viewModel.notificationsFilter
                    .copy(likes = !viewModel.notificationsFilter.replies)
            },
            label = {
                Text(text = "Replies")
            },
            leadingIcon = if (viewModel.notificationsFilter.replies) {
                { Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                ) }
            } else null

        )
        FilterChip(
            selected = viewModel.state.hideRead,
            onClick = {
                viewModel.toggleUnread()
            },
            label = {
                Text(text = "Hide if read")
            },
            leadingIcon = if (viewModel.state.hideRead) {
                { Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                ) }
            } else null
        )
    }
}