package com.morpho.app.screens.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.jetpack.ProvideNavigatorLifecycleKMPSupport
import cafe.adriel.voyager.jetpack.navigatorViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
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
import com.morpho.app.util.JavaSerializable
import com.morpho.butterfly.AtIdentifier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import cafe.adriel.voyager.navigator.tab.Tab as NavTab

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabbedProfileTopBar(
    profile: Profile?,
    ownProfile: Boolean,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()),
    tabs: List<ProfileSkylineTab>,
    onTabChanged: (Int) -> Unit = {},
    onBackClicked: () -> Unit,
    tabIndex: Int = 0,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(tabIndex) }
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, tab ->
                        ProfileTabItem(
                            tab, index.toUShort()
                        ) {
                            selectedTabIndex = index
                            onTabChanged(selectedTabIndex)
                        }
                    }
                }
            }
            false -> {
                // Loading
            }
        }

    }
}

@Composable
fun ProfileTabItem(
    tab: ProfileSkylineTab,
    currentIndex: UShort,
    onClick: () -> Unit = {},
) {
    val navigator = LocalTabNavigator.current

    val tabModifier = Modifier
        .padding(
            bottom = 12.dp,
            top = 6.dp,
            start = 6.dp,
            end = 6.dp
        )
    Tab(
        selected = currentIndex == tab.index,
        onClick = {
            onClick()
            navigator.current = tab
        },
    ) {
        Text(
            text = tab.title,
            modifier = tabModifier
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
       ExperimentalVoyagerApi::class
)
@Composable
fun TabScreen.TabbedProfileContent(
    id: AtIdentifier? = null,
    sm: TabbedProfileViewModel = navigatorViewModel { TabbedProfileViewModel(id) }
) {
    ProvideNavigatorLifecycleKMPSupport {
        val navigator = LocalNavigator.currentOrThrow


        LifecycleEffectOnce { sm.initProfile() }
        /*val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        //flingAnimationSpec = exponentialDecay()
    )*/
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        val ownProfile = remember { sm.api.atpUser?.id == id }
        val tabs = rememberSaveable(
            sm.tabFlow,
            sm.profileUiState.loadingState,
        ) {
            List(sm.tabFlow.value.size) { index ->
                ProfileSkylineTab(
                    index = index.toUShort(),
                    ownProfile = ownProfile,
                    title = sm.tabFlow.value[index].title,
                )
            }
        }
        val tabsCreated = rememberSaveable(tabs.size, sm.profileUiState.loadingState) {
            tabs.isNotEmpty() && sm.profileUiState.loadingState == UiLoadingState.Idle
        }
        if (tabsCreated) {
            TabNavigator(
                tab = tabs.first(),
                disposeNestedNavigators = false,
                tabDisposable = { TabDisposable(navigator = it, tabs = tabs) }
            ) {
                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex =
                    sm.profileUiState.tabStates[selectedTabIndex].value.feed.cursor.scroll
                )

                TabbedProfileScreenScaffold(
                    navBar = { navBar(navigator) },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topContent = {
                        TabbedProfileTopBar(
                            sm.profileState?.profile,
                            ownProfile, scrollBehavior, tabs.toImmutableList(),
                            onBackClicked = { navigator.pop() },
                            onTabChanged = { index ->
                                selectedTabIndex = index
                                sm.refreshTab(
                                    index,
                                    sm.profileUiState.tabStates[index].value.feed.cursor
                                        .copy(scroll = listState.firstVisibleItemIndex)
                                )
                                           },
                            tabIndex = selectedTabIndex,
                        )
                    },
                    content = { insets, state ->

                        CurrentProfileScreen(sm, insets, state, listState, Modifier)
                    },
                    state = sm.profileUiState.tabStates.getOrNull(selectedTabIndex),
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
                content = { _, _ ->
                    LoadingCircle()
                },
                scrollBehavior = scrollBehavior,
                state = sm.profileUiState.tabStates.getOrNull(selectedTabIndex),
            )
        }
    }
}

@Composable
public fun CurrentProfileScreen(
    sm: TabbedProfileViewModel,
    paddingValues: PaddingValues,
    state: StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>?,
    listState: LazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = state?.value?.feed?.cursor?.scroll ?: 0
    ),
    modifier: Modifier
) {
    val navigator = LocalNavigator.currentOrThrow
    val currentScreen = navigator.lastItem as ProfileTabScreen

    navigator.saveableState("currentScreen") {
        currentScreen.Content(
            sm = sm,
            paddingValues = paddingValues,
            state = state,
            listState = listState,
            modifier = modifier
        )
    }
}


abstract class ProfileTabScreen: NavTab {

    @Composable
    abstract fun Content(
        sm: TabbedProfileViewModel,
        paddingValues: PaddingValues,
        state: StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>?,
        listState: LazyListState,
        modifier: Modifier
    )

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    final override fun Content() = Content(TabbedProfileViewModel(),PaddingValues(0.dp),null, rememberLazyListState(), Modifier)
}

@Serializable
data class ProfileSkylineTab(
    val index:  UShort,
    val ownProfile: Boolean = false,
    val title: String,
): ProfileTabScreen(), JavaSerializable {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedProfileViewModel,
        paddingValues: PaddingValues,
        state: StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>?,
        listState: LazyListState,
        modifier: Modifier
    ) {
        TabbedSkylineFragment(sm, state, paddingValues, refresh = { cursor ->
            sm.refreshTab(index.toInt(), cursor)
        }, isProfileFeed = true, listState = listState)
    }

    override val key: ScreenKey
        get() = "${title}$uniqueScreenKey"


    @OptIn(ExperimentalResourceApi::class, ExperimentalCoilApi::class)
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = index,
                title = title,
                //icon = icon,
            )
        }

}
