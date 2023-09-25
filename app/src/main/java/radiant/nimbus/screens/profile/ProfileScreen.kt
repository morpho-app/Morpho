package radiant.nimbus.screens.profile

import android.view.Menu
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.FeedViewPost
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponse
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import radiant.nimbus.MainViewModel
import radiant.nimbus.R
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.components.Center
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.BskyLabel
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.model.Moment
import radiant.nimbus.ui.common.OutlinedAvatar
import radiant.nimbus.ui.common.SkylineFragment
import radiant.nimbus.ui.common.UserStatsFragment
import radiant.nimbus.ui.elements.RichText
import radiant.nimbus.ui.utils.DevicePreviews
import radiant.nimbus.ui.utils.FontScalePreviews
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle
import sh.christian.ozone.api.response.AtpResponse

@Destination
@Composable
fun ProfileScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: ProfileViewModel = hiltViewModel(),
    actor: AtIdentifier? = null,
) {
    var myProfile by rememberSaveable(viewModel.state) { mutableStateOf(false) }
    var profileUIState: ProfileUIState by rememberSaveable(viewModel.state) { mutableStateOf(ProfileUIState.Loading)}
    if (viewModel.state.isLoading) {
        if (actor == null) {
            if (mainViewModel.currentUser != null) {
                myProfile = true
                viewModel.getProfile(
                    mainViewModel.apiProvider,
                    mainViewModel.currentUser!!,
                    { profileUIState = ProfileUIState.Done },
                    { profileUIState = ProfileUIState.Error }
                )
            }
        } else {
            myProfile = actor == mainViewModel.currentUser
            viewModel.getProfile(
                mainViewModel.apiProvider,
                actor,
                {profileUIState = ProfileUIState.Done},
                {profileUIState = ProfileUIState.Error}
            )
        }
    }
    when (profileUIState) {
        ProfileUIState.Error -> {
            ScreenBody {
                Center {
                    Text("Error Loading Profile")
                }
            }
        }
        ProfileUIState.Done -> ProfileView(
            state = viewModel.state,
            apiProvider = mainViewModel.apiProvider,
            myProfile = myProfile
        )
        ProfileUIState.Loading -> {
            ScreenBody {
                Center {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                }
            }
        }
    }
}

enum class ProfileUIState {
    Error,
    Done,
    Loading
}

enum class ProfileTabs {
    Posts,
    PostsReplies,
    Media,
    Feeds,
    Lists,
}

