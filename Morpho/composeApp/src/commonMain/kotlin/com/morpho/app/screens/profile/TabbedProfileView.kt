package com.morpho.app.screens.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.annotation.ExperimentalCoilApi
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uidata.FeedEvent
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedProfileScreenScaffold
import com.morpho.app.ui.common.TabbedSkylineFragment
import com.morpho.app.ui.profile.DetailedProfileFragment
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import cafe.adriel.voyager.navigator.tab.Tab as NavTab

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyTabbedProfileTopBar(
    profile: ContentCardState.MyProfile,
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
        DetailedProfileFragment(
            profile = profile.profile,
            myProfile = true,
            isTopLevel = true,
            scrollBehavior = scrollBehavior,
            onBackClicked = onBackClicked,
        )

        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tab ->
                ProfileTabItem(
                    tab, index
                ) {
                    selectedTabIndex = index
                    onTabChanged(selectedTabIndex)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabbedProfileTopBar(
    profile: ContentCardState.FullProfile,
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
        DetailedProfileFragment(
            profile = profile.profile,
            myProfile = true,
            isTopLevel = true,
            scrollBehavior = scrollBehavior,
            onBackClicked = onBackClicked,
        )

        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tab ->
                ProfileTabItem(
                    tab, index
                ) {
                    selectedTabIndex = index
                    onTabChanged(selectedTabIndex)
                }
            }
        }
    }
}

@Composable
fun ProfileTabItem(
    tab: ProfileSkylineTab,
    currentIndex: Int,
    onClick: () -> Unit = {},
) {
    val navigator = LocalTabNavigator.current
    val tabModifier = Modifier
        .padding(bottom = 12.dp, top = 6.dp, start = 6.dp, end = 6.dp)
    Tab(
        selected = currentIndex == tab.index,
        onClick = {
            onClick()
            navigator.current = tab
        },
    ) {
        Text(text = tab.title, modifier = tabModifier)
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
       ExperimentalVoyagerApi::class
)
@Composable
fun TabScreen.TabbedProfileContent(
    ownProfile:  Boolean = false,
    myProfileState: ContentCardState.MyProfile,
    profileState: ContentCardState.FullProfile?,
    eventCallback: (Event) -> Unit = {},
) {
    //ProvideNavigatorLifecycleKMPSupport {
        val navigator = LocalNavigator.currentOrThrow
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        val tabs = remember(myProfileState, profileState) {
            if(ownProfile) myProfileState.toTabList() else profileState?.toTabList() ?: listOf()
        }
    TabNavigator(
        tab = tabs.first(),
        disposeNestedNavigators = true,
        tabDisposable = { TabDisposable(navigator = it, tabs = tabs) }
    ) {
        TabbedProfileScreenScaffold(
            navBar = { navBar(navigator) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topContent = {
                if(ownProfile) MyTabbedProfileTopBar(
                    profile = myProfileState,
                    scrollBehavior = scrollBehavior,
                    tabs = tabs,
                    onBackClicked = { navigator.pop() },
                    onTabChanged = { index ->
                        selectedTabIndex = index
                        val state = myProfileState.indexToState(index)
                        val actor = myProfileState.profile.did
                        when(state) {
                            is ContentCardState.ProfileTimeline -> state.events
                                .tryEmit(FeedEvent.LoadFeed(actor, state.filter))
                            is ContentCardState.ProfileList -> state.events
                                .tryEmit(FeedEvent.LoadLists(actor, state.listsOrFeeds))
                            is ContentCardState.ProfileLabeler -> {}
                            else -> {}
                        }
                    },
                    tabIndex = selectedTabIndex,
                ) else if(profileState != null) TabbedProfileTopBar(
                    profile = profileState,
                    scrollBehavior = scrollBehavior,
                    tabs = tabs,
                    onBackClicked = { navigator.pop() },
                    onTabChanged = { index ->
                        selectedTabIndex = index
                        val state = profileState.indexToState(index)
                        val actor = profileState.profile.did
                        when(state) {
                            is ContentCardState.ProfileTimeline -> state.events
                                .tryEmit(FeedEvent.LoadFeed(actor, state.filter))
                            is ContentCardState.ProfileList -> state.events
                                .tryEmit(FeedEvent.LoadLists(actor, state.listsOrFeeds))
                            is ContentCardState.ProfileLabeler -> {}
                            else -> {}
                        }
                    },
                    tabIndex = selectedTabIndex,
                ) else LoadingCircle()
            },
            content = { insets, state -> CurrentProfileScreen(eventCallback, insets, state, Modifier) },
            state = if(ownProfile) myProfileState.indexToState(selectedTabIndex)
                else profileState?.indexToState(selectedTabIndex),
            scrollBehavior = scrollBehavior,
        )
    }
    //}
}


@Composable
public fun <E : Event> CurrentProfileScreen(
    eventCallback: (Event) -> Unit,
    paddingValues: PaddingValues,
    state: ContentCardState<E>?,
    modifier: Modifier
) {
    val navigator = LocalNavigator.currentOrThrow
    val currentScreen = navigator.lastItem as ProfileTabScreen

    navigator.saveableState("currentScreen") {
        currentScreen.Content(
            eventCallback = eventCallback,
            paddingValues = paddingValues,
            state = state,
            modifier = modifier
        )
    }
}


abstract class ProfileTabScreen: NavTab {

    @Composable
    abstract fun <E : Event> Content(
        eventCallback: (Event) -> Unit,
        paddingValues: PaddingValues,
        state: ContentCardState<E>?,
        modifier: Modifier
    )

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    final override fun Content() = Content<Event>(
        eventCallback = {}, PaddingValues(0.dp),null, Modifier
    )

}

@Parcelize
@Serializable
data class ProfileSkylineTab(
    val index: Int,
    val ownProfile: Boolean = false,
    val title: String,
): ProfileTabScreen(), Parcelable {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun <E : Event> Content(
        eventCallback: (Event) -> Unit,
        paddingValues: PaddingValues,
        state: ContentCardState<E>?,
        modifier: Modifier
    ) {
        if(state == null) return
        TabbedSkylineFragment(
            paddingValues,
            isProfileFeed = true,
            uiUpdate = state.updates,
            eventCallback = eventCallback,
        )
    }

    override val key: ScreenKey
        get() = "${title}$uniqueScreenKey"


    @OptIn(ExperimentalResourceApi::class, ExperimentalCoilApi::class)
    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = index.toUShort(),
                title = title,
                //icon = icon,
            )
        }

}

