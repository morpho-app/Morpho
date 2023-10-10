package radiant.nimbus.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.components.Center
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.ui.common.SkylineFragment
import radiant.nimbus.ui.profile.DetailedProfileFragment
import radiant.nimbus.ui.profile.ProfileTabRow
import radiant.nimbus.ui.utils.DevicePreviews
import radiant.nimbus.ui.utils.FontScalePreviews

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
        navigator.navigateUp()
    }
    if (showLoadingScreen) {
        ScreenBody {
            Center {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (actor != null) {
            viewModel.getProfile(
                mainViewModel.apiProvider,
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
            if (mainViewModel.currentUser != null) {
                viewModel.getProfile(
                    mainViewModel.apiProvider,
                    AtIdentifier(mainViewModel.currentUser!!.did.did),
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

            ProfileView(
                model = viewModel,
                apiProvider = mainViewModel.apiProvider,
                navigator = navigator,
                navBar = { mainViewModel.navBar?.let { it() } },

            )
        }
    }

}

@Destination
@Composable
fun MyProfileScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    if (viewModel.useCachedProfile(mainViewModel.currentUser)) {
        LaunchedEffect(Unit) {
            viewModel.getProfileFeed(ProfileTabs.Posts, mainViewModel.apiProvider)
            viewModel.getProfileFeed(ProfileTabs.PostsReplies, mainViewModel.apiProvider)
            viewModel.getProfileFeed(ProfileTabs.Media, mainViewModel.apiProvider)
        }
        ProfileView(
            model = viewModel,
            apiProvider = mainViewModel.apiProvider,
            myProfile = true,
            navigator = navigator,
            navBar = { mainViewModel.navBar?.let { it() } },
        )
    }
}

enum class ProfileUIState {
    Error,
    Done,
    Loading
}

@Composable
fun ProfileView(
    model: ProfileViewModel,
    modifier: Modifier = Modifier,
    apiProvider: ApiProvider? = null,
    navigator: DestinationsNavigator? = null,
    myProfile: Boolean = false,
    isTopLevel: Boolean = true,
    navBar: @Composable () -> Unit = {},
){
    var selectedTab by rememberSaveable { mutableStateOf(ProfileTabs.Posts) }
    Scaffold(
        topBar = {
            if (isTopLevel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    model.state.profile.let {
                        it?.let { it1 ->
                            DetailedProfileFragment(
                                profile = it1,
                                myProfile = myProfile,
                                isTopLevel = true,
                            )
                        }
                    }
                    if (apiProvider != null) {
                        ProfileTabRow(selectedTab, apiProvider, model) {
                            selectedTab = it
                        }
                    }

                }
            }
        },
        bottomBar = {
            if (isTopLevel) {
                navBar()
            }
        },
        contentWindowInsets = WindowInsets.navigationBars,
    ) { insets ->
        Row (Modifier.consumeWindowInsets(insets)){
            if(!isTopLevel){
                navBar()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    model.state.profile.let {
                        it?.let { it1 ->
                            DetailedProfileFragment(
                                profile = it1,
                                myProfile = myProfile
                            )
                        }
                    }
                    if (apiProvider != null) {
                        ProfileTabRow(selectedTab, apiProvider, model) {
                            selectedTab = it
                        }
                    }
                }
            }
            when (selectedTab) {
                ProfileTabs.Posts -> {
                    if (navigator != null) {
                        SkylineFragment(
                            navigator = navigator,
                            postFlow = model.profilePosts,
                            contentPadding = insets,
                            onItemClicked = {},
                            refresh = {cursor ->
                                if (apiProvider != null) {
                                    model.getProfileFeed(selectedTab,apiProvider, cursor)
                                }
                            },
                            onUnClicked = {type, uri ->  apiProvider?.deleteRecord(type, uri = uri)},
                            onRepostClicked = {
                                apiProvider?.createRecord(RecordUnion.Repost(it))
                                /* TODO: Add dialog/quote post option */
                            },
                            onReplyClicked = { },
                            onMenuClicked = { },
                            onLikeClicked = {
                                apiProvider?.createRecord(RecordUnion.Like(it))
                            },
                        )
                    }
                }

                ProfileTabs.PostsReplies -> {
                    if (navigator != null) {
                        SkylineFragment(
                            navigator = navigator,
                            postFlow = model.profilePostsReplies,
                            contentPadding = insets,
                            onItemClicked = {},
                            refresh = {cursor ->
                                if (apiProvider != null) {
                                    model.getProfileFeed(selectedTab,apiProvider, cursor)
                                }
                            },
                            onUnClicked = {type, uri ->  apiProvider?.deleteRecord(type, uri = uri)},
                            onRepostClicked = {
                                apiProvider?.createRecord(RecordUnion.Repost(it))
                                /* TODO: Add dialog/quote post option */
                            },
                            onReplyClicked = { },
                            onMenuClicked = { },
                            onLikeClicked = {
                                apiProvider?.createRecord(RecordUnion.Like(it))
                            },
                        )
                    }
                }
                ProfileTabs.Media -> {
                    if (navigator != null) {
                        SkylineFragment(
                            navigator = navigator,
                            postFlow = model.profileMedia,
                            contentPadding = insets,
                            onItemClicked = {},
                            refresh = {cursor ->
                                if (apiProvider != null) {
                                    model.getProfileFeed(selectedTab,apiProvider, cursor)
                                }
                            },
                            onUnClicked = {type, uri ->  apiProvider?.deleteRecord(type, uri = uri)},
                            onRepostClicked = {
                                apiProvider?.createRecord(RecordUnion.Repost(it))
                                /* TODO: Add dialog/quote post option */
                            },
                            onReplyClicked = { },
                            onMenuClicked = { },
                            onLikeClicked = {
                                apiProvider?.createRecord(RecordUnion.Like(it))
                            },
                        )
                    }
                }
                ProfileTabs.Feeds -> {}
                ProfileTabs.Lists -> {}
            }
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