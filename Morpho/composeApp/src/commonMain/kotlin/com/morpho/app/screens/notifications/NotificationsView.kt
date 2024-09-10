package com.morpho.app.screens.notifications

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.jetpack.navigatorViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DraftPost
import com.morpho.app.model.bluesky.NotificationsListItem
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uidata.getPost
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.screens.base.tabbed.ThreadTab
import com.morpho.app.ui.common.BottomSheetPostComposer
import com.morpho.app.ui.common.ComposerRole
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.app.ui.elements.WrappedLazyColumn
import com.morpho.app.ui.elements.doMenuOperation
import com.morpho.app.ui.notifications.NotificationsElement
import com.morpho.app.ui.notifications.NotificationsFilterElement
import com.morpho.app.util.ClipboardManager
import com.morpho.butterfly.model.RecordUnion
import kotlinx.collections.immutable.persistentListOf
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
    val sm = navigatorViewModel { TabbedNotificationScreenModel() }
    val numberUnread by sm.uiState.value.numberUnread.collectAsState(0)
    var showSettings by remember { mutableStateOf(false) }
    val hasUnread = remember(numberUnread) { numberUnread > 0 }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
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
                markAsRead = { sm.markAllRead() }
            )
        },
        state = sm.uiState,
        modifier = Modifier,
        content = { insets, state ->

            val refreshing by remember { mutableStateOf(false)}
            val refreshState = rememberPullRefreshState(
                refreshing,
                {
                    sm.notifService.updateNotificationsSeen()
                    sm.refreshNotifications(AtCursor.EMPTY)
                }
            )
            val notifications by sm.uiState.value.notifications.collectAsState(persistentListOf())


            var repostClicked by remember { mutableStateOf(false)}
            var initialContent: BskyPost? by remember { mutableStateOf(null) }
            var showComposer by remember { mutableStateOf(false)}
            var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            // Probably pull this farther up,
            //      but this means if you don't explicitly cancel you don't lose the post
            var draft by remember{ mutableStateOf(DraftPost()) }

            val clipboardManager = getKoin().get<ClipboardManager>()
            val cursor by rememberUpdatedState(sm.uiState.value.cursor)

            LaunchedEffect(
                notifications.isNotEmpty() &&
                        !listState.canScrollForward &&
                        !refreshing
            ) {
                sm.refreshNotifications(cursor)
            }


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
                                    sm.uiState.value.filterState,
                                    onFilterClicked = {
                                        sm.notifService.updateFilter(it).invokeOnCompletion {
                                            // forcing a refresh should reload the list with new filters
                                            sm.refreshNotifications(cursor)
                                        }
                                    }
                                )
                                HorizontalDivider(Modifier.fillMaxWidth(),thickness = Dp.Hairline)
                            }
                        }
                    }
                    items(
                        count = notifications.size,
                        //key = { index -> notifications[index].hashCode() },
                        contentType = {
                            NotificationsListItem
                        }
                    ) { index ->
                        if (state != null) {
                            NotificationsElement(
                                item = notifications[index],
                                showPost = state.value.showPosts,
                                getPost = { getPost(it, sm.api)},
                                onUnClicked = { type, rkey ->
                                    sm.api.deleteRecord(type, rkey)
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
                                onLikeClicked = {
                                    sm.api.createRecord(RecordUnion.Like(it))
                                },
                                onPostClicked = {
                                    navigator.push(ThreadTab(it))
                                },
                                // If someone hides their read notifications,
                                // we don't want to just mark them as read unprompted.
                                // Might cause them to disappear unexpectedly.
                                readOnLoad = !state.value.filterState.value.showAlreadyRead,
                                markRead = { sm.markAsRead(it) }
                            )
                        }
                    }
                    item {
                        TextButton(
                            onClick = {
                                sm.refreshNotifications(cursor)
                            }
                        ) {
                            Text("Load More")
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
                            sm.viewModelScope.launch(Dispatchers.IO) {
                                val post = finishedDraft.createPost(sm.api)
                                sm.api.createRecord(RecordUnion.MakePost(post))
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