@Composable
fun ProfileView(
    state: ProfileState,
    modifier: Modifier = Modifier,
    feed: ImmutableList<FeedViewPost> = persistentListOf(),
    apiProvider: ApiProvider? = null,
    navigator: DestinationsNavigator? = null,
    myProfile: Boolean = false,
){
    var selectedTab = rememberSaveable { mutableStateOf(ProfileTabs.Posts) }
    if (!state.isLoading) {
        Scaffold(
            topBar = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    state.profile.let {
                        it?.let { it1 ->
                            DetailedProfileFragment(
                                profile = it1,
                                myProfile = myProfile
                            )
                        }
                    }
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.value.ordinal,
                        edgePadding = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val tabModifier = Modifier
                            .padding(
                                bottom = 12.dp,
                                top = 6.dp,
                                start = 6.dp,
                                end = 6.dp
                            )
                        Tab(
                            selected = true,
                            onClick = { selectedTab.value = ProfileTabs.Posts },

                            ) {
                            Text(
                                text = "Posts",
                                modifier = tabModifier
                            )
                        }

                        Tab(
                            selected = false,
                            onClick = { selectedTab.value = ProfileTabs.PostsReplies },

                            ) {
                            Text(
                                text = "Posts & Replies",
                                modifier = tabModifier
                            )
                        }
                        //Spacer(modifier = Modifier.width(2.dp))
                        Tab(
                            selected = false,
                            onClick = { selectedTab.value = ProfileTabs.Media },
                        ) {
                            Text(
                                text = "Media",
                                modifier = tabModifier
                            )
                        }

                        Tab(
                            selected = false,
                            onClick = { selectedTab.value = ProfileTabs.Feeds },
                        ) {
                            Text(
                                text = "Feeds",
                                modifier = tabModifier
                            )
                        }

                        Tab(
                            selected = false,
                            onClick = { selectedTab.value = ProfileTabs.Lists },
                        ) {
                            Text(
                                text = "Lists",
                                modifier = tabModifier
                            )
                        }
                    }
                }

            }
        ) { contentPadding ->

            when (selectedTab.value) {
                ProfileTabs.Posts -> {

                    SkylineFragment(
                        postList = persistentListOf(),
                        modifier = Modifier.padding(contentPadding),
                        onItemClicked = {}
                    )
                }

                ProfileTabs.PostsReplies -> {
                    SkylineFragment(
                        postList = persistentListOf(),
                        modifier = Modifier.padding(contentPadding),
                        onItemClicked = {}
                    )
                }
                ProfileTabs.Media -> {
                    val params = GetPostThreadQueryParams(
                        AtUri("at://bitdizzy.bsky.social/app.bsky.feed.post/3k7kimkoejx27"),
                        10, 10)
                    var postThread: AtpResponse<GetPostThreadResponse>? = null
                    LaunchedEffect(selectedTab) {
                        //postThread = apiProvider?.api?.getPostThread(params)

                    }
                    //Text(text = Json.encodeToString(value = postThread?.maybeResponse()?.thread))
                }
                ProfileTabs.Feeds -> TODO()
                ProfileTabs.Lists -> TODO()
            }

        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
public fun DetailedProfileFragment(
    profile: DetailedProfile,
    //onValueChange: (DetailedProfile) -> Unit,
    modifier: Modifier = Modifier,
    myProfile: Boolean = false,
) {

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 12.dp)
    ){
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val (appbar, userStats, banner, labels) = createRefs()
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.banner.orEmpty())
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.test_banner),
                    contentDescription = "Profile Banner for ${profile.displayName} ${profile.handle}",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier
                        // Set image size to 40 dp
                        .fillMaxWidth()
                        .height(120.dp)
                        .constrainAs(banner) {
                            top.linkTo(parent.top)
                        }

                )
                SelectionContainer {
                    ProfileLabels(
                        labels = profile.labels,
                        modifier = Modifier
                            .constrainAs(labels) {
                                top.linkTo(anchor = parent.top, margin = 12.dp)
                                end.linkTo(anchor = parent.end, margin = 8.dp)
                            }
                    )
                }
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                LargeTopAppBar(
                    title = {
                        ConstraintLayout(//constraintSet = ,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp)
                        ){
                            val (avatar,buttons) = createRefs()
                            val avatarGuide = createGuidelineFromStart(.1f)
                            val centreGuide =createGuidelineFromTop(.6f)

                            OutlinedAvatar(
                                url = profile.avatar.orEmpty(),
                                contentDescription = "Avatar for ${profile.displayName} ${profile.handle}",
                                modifier = Modifier
                                    .size(80.dp)
                                    .constrainAs(avatar) {
                                        centerAround(avatarGuide)

                                    }
                            )
                            ProfileButtons(
                                myProfile = myProfile,
                                modifier = Modifier
                                    .constrainAs(buttons) {
                                        centerAround(centreGuide)
                                        end.linkTo(parent.end, 12.dp)
                                    }
                            )
                        }
                    },
                    navigationIcon = {

                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier
                                .size(30.dp)


                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                //tint =
                            )
                        }
                    },
                    actions = {

                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.constrainAs(appbar) {

                    }
                )
                SelectionContainer {
                    UserStatsFragment(
                        profile = profile,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .widthIn(max = 250.dp)
                            .constrainAs(userStats) {
                                bottom.linkTo(appbar.bottom, (-15).dp)
                                end.linkTo(parent.end)
                            }
                    )
                }

            }
        SelectionContainer {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 0.dp)
            ) {
                val name = profile.displayName ?: profile.handle.handle
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " @${profile.handle}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(10.dp))
                RichText(profile)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileLabels(
    modifier: Modifier = Modifier,
    labels: ImmutableList<BskyLabel>
) {
    FlowRow (
        modifier = modifier
    ){
        labels.forEach {
            ProfileLabel(
                label = it,
                modifier = modifier

            )
        }
    }
}

@Composable
fun ProfileButtons(
    modifier: Modifier = Modifier,
    myProfile: Boolean = false
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 2.dp)
    ) {
        if (myProfile){
            EditProfileButton(
                modifier = modifier
                    .padding(horizontal = 5.dp)
            )
        } else {
            FollowButton(
                modifier = modifier
                    .padding(horizontal = 5.dp)
            )
        }
        ProfileMenuButton()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileLabel(
    modifier: Modifier = Modifier,
    label:BskyLabel
) {
    InputChip(
        selected = true,
        onClick = { /*TODO*/ },
        label = {
            Text(
                text = label.value,
                style = MaterialTheme.typography.labelSmall,
                modifier = modifier
            )
                },
        modifier = Modifier
            .heightIn(min = 20.dp, max = 48.dp)
            .padding(horizontal = 6.dp)

    )
}

@Composable
fun ProfileMenuButton(
    modifier: Modifier = Modifier,
    menu: Menu? = null,
) {
    SmallFloatingActionButton(
        onClick = { /*TODO*/ },
        shape = ButtonDefaults.filledTonalShape,
        modifier = modifier
            .sizeIn(
                minWidth = 25.dp,
                maxWidth = 30.dp,
                minHeight = 25.dp,
                maxHeight = 30.dp
            )
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Menu",
            modifier = Modifier
                .size(18.dp)
        )
    }
}


@Composable
fun EditProfileButton(
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        text = {
            Text(
                text = "Follow",
                style = MaterialTheme.typography.labelLarge,
                fontSize = MaterialTheme.typography.labelLarge
                    .fontSize.times(0.9)
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Follow",
                modifier = Modifier.size(19.dp)
            )
        },
        onClick = { /*TODO*/ },
        shape = ButtonDefaults.filledTonalShape,
        modifier = modifier
            .heightIn(min = 30.dp, max = 48.dp)
    )
}

@Composable
fun FollowButton(
    modifier: Modifier = Modifier,
    following: Boolean = false,
) {

    val label: String = if (following) {
        "Following"
    } else {
        "Follow"
    }

    ExtendedFloatingActionButton(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontSize = MaterialTheme.typography.labelLarge
                    .fontSize.times(0.9)
            )
        },
        icon = {

            Icon(
                imageVector =
                    if (following) {
                        Icons.Filled.Check
                    } else {
                        Icons.Filled.Add
                    },
                contentDescription = "$label Icon",
                modifier = Modifier.size(19.dp)
            )
        },
        onClick = { /*TODO*/ },
        shape = ButtonDefaults.filledTonalShape,
        modifier = modifier
            .heightIn(min = 30.dp, max = 48.dp)
    )
}

@Composable
@DevicePreviews
@FontScalePreviews
fun ProfilePreview(){
    ProfileView(
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
    )
}