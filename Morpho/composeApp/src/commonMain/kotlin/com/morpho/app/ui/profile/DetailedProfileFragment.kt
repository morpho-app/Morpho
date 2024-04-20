package com.morpho.app.ui.profile


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant

import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.app.model.bluesky.BskyLabel
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.uidata.Moment
import com.morpho.app.ui.common.TopAppBarPreview
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.ui.theme.MorphoTheme
import androidx.constraintlayout.compose.ConstraintLayout
import morpho.composeapp.generated.resources.Res
import androidx.constraintlayout.compose.platform.annotation.SuppressLint
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import morpho.composeapp.generated.resources.test_banner
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalResourceApi::class
)
@Composable
public fun DetailedProfileFragment(
    profile: DetailedProfile,
    //onValueChange: (DetailedProfile) -> Unit,
    modifier: Modifier = Modifier,
    myProfile: Boolean = false,
    isTopLevel:Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()),
    onBackClicked: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val name = profile.displayName ?: profile.handle.handle
    val bannerHeight = if (scrollBehavior.state.collapsedFraction <= .2) {
        155.dp
    } else {
        (155.dp - (60 * scrollBehavior.state.collapsedFraction).dp)
    }
    val collapsed = scrollBehavior.state.collapsedFraction > 0.5

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        val (appbar, userStats, banner, labels, text, collapsedText) = createRefs()

        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(profile.banner.orEmpty())
                .crossfade(true)
                .build(),
            placeholder = painterResource(Res.drawable.test_banner),
            contentDescription = "Profile Banner for ${profile.displayName} ${profile.handle}",
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(banner) {
                    top.linkTo(parent.top)
                }
                .animateContentSize(
                    spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioNoBouncy
                    )
                )
                .requiredHeight(bannerHeight)
        )
        if(!collapsed){
            ProfileLabels(
                labels = profile.labels,
                modifier = Modifier
                    .constrainAs(labels) {
                        top.linkTo(anchor = parent.top, margin = 12.dp)
                        end.linkTo(anchor = parent.end, margin = 8.dp)
                    }
            )
        }

        LargeTopAppBar(
            title = {
                ConstraintLayout(//constraintSet = ,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val (avatar, buttons, info) = createRefs()
                    val expanded = scrollBehavior.state.collapsedFraction <= 0.5
                    val avatarSize = (80.dp - (30.0 * scrollBehavior.state.collapsedFraction).dp)
                    val centreGuideFraction = if(expanded) .6f else .5f
                    val avatarGuide = createGuidelineFromStart(.1f )
                    val centreGuide = createGuidelineFromTop(centreGuideFraction)

                    if(expanded){
                        ProfileButtons(
                            myProfile = myProfile,
                            following = profile.followedByMe,
                            modifier = Modifier
                                .constrainAs(buttons) {
                                    centerAround(centreGuide)
                                    end.linkTo(parent.end, 12.dp)
                                }
                        )
                        OutlinedAvatar(
                            url = profile.avatar.orEmpty(),
                            contentDescription = "Avatar for ${profile.displayName} ${profile.handle}",
                            modifier = Modifier
                                .constrainAs(avatar) {
                                    centerAround(avatarGuide)
                                },
                            size = avatarSize,
                            shape = AvatarShape.Rounded
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .height(avatarSize)
                                .constrainAs(info) {
                                    centerAround(centreGuide)
                                    start.linkTo(avatarGuide, (-20).dp)
                                },
                        ) {
                            Row {
                                OutlinedAvatar(
                                    url = profile.avatar.orEmpty(),
                                    contentDescription = "Avatar for ${profile.displayName} ${profile.handle}",
                                    size = avatarSize,
                                    shape = AvatarShape.Rounded
                                )
                                Column(
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.padding(start = 10.dp, end = 8.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = " @${profile.handle}",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                        }
                    }

                }
            },
            navigationIcon = {
                if (isTopLevel) {
                    IconButton(
                        onClick = { onBackClicked() },
                        modifier = Modifier.size(30.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            },
            actions = {},
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .constrainAs(appbar) {
                    top.linkTo(parent.top)
                }
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .wrapContentHeight(Alignment.Top)
            ,
            windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
        )
        if(!collapsed){
            SelectionContainer(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .constrainAs(userStats) {
                        top.linkTo(appbar.bottom, (-10).dp)
                        end.linkTo(parent.end)
                    }
            ) {

                UserStatsFragment(
                    profile = profile,
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                )
            }

            Column(
                modifier = Modifier
                    .constrainAs(text) {
                        top.linkTo(userStats.bottom, (-10).dp)
                        start.linkTo(parent.start)
                    }
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp)
            ) {

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
                    RichTextElement(profile.description.orEmpty())
                }
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

    MorphoTheme {

        Scaffold(
            topBar = {
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