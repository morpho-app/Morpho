package com.morpho.app.screens.main.tabbed



import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.jetpack.ProvideNavigatorLifecycleKMPSupport
import cafe.adriel.voyager.jetpack.navigatorViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent
import coil3.annotation.ExperimentalCoilApi
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.ui.common.LoadingCircle
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.app.ui.common.TabbedSkylineFragment
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.util.JavaSerializable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.max
import kotlin.math.min
import cafe.adriel.voyager.navigator.tab.Tab as NavTab

@Composable
public fun CurrentSkylineScreen(
    sm: TabbedMainScreenModel,
    paddingValues: PaddingValues,
    state: StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>?,
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
    abstract fun Content(
        sm: TabbedMainScreenModel,
        paddingValues: PaddingValues,
        state: StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>?,
        modifier: Modifier
    )

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    final override fun Content() = Content(TabbedMainScreenModel(),PaddingValues(0.dp),null,Modifier)
}


@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
       ExperimentalVoyagerApi::class
)
@Composable
fun TabScreen.TabbedHomeView(
    sm: TabbedMainScreenModel = navigatorViewModel { TabbedMainScreenModel() }
) {
    ProvideNavigatorLifecycleKMPSupport {
        val navigator = LocalNavigator.currentOrThrow


        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        var insets = WindowInsets.navigationBars.asPaddingValues()

        LifecycleEffectOnce {
            sm.initTabs()
        }
        val tabs = remember(
            sm.tabFlow.value, sm.uiState.loadingState, sm.uiState.tabs.value.size
        ) {
            List(sm.uiState.tabs.value.size) { index ->
                HomeSkylineTab(
                    index = index.toUShort(),
                    title = sm.uiState.tabs.value[index].title,
                    avatar = sm.uiState.tabs.value[index].avatar,
                )
            }
        }
        val tabsCreated = remember(tabs.size, sm.uiState.loadingState) {
            tabs.isNotEmpty() && sm.uiState.loadingState == UiLoadingState.Idle
        }
        if (tabsCreated) {
            Navigator(
                tabs.first(),
                disposeBehavior = NavigatorDisposeBehavior(
                    disposeNestedNavigators = false,
                )
            ) { nav ->
                TabbedScreenScaffold(
                    navBar = { navBar(navigator) },
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
                            }

                        )
                    },
                    content = { insets, state ->
                        SkylineTabTransition(nav, sm, insets, state)
                    },
                    modifier = Modifier,
                    state = sm.uiState.tabStates.getOrNull(selectedTabIndex) as StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>?
                )
            }

        } else LoadingCircle()
    }
}

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun SkylineTabTransition(
    navigator: Navigator,
    sm: TabbedMainScreenModel,
    insets: PaddingValues = PaddingValues(0.dp),
    state: StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>?,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    ),
    content: ScreenTransitionContent = {
        CurrentSkylineScreen(sm, insets, state, Modifier)
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
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(tabIndex) }

    SecondaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),//.zIndex(1f),
        edgePadding = 10.dp,
        indicator = { tabPositions ->
            if(tabPositions.isNotEmpty()) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[max(0, min(selectedTabIndex, tabs.lastIndex))])
                )
            }
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = max(0, min(index, tabs.lastIndex))
                    onChanged(max(0, min(index, tabs.lastIndex)))
                },
                //icon = { tab.icon() },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
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
                            //style = MaterialTheme.typography.titleSmall,
                        )
                    } }
            )
        }
    }
}



@Serializable
data class HomeSkylineTab @OptIn(ExperimentalVoyagerApi::class) constructor(
    val index: UShort,
    val title: String,
    val avatar: String? = null,
): SkylineTab(), JavaSerializable {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        paddingValues: PaddingValues,
        state: StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>?,
        modifier: Modifier
    ) {

        TabbedSkylineFragment(
            sm, state, paddingValues,
            refresh = { cursor ->
                sm.refreshTab(index.toInt(), cursor)
            },
        )
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