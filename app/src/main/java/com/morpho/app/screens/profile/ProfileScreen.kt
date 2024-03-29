package com.morpho.app.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import com.atproto.repo.StrongRef
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import com.morpho.app.MainViewModel
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import com.morpho.app.components.Center
import com.morpho.app.components.ScreenBody
import com.morpho.app.extensions.activityViewModel
import com.morpho.app.model.BskyPost
import com.morpho.app.model.DraftPost
import com.morpho.app.ui.common.BottomSheetPostComposer
import com.morpho.app.ui.common.ComposerRole
import com.morpho.app.ui.common.RepostQueryDialog
import com.morpho.app.ui.common.SkylineFragment
import com.morpho.app.ui.profile.DetailedProfileFragment
import com.morpho.app.ui.profile.ProfileTabRow
import morpho.app.ui.utils.DevicePreviews
import morpho.app.ui.utils.FontScalePreviews

@OptIn(ExperimentalKStoreApi::class)
@Destination
@Composable
fun ProfileScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: ProfileViewModel = hiltViewModel(),
    actor: AtIdentifier? = null,
) {
    var showLoadingScreen by rememberSaveable { mutableStateOf(true)}
    var profileUIState: ProfileUIState by rememberSaveable { mutableStateOf(ProfileUIState.Loading)}

    BackHandler {
        navigator.popBackStack()
    }
    if (showLoadingScreen) {
        ScreenBody {
            Center {
                Text("Loading")
                //CircularProgressIndicator(
                //    color = MaterialTheme.colorScheme.onSurface,
                //)
            }
        }
        if (actor != null) {
            viewModel.getProfile(
                actor,
                {
                    profileUIState = ProfileUIState.Done
                    showLoadingScreen = false
                },
                {
                    profileUIState = ProfileUIState.Error
                    showLoadingScreen = false
                }
            )
        } else {
            if (mainViewModel.currentUser!= null) {
                viewModel.getProfile(
                    mainViewModel.currentUser!!.did,
                    {
                        profileUIState = ProfileUIState.Done
                        showLoadingScreen = false
                    },
                    {
                        profileUIState = ProfileUIState.Error
                        showLoadingScreen = false
                    }
                )
            }
        }

    } else {
        if(profileUIState == ProfileUIState.Error) {
                ScreenBody {
                    Center {
                        Text("Error Loading Profile")
                    }
                }
        } else  if (profileUIState == ProfileUIState.Done){

            ProfileViewPhone(
                model = viewModel,
                navigator = navigator,
                navBar = { mainViewModel.navBar?.let { it(4) } },

            )
        }
    }

}

@OptIn(ExperimentalKStoreApi::class)
@Destination
@Composable
fun MyProfileScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    if (viewModel.useCachedProfile(mainViewModel.currentUser)) {
        LaunchedEffect(Unit) {
            viewModel.getProfileFeed(ProfileTabs.Posts)
            viewModel.getProfileFeed(ProfileTabs.PostsReplies)
            viewModel.getProfileFeed(ProfileTabs.Media)
        }
        ProfileViewPhone(
            model = viewModel,
            myProfile = true,
            navigator = navigator,
            navBar = { mainViewModel.navBar?.let { it(4) } },
        )
    }
}

