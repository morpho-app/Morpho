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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.morpho.app.model.bluesky.BskyLabelService
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uidata.LabelerEvent
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import kotlinx.collections.immutable.toImmutableList
import morpho.composeapp.generated.resources.Res
import morpho.composeapp.generated.resources.test_banner
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalResourceApi::class
)
@Composable
actual fun DetailedProfileFragment(
    profile: DetailedProfile,
    modifier: Modifier,
    myProfile: Boolean,
    isTopLevel: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClicked: () -> Unit,
    eventCallback: (Event) -> Unit,
) {
    val scrollState = rememberScrollState(0)
    val name = profile.displayName ?: profile.handle.handle
    val bannerHeight = if (scrollBehavior.state.collapsedFraction <= .2) {
        135.dp
    } else {
        (135.dp - (60 * scrollBehavior.state.collapsedFraction).dp)
    }
    val collapsed = scrollBehavior.state.collapsedFraction > 0.5
    LaunchedEffect(scrollState) {
        println("Banner Height: $bannerHeight")
        print("Collapsed: $collapsed")
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            //.requiredHeight(bannerHeight*2)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            //.border(1.dp, Color.Red)
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
                .requiredHeight(bannerHeight)//.border(1.dp, Color.Blue)
        )
        if(!collapsed){
            ProfileLabels(
                labels = profile.labels.toImmutableList(),
                modifier = Modifier
                    .constrainAs(labels) {
                        top.linkTo(anchor = parent.top, margin = 12.dp)
                        end.linkTo(anchor = parent.end, margin = 8.dp)
                    }//.border(1.dp, Color.Red)
            )
        }

        LargeTopAppBar(
            title = {
                ConstraintLayout(//constraintSet = ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
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
                            modifier = Modifier.zIndex(4f)
                                .constrainAs(buttons) {
                                    centerAround(centreGuide)
                                    end.linkTo(parent.end, 12.dp)
                                }
                        )
                        OutlinedAvatar(
                            url = profile.avatar.orEmpty(),
                            contentDescription = "Avatar for ${profile.displayName} ${profile.handle}",
                            modifier = Modifier.zIndex(4f)
                                .constrainAs(avatar) {
                                    centerAround(avatarGuide)
                                },
                            size = avatarSize,
                            outlineSize = 2.dp,
                            avatarShape = AvatarShape.Rounded
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
                                }//.border(1.dp, Color.Green),
                        ) {
                            Row {
                                OutlinedAvatar(
                                    url = profile.avatar.orEmpty(),
                                    contentDescription = "Avatar for ${profile.displayName} ${profile.handle}",
                                    size = avatarSize,
                                    outlineSize = 2.dp,
                                    avatarShape = AvatarShape.Rounded
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
                        modifier = Modifier.size(30.dp).zIndex(4f),

                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
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
                    top.linkTo(parent.top, 15.dp)
                }.zIndex(1f)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .wrapContentHeight(Alignment.Top)
                //.border(1.dp, Color.Magenta)
            ,
            windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
        )
        if(!collapsed){
            SelectionContainer(
                modifier = Modifier.fillMaxWidth().zIndex(0.5f)
                    .padding(end = 10.dp, top = bannerHeight+15.dp, start=100.dp)
                    .constrainAs(userStats) {
                        top.linkTo(appbar.bottom)
                        end.linkTo(parent.end)
                    }//.border(1.dp, Color.Red)
            ) {

                UserStatsFragment(
                    profile = profile,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                )
            }

            Column(
                modifier = Modifier
                    .constrainAs(text) {
                        top.linkTo(userStats.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(start = 20.dp, end = 20.dp, top = bannerHeight +40.dp)//.border(1.dp, Color.Yellow)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun LabelerProfileFragment(
    labeler: BskyLabelService,
    modifier: Modifier,
    isSubscribed: Boolean,
    isTopLevel: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClicked: () -> Unit,
    eventCallback: (Event) -> Unit,
) {
    val scrollState = rememberScrollState(0)
    val name = labeler.displayName ?: labeler.handle.handle
    val bannerHeight = if (scrollBehavior.state.collapsedFraction <= .2) {
        135.dp
    } else {
        (135.dp - (60 * scrollBehavior.state.collapsedFraction).dp)
    }
    val collapsed = scrollBehavior.state.collapsedFraction > 0.5
    LaunchedEffect(scrollState) {
        println("Banner Height: $bannerHeight")
        print("Collapsed: $collapsed")
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            //.requiredHeight(bannerHeight*2)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
        //.border(1.dp, Color.Red)
    ) {
        val (appbar, userStats, banner, labels, text, collapsedText) = createRefs()

        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(labeler.creator?.banner.orEmpty())
                .crossfade(true)
                .build(),
            placeholder = painterResource(Res.drawable.test_banner),
            contentDescription = "Profile Banner for ${labeler.displayName} ${labeler.handle}",
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
                .requiredHeight(bannerHeight)//.border(1.dp, Color.Blue)
        )

        LargeTopAppBar(
            title = {
                ConstraintLayout(//constraintSet = ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    val (avatar, buttons, info) = createRefs()
                    val expanded = scrollBehavior.state.collapsedFraction <= 0.5
                    val avatarSize = (80.dp - (30.0 * scrollBehavior.state.collapsedFraction).dp)
                    val centreGuideFraction = if(expanded) .6f else .5f
                    val avatarGuide = createGuidelineFromStart(.1f )
                    val centreGuide = createGuidelineFromTop(centreGuideFraction)

                    if(expanded){
                        LabelerButtons(
                            subscribed = isSubscribed,
                            modifier = Modifier.zIndex(4f)
                                .constrainAs(buttons) {
                                    centerAround(centreGuide)
                                    end.linkTo(parent.end, 12.dp)
                                },
                            onSubscribeClicked = {
                                eventCallback(LabelerEvent.Subscribe(labeler.did))
                            },
                            onUnsubscribeClicked = {
                                eventCallback(LabelerEvent.Unsubscribe(labeler.did))
                            },
                            onMenuClicked = {
                                // TODO: add labeler menu
                            },
                        )
                        OutlinedAvatar(
                            url = labeler.avatar.orEmpty(),
                            contentDescription = "Avatar for ${labeler.displayName} ${labeler.handle}",
                            modifier = Modifier.zIndex(4f)
                                .constrainAs(avatar) {
                                    centerAround(avatarGuide)
                                },
                            size = avatarSize,
                            outlineSize = 2.dp,
                            avatarShape = AvatarShape.Rounded
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
                                }//.border(1.dp, Color.Green),
                        ) {
                            Row {
                                OutlinedAvatar(
                                    url = labeler.avatar.orEmpty(),
                                    contentDescription = "Avatar for ${labeler.displayName} ${labeler.handle}",
                                    size = avatarSize,
                                    outlineSize = 2.dp,
                                    avatarShape = AvatarShape.Rounded
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
                                        text = " @${labeler.handle}",
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
                        modifier = Modifier.size(30.dp).zIndex(4f),

                        ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
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
                    top.linkTo(parent.top, 15.dp)
                }.zIndex(1f)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .wrapContentHeight(Alignment.Top)
            //.border(1.dp, Color.Magenta)
            ,
            windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
        )
        if(!collapsed){

            Column(
                modifier = Modifier
                    .constrainAs(text) {
                        top.linkTo(userStats.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(start = 20.dp, end = 20.dp, top = bannerHeight +40.dp)//.border(1.dp, Color.Yellow)
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
                        text = " @${labeler.handle}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                SelectionContainer {
                    RichTextElement(labeler.creator?.description.orEmpty())
                }
            }

        }
    }
}