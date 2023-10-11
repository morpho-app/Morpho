package radiant.nimbus.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import radiant.nimbus.R
import radiant.nimbus.api.Did
import radiant.nimbus.api.Handle
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.model.BskyLabel
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.model.Moment
import radiant.nimbus.screens.profile.ProfileTabs
import radiant.nimbus.screens.skyline.TopAppBarPreview
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.ui.elements.RichText
import radiant.nimbus.ui.theme.NimbusTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
public fun DetailedProfileFragment(
    profile: DetailedProfile,
    //onValueChange: (DetailedProfile) -> Unit,
    modifier: Modifier = Modifier,
    myProfile: Boolean = false,
    isTopLevel:Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 12.dp)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    .fillMaxWidth()
                    .height(155.dp)
                    .constrainAs(banner) {
                        top.linkTo(parent.top)
                    }

            )
            ProfileLabels(
                labels = profile.labels,
                modifier = Modifier
                    .constrainAs(labels) {
                        top.linkTo(anchor = parent.top, margin = 12.dp)
                        end.linkTo(anchor = parent.end, margin = 8.dp)
                    }
            )

            LargeTopAppBar(
                title = {
                    ConstraintLayout(//constraintSet = ,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                    ) {
                        val (avatar, buttons) = createRefs()
                        val avatarGuide = createGuidelineFromStart(.1f)
                        val centreGuide = createGuidelineFromTop(.6f)

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
                            following = profile.followedByMe,
                            modifier = Modifier
                                .constrainAs(buttons) {
                                    centerAround(centreGuide)
                                    end.linkTo(parent.end, 12.dp)
                                }
                        )
                    }
                },
                navigationIcon = {
                    if (isTopLevel) {
                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier
                                .size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                //tint =
                            )
                        }
                    }
                },
                actions = {

                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.constrainAs(appbar) {
                    top.linkTo(parent.top, (-45).dp)
                },
                windowInsets = WindowInsets.systemBars
            )
            SelectionContainer(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .constrainAs(userStats) {
                        bottom.linkTo(appbar.bottom, (-15).dp)
                        end.linkTo(parent.end)
                    }
            ) {
                UserStatsFragment(
                    profile = profile,
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                )
            }

        }

        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
        ) {
            val name = profile.displayName ?: profile.handle.handle
            SelectionContainer {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            SelectionContainer {
                Text(
                    text = " @${profile.handle}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SelectionContainer {
                RichText(profile)
            }
        }

    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DetailedProfilePreview() {
    var selectedTab by rememberSaveable { mutableStateOf(ProfileTabs.Posts) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    NimbusTheme {

        ScreenBody(
            topContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                ) {

                    DetailedProfileFragment(
                        profile = testProfile,
                        myProfile = true,
                        isTopLevel = true,
                        scrollBehavior = scrollBehavior
                    )
                    PreviewProfileTabRow(selectedTab) {
                        selectedTab = it
                    }
                }

            },
            contentWindowInsets = WindowInsets.systemBars,
            modifier = Modifier.consumeWindowInsets(WindowInsets.systemBars)
        ) {
            TopAppBarPreview()
        }
    }


}

val testProfile = DetailedProfile(
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
)