enum class ProfileUIState {
    Error,
    Done,
    Loading
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewPhone(
    model: ProfileViewModel,
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
    myProfile: Boolean = false,
    navBar: @Composable () -> Unit = {},
){
    val apiProvider = model.apiProvider
    var selectedTab by rememberSaveable { mutableStateOf(ProfileTabs.Posts) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        //flingAnimationSpec = exponentialDecay()
    )
    var repostClicked by remember { mutableStateOf(false)}
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false)}
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Probably pull this farther up,
    //      but this means if you don't explicitly cancel you don't lose the post
    var draft by remember{ mutableStateOf(DraftPost()) }

    val onProfileClicked:(actor: AtIdentifier, navigator: DestinationsNavigator) -> Unit = remember { return@remember model::onProfileClicked }
    val onItemClicked:(uri: AtUri, navigator: DestinationsNavigator) -> Unit = remember { return@remember model::onItemClicked }
    val onUnClicked:(type: RecordType, rkey: AtUri) -> Unit = remember { return@remember model.apiProvider::deleteRecord }
    val onLikeClicked:(ref: StrongRef) -> Unit = remember { return@remember {
        model.createRecord(RecordUnion.Like(it))
    } }
    val onReplyClicked:(post: BskyPost) -> Unit = remember {return@remember {
        initialContent = it
        composerRole = ComposerRole.Reply
        showComposer = true
    }}

    val onRepostClicked:(post: BskyPost) -> Unit = remember {return@remember {
        initialContent = it
        repostClicked = true
    }}

    val onPostButtonClicked:() -> Unit = remember {return@remember {
        composerRole = ComposerRole.StandalonePost
        showComposer = true
    }}

    ScreenBody(
        topContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                model.state.profile.let {
                    it?.let { it1 ->
                        DetailedProfileFragment(
                            profile = it1,
                            myProfile = myProfile,
                            isTopLevel = true,
                            scrollBehavior = scrollBehavior,
                            onBackClicked = {
                                navigator.popBackStack()
                            },
                        )
                    }
                }
                ProfileTabRow(
                    selected = selectedTab, model = model
                ) {
                    selectedTab = it
                }

            }
        },
        navBar = navBar,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.navigationBars,
    ) { insets ->
        when (selectedTab) {
            ProfileTabs.Posts -> {
                SkylineFragment(
                    postFlow = model.profilePosts,
                    contentPadding = insets,
                    onItemClicked = {onItemClicked(it, navigator)},
                    onProfileClicked = {onProfileClicked(it, navigator)},
                    refresh = {cursor ->
                        model.getProfileFeed(ProfileTabs.Posts, cursor)
                    },
                    onUnClicked = {type, rkey ->   onUnClicked(type, rkey)},
                    onRepostClicked = {onRepostClicked(it)},
                    onReplyClicked = {onReplyClicked(it)},
                    onMenuClicked = {
                    },
                    onLikeClicked = {onLikeClicked(it)},
                    onPostButtonClicked = {onPostButtonClicked()},
                    isProfileFeed = true
                )
            }

            ProfileTabs.PostsReplies -> {
                SkylineFragment(
                    postFlow = model.profilePostsReplies,
                    contentPadding = insets,
                    onItemClicked = {onItemClicked(it, navigator)},
                    onProfileClicked = {onProfileClicked(it, navigator)},
                    refresh = {cursor ->
                        model.getProfileFeed(ProfileTabs.PostsReplies, cursor)
                    },
                    onUnClicked = {type, rkey ->   onUnClicked(type, rkey)},
                    onRepostClicked = {onRepostClicked(it)},
                    onReplyClicked = {onReplyClicked(it)},
                    onMenuClicked = {
                    },
                    onLikeClicked = {onLikeClicked(it)},
                    onPostButtonClicked = {onPostButtonClicked()},
                    isProfileFeed = true
                )
            }
            ProfileTabs.Media -> {
                SkylineFragment(
                    postFlow = model.profileMedia,
                    contentPadding = insets,
                    onItemClicked = {onItemClicked(it, navigator)},
                    onProfileClicked = {onProfileClicked(it, navigator)},
                    refresh = {cursor ->
                        model.getProfileFeed(ProfileTabs.Media, cursor)
                    },
                    onUnClicked = {type, rkey ->   onUnClicked(type, rkey)},
                    onRepostClicked = {onRepostClicked(it)},
                    onReplyClicked = {onReplyClicked(it)},
                    onMenuClicked = {
                    },
                    onLikeClicked = {onLikeClicked(it)},
                    onPostButtonClicked = {onPostButtonClicked()},
                    isProfileFeed = true
                )
            }
            ProfileTabs.Feeds -> {}
            ProfileTabs.Lists -> {}
        }
        if(repostClicked) {
            RepostQueryDialog(
                onDismissRequest = {
                    showComposer = false
                    repostClicked = false
                },
                onRepost = {
                    repostClicked = false
                    initialContent?.let { post ->
                        RecordUnion.Repost(
                            StrongRef(post.uri,post.cid)
                        )
                    }?.let { apiProvider.createRecord(it) }
                },
                onQuotePost = {
                    showComposer = true
                    repostClicked = false
                }
            )
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
    }
}


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
@DevicePreviews
@FontScalePreviews
fun ProfilePreview(){
   /* ProfileView(
        ProfileState(
            AtIdentifier("nonbinary.computer"),
            DetailedProfile(
                did = Did("did:plc:yfvwmnlztr4dwkb7hwz55r2g"),
                handle = Handle("nonbinary.computer"),
                displayName = "Orual",
                avatar = "https://av-cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreifpzcenp6rhmxohv3kkez4uv4ldjphiysmju6scwgne34nb245wra@jpeg",
                banner = "https://av-cdn.bsky.app/img/banner/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreihed7ohctbeer7l5fmpu4dqwxarfvxiyoik5tyd6lhelvp76yr2wm@jpeg",
                description = "Person who does computer and music things.\nthey/she",
                followersCount = 592,
                followsCount = 470,
                indexedAt = Moment(Instant.parse("2023-05-24T20:04:20-05")),
                labels = persistentListOf(
                    BskyLabel("testLabel1"),
                    BskyLabel("testLabel2")
                ),
                postsCount = 2743,
                mutedByMe = false,
                followingMe = false,
                followedByMe = false,
            ),
            isLoading = false
        ),
    )*/
}