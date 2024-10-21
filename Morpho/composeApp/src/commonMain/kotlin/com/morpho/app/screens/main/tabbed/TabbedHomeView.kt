package com.morpho.app.screens.main.tabbed



import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent
import coil3.annotation.ExperimentalCoilApi
import com.morpho.app.data.ContentLabelService
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ScrollPosition
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.app.ui.common.TabbedSkylineFragment
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import morpho.composeapp.generated.resources.BlueSkyKawaii
import morpho.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.max
import kotlin.math.min
import cafe.adriel.voyager.navigator.tab.Tab as NavTab

@Composable
public fun <T: Event>CurrentSkylineScreen(
    sm: TabbedMainScreenModel,
    paddingValues: PaddingValues,
    state: ContentCardState<T>?,
    modifier: Modifier
) {
    val navigator = LocalNavigator.currentOrThrow
    val currentScreen = navigator.lastItem as SkylineTab

    navigator.saveableState("currentScreen") {
        currentScreen.Content(
            sm = sm,
            paddingValues = paddingValues,
            state = state,
            modifier = modifier
        )
    }
}


abstract class SkylineTab: NavTab {

    @Composable
    abstract fun <T: Event> Content(
        sm: TabbedMainScreenModel,
        paddingValues: PaddingValues,
        state: ContentCardState<T>?,
        modifier: Modifier
    )

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    final override fun Content() = Content<Event>(
        TabbedMainScreenModel(koinInject<MorphoAgent>(), koinInject<ContentLabelService>()),
        PaddingValues(0.dp), null, Modifier)
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
       ExperimentalVoyagerApi::class
)
@Composable
fun TabScreen.TabbedHomeView(
    navigator: Navigator = LocalNavigator.currentOrThrow,
    sm: TabbedMainScreenModel = navigator.koinNavigatorScreenModel<TabbedMainScreenModel>()
) {
    //ProvideNavigatorLifecycleKMPSupport {

        var selectedTabIndex by rememberSaveable { mutableIntStateOf(sm.timelineIndex) }

        val tabs = remember(
            sm.tabs, sm.loaded, sm.tabs.size
        ) {

            List(sm.tabs.size) { index ->

                HomeSkylineTab(
                    index = index.toUShort(),
                    title = sm.tabs[index].title,
                    avatar = sm.tabs[index].avatar,
                )
            }
        }
        val tabsCreated by derivedStateOf { sm.loaded && sm.tabs.isNotEmpty() }
        if (tabsCreated) {
            Navigator(
                tabs.first(),
                key = "homeFeedsNavigator",
                disposeBehavior = NavigatorDisposeBehavior(
                    //disposeNestedNavigators = false,
                )
            ) { nav ->
                val tabUri = sm.uriForTab(selectedTabIndex)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                TabbedScreenScaffold(
                    navBar = { navBar(navigator) },
                    content = { insets, state ->

                        SkylineTabTransition(nav, sm, insets, state)
                    },
                    topContent = {
                        HomeTabRow(
                            tabs = tabs,
                            modifier = Modifier.statusBarsPadding(),
                            tabIndex = selectedTabIndex,
                            onChanged = { index ->
                                if (index == selectedTabIndex) return@HomeTabRow

                                if(index < selectedTabIndex) {
                                    if (nav.items.contains(tabs[index])) {
                                        nav.popUntil {it == tabs[index] }
                                    } else nav.replace(tabs[index])
                                } else if(index > selectedTabIndex) nav.push(tabs[index])
                                selectedTabIndex = index

                            },
                            drawerState = drawerState,
                            kawaiiMode = sm.kawaiiMode,

                        )
                    },
                    state = sm.feedStates[tabUri] as ContentCardState.Skyline?,
                    modifier = Modifier,
                    drawerState = drawerState,
                    profile = sm.userProfile,
                )
            }

        } else LoadingCircle()
   // }
}

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun <T: Event> SkylineTabTransition(
    navigator: Navigator,
    sm: TabbedMainScreenModel,
    insets: PaddingValues = PaddingValues(0.dp),
    state: ContentCardState<T>?,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    ),
    content: ScreenTransitionContent = {
        CurrentSkylineScreen(sm, insets, state, modifier)
    }
) {
    ScreenTransition(
        navigator = navigator,
        modifier = modifier,
        content = content,
        disposeScreenAfterTransitionEnd = true,
        transition = {
            val (initialOffset, targetOffset) = when (navigator.lastEvent) {
                StackEvent.Pop -> ({ size: Int -> -size }) to ({ size: Int -> size })
                StackEvent.Replace -> ({ size: Int -> -size }) to ({ size: Int -> size })
                else -> ({ size: Int -> size }) to ({ size: Int -> -size })
            }

            slideInHorizontally(animationSpec, initialOffset) togetherWith
                    slideOutHorizontally(animationSpec, targetOffset)

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabRow(
    tabs: List<HomeSkylineTab>,
    modifier: Modifier = Modifier,
    tabIndex: Int = 0,
    onChanged: (Int) -> Unit = {},
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    kawaiiMode: Boolean = false,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(tabIndex) }
    val scope = rememberCoroutineScope()

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(
                onClick = {
                    if(drawerState.isClosed) scope.launch { drawerState.open() }
                    else scope.launch { drawerState.close() }
                },
                modifier = if(kawaiiMode) Modifier.size(90.dp) else Modifier
            ) {
                if(kawaiiMode) {
                    Image(
                        painterResource(Res.drawable.BlueSkyKawaii),
                        contentDescription = "open navigation drawer (but kawaii)",
                    )
                } else {
                    Icon(Icons.Default.Menu, contentDescription = "open navigation drawer")
                }
            }
        },
        title = {
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 10.dp,
                indicator = { tabPositions ->
                    if(tabPositions.isNotEmpty()) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[max(0, min(selectedTabIndex, tabs.lastIndex))])
                        )
                    }
                },
                divider = {},
                //modifier = Modifier.offset(y = 8.dp),
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = max(0, min(index, tabs.lastIndex))
                            onChanged(max(0, min(index, tabs.lastIndex)))
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ){
                                if(tab.avatar != null) {
                                    OutlinedAvatar(
                                        url = tab.avatar,
                                        size = 20.dp,
                                        avatarShape = AvatarShape.Rounded,
                                        modifier = Modifier.padding(end = 8.dp),
                                    )
                                }
                                Text(
                                    text = tab.title,
                                )
                            } }
                    )
                }
            }
        },
    )
}


