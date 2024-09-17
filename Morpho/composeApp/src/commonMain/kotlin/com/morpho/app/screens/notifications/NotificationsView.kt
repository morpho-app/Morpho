package com.morpho.app.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.data.NotificationsListItem
import com.morpho.app.data.collectNotifications
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.uistate.NotificationsUIState
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.screens.base.tabbed.ThreadTab
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.ui.common.BottomSheetPostComposer
import com.morpho.app.ui.common.ComposerRole
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.app.ui.elements.WrappedLazyColumn
import com.morpho.app.ui.elements.doMenuOperation
import com.morpho.app.ui.notifications.NotificationsElement
import com.morpho.app.ui.notifications.NotificationsFilterElement
import com.morpho.app.util.ClipboardManager
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.compose.getKoin


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
       ExperimentalVoyagerApi::class
)
@Composable
fun TabScreen.NotificationViewContent(
    navigator: Navigator = LocalNavigator.currentOrThrow,

) {
    val sm = navigator.rememberNavigatorScreenModel { TabbedMainScreenModel() }
    val numberUnread = sm.unreadNotificationsCount().value
    var showSettings by remember { mutableStateOf(false) }
    val hasUnread = remember(numberUnread) { numberUnread > 0 }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val pager = sm.notificationsRaw.collectAsLazyPagingItems()
    var uiState by rememberSaveable { mutableStateOf(NotificationsUIState()) }
    val toMarkRead = mutableStateListOf<AtUri>()
    TabbedScreenScaffold(
        navBar = { navBar(navigator) },
        topContent = {
            NotificationsTopBar(
                navigator = navigator,
                onSettingsClicked = {
                    showSettings = it
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                                    },
                showSettings = showSettings,
                hasUnread = hasUnread,
                markAsRead = {
                    sm.updateSeenNotifications()
                }
            )
        },
        state = uiState,
        modifier = Modifier,
        content = { insets, state ->

            val refreshing by remember { mutableStateOf(false)}
            val refreshState = rememberPullRefreshState(
                refreshing,
                { sm.updateSeenNotifications()
                    pager.refresh() })


            var repostClicked by remember { mutableStateOf(false)}
            var initialContent: BskyPost? by remember { mutableStateOf(null) }
            var showComposer by remember { mutableStateOf(false)}
            var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            // Probably pull this farther up,
            //      but this means if you don't explicitly cancel you don't lose the post
            var draft by remember{ mutableStateOf(DraftPost()) }

            val clipboardManager = getKoin().get<ClipboardManager>()


            ConstraintLayout(
                Modifier
                    .fillMaxSize()
            ) {
                val (notificationList, refreshIndicator) = createRefs()

                // would love to pull this out rather than replicating it,
                // but the notification list in the non-tabbed view is likely to differ in more ways
                //      than just not having the integrated post composer
                WrappedLazyColumn(
                    state = listState,
                    contentPadding = insets,
                    modifier = Modifier
                        .pullRefresh(refreshState)
                        .constrainAs(notificationList) {
                            top.linkTo(parent.top)
                        },

                    ) {

                    item {
                        if (showSettings) {
                            Column {
                                HorizontalDivider(Modifier.fillMaxWidth(),thickness = Dp.Hairline)
                                NotificationsFilterElement(
                                    uiState.filterState,
                                    onFilterClicked = {
                                        uiState.filterState.value = it
                                        pager.refresh()
                                    }
                                )
                                HorizontalDivider(Modifier.fillMaxWidth(),thickness = Dp.Hairline)
                            }
                        }
                    }
                    when(val loadState = pager.loadState.refresh) {
                        is LoadStateError ->{
                            item { Text("Error: ${loadState.error}") }
                            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                TextButton(onClick = { pager.retry() }) {
                                    Text("Retry")
                                } } }
                        }
                        is LoadStateNotLoading -> {

                            val notifications = pager.collectNotifications(toMarkRead)
                            items(
                                count = pager.itemCount,
                                //key = { index -> notifications[index].hashCode() },
                                contentType = {
                                    NotificationsListItem
                                }
                            ) { index ->
                                if (state != null) {
                                    NotificationsElement(
                                        item = notifications[index],
                                        showPost = state.showPosts,
                                        getPost = { sm.getPost(it).getOrNull() },
                                        onUnClicked = { type, rkey ->
                                            sm.agent.deleteRecord(type, rkey)
                                        },
                                        onAvatarClicked = {
                                            navigator.push(ProfileTab(it))
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
                                        onMenuClicked = { option, post ->
                                            doMenuOperation(option, post,
                                                            clipboardManager = clipboardManager,
                                                            uriHandler = uriHandler
                                            ) },
                                        onLikeClicked = { sm.agent.like(it) },
                                        onPostClicked = {
                                            navigator.push(ThreadTab(it))
                                        },
                                        // If someone hides their read notifications,
                                        // we don't want to just mark them as read unprompted.
                                        // Might cause them to disappear unexpectedly.
                                        readOnLoad = !state.filterState.value.showAlreadyRead,
                                        markRead = { toMarkRead.add(it) },
                                        resolveHandle = { handle -> sm.agent.resolveHandle(handle).getOrNull() }
                                    )
                                }
                            }
                        }
                        else -> { item { LoadingCircle() } }
                    }
                }
                if(showComposer) {
                    BottomSheetPostComposer(
                        onDismissRequest = { showComposer = false },
                        sheetState = sheetState,
                        role = composerRole,
                        initialContent = initialContent,
                        draft = draft,
                        onCancel = {
                            showComposer = false
                            draft = DraftPost()
                        },
                        onSend = { finishedDraft ->
                            sm.screenModelScope.launch(Dispatchers.IO) {
                                val post = finishedDraft.createPost(sm.agent)
                                sm.agent.post(post)
                            }
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsTopBar(
    navigator: Navigator = LocalNavigator.currentOrThrow,
    onSettingsClicked : (Boolean) -> Unit = {},
    showSettings: Boolean = false,
    hasUnread: Boolean = false,
    markAsRead: () -> Unit = {}
) {
    var show by remember { mutableStateOf(showSettings) }
    CenterAlignedTopAppBar(
        title = { Text("Notifications") },
        navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            if(hasUnread) {
                TextButton(
                    onClick = markAsRead,
                ) {
                    Text(text = "Mark as Read")
                }
            }
            IconToggleButton(
                checked = show,
                onCheckedChange = {
                    show = it
                    onSettingsClicked(it)
                }
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}