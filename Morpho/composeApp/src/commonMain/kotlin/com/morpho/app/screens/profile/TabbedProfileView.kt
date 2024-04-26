package com.morpho.app.screens.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import coil3.annotation.ExperimentalCoilApi
import com.morpho.app.model.bluesky.BskyLabelService
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.Profile
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedProfileScreenScaffold
import com.morpho.app.ui.common.TabbedSkylineFragment
import com.morpho.app.ui.profile.DetailedProfileFragment
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import cafe.adriel.voyager.navigator.tab.Tab as NavTab

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabbedProfileTopBar(
    profile: Profile?,
    ownProfile: Boolean,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()),
    tabs: ImmutableList<ProfileSkylineTab>,
    onBackClicked: () -> Unit,
) {
    val selectedTabIndex = tabs.indexOfFirst { it.options.index == 0.toUShort() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        when(profile != null) {
            true -> {
                when(profile) {
                    is DetailedProfile -> DetailedProfileFragment(
                        profile = profile,
                        myProfile = ownProfile,
                        isTopLevel = true,
                        scrollBehavior = scrollBehavior,
                        onBackClicked = onBackClicked,
                    )
                    is BskyLabelService -> { TODO("Make different title card for label services")}
                    else -> { /* Shouldn't happen */ }
                }

                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    tabs.forEach { tab -> ProfileTabItem(tab) }
                }
            }
            false -> {
                // Loading
            }
        }

    }
}

@Composable
fun ProfileTabItem(tab: ProfileSkylineTab) {
    val navigator = LocalTabNavigator.current
    val title = rememberSaveable {
        tab.state?.value?.feed?.title.orEmpty()
    }
    val tabModifier = Modifier
        .padding(
            bottom = 12.dp,
            top = 6.dp,
            start = 6.dp,
            end = 6.dp
        )
    Tab(
        selected = navigator.current == tab,
        onClick = {
            navigator.current = tab
        },
    ) {
        Text(
            text = title,
            modifier = tabModifier
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabScreen.TabbedProfileContent(
    ownProfile: Boolean,
    sm: TabbedProfileViewModel = LocalNavigator.currentOrThrow.getNavigatorScreenModel<TabbedProfileViewModel>(),
) {

    val navigator = LocalNavigator.currentOrThrow

    LifecycleEffect(
        onStarted = {
            sm.initProfile()
        },
        onDisposed = {},
    )
    /*val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        //flingAnimationSpec = exponentialDecay()
    )*/
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var insets = WindowInsets.navigationBars.asPaddingValues()
    val tabs = rememberSaveable(
        sm.tabFlow.value,
        sm.profileUiState.loadingState,
        sm.profileUiState.tabs.value.size
    ) {
        List(sm.profileUiState.tabs.value.size) { index ->
            ProfileSkylineTab(
                index = index.toUShort(),
                screenModel = sm,
                state = sm.profileUiState.tabStates[index] as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>?,
                paddingValues = insets,
                ownProfile = ownProfile,
            )
        }.toImmutableList()
    }
    val tabsCreated = rememberSaveable(tabs.size, sm.profileUiState.loadingState) {
        tabs.isNotEmpty() && sm.profileUiState.loadingState == UiLoadingState.Idle
    }
    if (tabsCreated) {
        TabNavigator(
            tab = tabs.first(),
            tabDisposable = { TabDisposable(navigator = it, tabs = tabs) }
        ) {
            TabbedProfileScreenScaffold(
                navBar = { navBar(navigator) },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topContent = {
                    TabbedProfileTopBar(
                        sm.profileState?.profile, true, scrollBehavior, tabs.toImmutableList(),
                        onBackClicked = { navigator.pop() }
                    )
                },
                content = {
                    insets = it
                    CurrentTab()
                },
                scrollBehavior = scrollBehavior,
            )
        }
    } else {
        TabbedProfileScreenScaffold(
            navBar = { navBar(navigator) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topContent = {
                if (sm.profileState?.profile != null) {
                    DetailedProfileFragment(
                        profile = sm.profileState?.profile!! as DetailedProfile,
                        myProfile = ownProfile,
                        isTopLevel = true,
                        scrollBehavior = scrollBehavior,
                        onBackClicked = { navigator.pop() }
                    )
                } else {
                    TopAppBar(
                        title = { Text("Loading...") }
                    )
                }
            },
            content = {
                LoadingCircle()
            },
            scrollBehavior = scrollBehavior,
        )
    }

}


data class ProfileSkylineTab(
    val index: UShort,
    val screenModel: TabbedProfileViewModel,
    val state: StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>?,
    val paddingValues: PaddingValues = PaddingValues(0.dp),
    val ownProfile: Boolean = false,
): NavTab {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        TabbedSkylineFragment(screenModel, state, paddingValues, refresh = {
            state?.value?.uri?.let { it1 -> screenModel.updateFeed(it1, it) }
        }, isProfileFeed = true)
    }

    override val key: ScreenKey
        get() = "${state?.value?.uri?.atUri.orEmpty()}${hashCode()}"


    @OptIn(ExperimentalResourceApi::class, ExperimentalCoilApi::class)
    override val options: TabOptions
        @Composable
        get() {
            /* Curious if this works for tab icons
            val icon = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .fallback(ImageRequest.Builder(LocalPlatformContext.current)
                        .data(imageResource(Res.drawable.placeholder_pfp).asSkiaBitmap())
                        .build().fallbackFactory)
                    .data(state.profile.avatar)
                    .crossfade(true)
                    .build(),
            )
            */

            val name = rememberSaveable {
                if (state?.value?.profile?.displayName != null && state.value.profile.displayName!!.isNotEmpty()) {
                    state.value.profile.displayName!!
                } else { state?.value?.profile?.handle?.handle.orEmpty() }
            }
            return TabOptions(
                index = index,
                title = name,
                //icon = icon,
            )
        }

}