@Parcelize
@Serializable
data class HomeSkylineTab @OptIn(ExperimentalVoyagerApi::class) constructor(
    val index: UShort,
    val title: String,
    val avatar: String? = null,
): SkylineTab(), Parcelable {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun <E : Event> Content(
        sm: TabbedMainScreenModel,
        paddingValues: PaddingValues,
        state: ContentCardState<E>?,
        modifier: Modifier
    ) {
        if(state == null) return
        val data = sm.tabPagers[state.uri]?.collectAsLazyPagingItems()
        val listState = rememberLazyListState(
            state.scrollPosition.value.index,
            state.scrollPosition.value.scrollOffset
        )

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect {
                    state.scrollPosition.value = ScrollPosition(
                        index = listState.firstVisibleItemIndex,
                        scrollOffset = listState.firstVisibleItemScrollOffset
                    )
                }
        }


        if(data != null) {
            TabbedSkylineFragment(
                paddingValues = paddingValues,
                isProfileFeed = false,
                uiUpdate = state.updates,
                eventCallback = { sm.sendGlobalEvent(it) },
                getContentHandling = { post -> sm.labelService.getContentHandlingForPost(post).map { it.first } },
                pager = data,
                listState = listState,
                agent = sm.agent,
            )
        } else {
            LoadingCircle()
        }

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
                //icon = tabIcon,
            )
        }
}