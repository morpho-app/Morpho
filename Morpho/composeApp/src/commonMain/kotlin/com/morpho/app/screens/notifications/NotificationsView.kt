package com.morpho.app.screens.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.bluesky.NotificationsListItem
import com.morpho.app.model.uistate.NotificationsUIState
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.ui.common.BottomSheetPostComposer
import com.morpho.app.ui.common.ComposerRole
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.app.ui.elements.WrappedLazyColumn
import com.morpho.app.ui.elements.doMenuOperation
import com.morpho.app.ui.notifications.NotificationsElement
import com.morpho.app.ui.notifications.NotificationsFilterElement
import com.morpho.app.ui.post.PlaceholderSkylineItem
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
    val sm = navigator.koinNavigatorScreenModel<TabbedMainScreenModel>()
    var showSettings by remember { mutableStateOf(false) }
    val hasUnread by sm.hasUnreadNotifications().collectAsState(initial = false)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val pager = sm.notifications.collectAsLazyPagingItems()
    var uiState by rememberSaveable { mutableStateOf(NotificationsUIState()) }
    val toMarkRead = mutableStateListOf<AtUri>()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    TabbedScreenScaffold(
        navBar = { navBar(navigator) },
        drawerState = drawerState,
        profile = sm.userProfile,
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
                    val refreshLoadState = pager.loadState.refresh
                    val appendLoadState = pager.loadState.append

                    when {
                        refreshLoadState is LoadStateError || appendLoadState is LoadStateError -> {
                            item { Text("$refreshLoadState\n$appendLoadState") }
                            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                TextButton(onClick = { pager.retry() }) {
                                    Text("Retry")
                                } } }
                        }
                        refreshLoadState is LoadStateLoading  -> { item { LoadingCircle() } }
                        else -> {


                            items(
                                count = pager.itemCount,
                                key = { pager.itemKey {
                                    it.hashCode()
                                }},
                                contentType = {
                                    NotificationsListItem
                                }
                            ) { index ->
                                if (state != null) {
                                    when(val item = pager[index]) {
                                        is NotificationsListItem -> {
                                            NotificationsElement(
                                                item = item,
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
                                                    doMenuOperation(
                                                        option, post,
                                                        clipboardManager = clipboardManager,
                                                        uriHandler = uriHandler
                                                    )
                                                },
                                                onLikeClicked = { sm.agent.like(it) },
                                                // If someone hides their read notifications,
                                                // we don't want to just mark them as read unprompted.
                                                // Might cause them to disappear unexpectedly.
                                                readOnLoad = !state.filterState.value.showAlreadyRead,
                                                markRead = { toMarkRead.add(it) },
                                                resolveHandle = { handle ->
                                                    sm.agent.resolveHandle(
                                                        handle
                                                    ).getOrNull()
                                                }
                                            )
                                        }
                                        null -> {
                                            PlaceholderSkylineItem()
                                        }
                                    }

                                }

                            }
                        }
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
        },
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
                },
                drawerState = drawerState,
            )
        },
        state = uiState,
        modifier = Modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsTopBar(
    navigator: Navigator = LocalNavigator.currentOrThrow,
    onSettingsClicked : (Boolean) -> Unit = {},
    showSettings: Boolean = false,
    hasUnread: Boolean = false,
    markAsRead: () -> Unit = {},
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    var show by remember { mutableStateOf(showSettings) }
    val scope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        title = { Text("Notifications") },
        navigationIcon = {
            IconButton(onClick = {
                if(drawerState.isClosed) scope.launch { drawerState.open() }
                else scope.launch { drawerState.close() }
            }) {
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