fun countProfileTabs(profileState: ContentCardState.FullProfile): Int {
    var count = 3
    if(profileState.lists != null) count++
    if(profileState.feeds != null) count++
    if(profileState.labeler != null) count++
    return count
}
fun countMyProfileTabs(profileState: ContentCardState.MyProfile): Int {
    var count = 4
    if(profileState.lists != null) count++
    if(profileState.feeds != null) count++
    if(profileState.labeler != null) count++
    return count
}


fun ContentCardState.FullProfile.toTabList(): List<ProfileSkylineTab> {
    val tabs = mutableListOf<ProfileSkylineTab>()
    if(labeler != null) tabs.add(ProfileSkylineTab(0, false, "Labels"))
    var index = if(labeler != null) 1 else 0
    tabs.add(ProfileSkylineTab(index++, false, "Posts"))
    tabs.add(ProfileSkylineTab(index++, false, "Replies"))
    tabs.add(ProfileSkylineTab(index++, false, "Media"))
    if(lists != null) tabs.add(ProfileSkylineTab(index++, false, "Lists"))
    if(feeds != null) tabs.add(ProfileSkylineTab(index, false, "Feeds"))
    return tabs.toList()
}

fun ContentCardState.MyProfile.toTabList(): List<ProfileSkylineTab> {
    val tabs = mutableListOf<ProfileSkylineTab>()
    if(labeler != null) tabs.add(ProfileSkylineTab(0, true, "Labels"))
    var index = if(labeler != null) 1 else 0
    tabs.add(ProfileSkylineTab(index++, true, "Posts"))
    tabs.add(ProfileSkylineTab(index++, true, "Replies"))
    tabs.add(ProfileSkylineTab(index++, true, "Media"))
    if(lists != null) tabs.add(ProfileSkylineTab(index++, true, "Lists"))
    if(feeds != null) tabs.add(ProfileSkylineTab(index++, true, "Feeds"))
    if(labeler != null) tabs.add(ProfileSkylineTab(index, true, "Labels"))
    return tabs.toList()
}

fun ContentCardState.MyProfile.indexToState(index: Int): ContentCardState<out Event>? {
    return when(index) {
        0 -> labeler ?: posts
        1 -> if(labeler == null) postReplies else posts
        2 -> if(labeler == null) media else postReplies
        3 -> if(labeler == null) likes else media
        4 -> if(labeler == null) lists ?: feeds else likes
        5 -> if(labeler == null) feeds else lists ?: feeds
        6 -> if(labeler == null) null else feeds
        else -> throw IllegalArgumentException("Invalid index: $index")
    }
}

fun ContentCardState.FullProfile.indexToState(index: Int): ContentCardState<out Event>? {
    return when(index) {
        0 -> labeler ?: posts
        1 -> if(labeler == null) postReplies else posts
        2 -> if(labeler == null) media else postReplies
        3 -> if(labeler == null) lists ?: feeds else media
        4 -> if(labeler == null) feeds else lists ?: feeds
        5 -> if(labeler == null) null else feeds
        else -> throw IllegalArgumentException("Invalid index: $index")
    }
}


