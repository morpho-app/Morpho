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
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
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
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.max
import kotlin.math.min
import cafe.adriel.voyager.navigator.tab.Tab as NavTab

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabScreen.TabbedHomeView() {

    val navigator = LocalNavigator.currentOrThrow
    val sm = navigator.getNavigatorScreenModel<TabbedMainScreenModel>()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var insets = WindowInsets.navigationBars.asPaddingValues()

    LifecycleEffect(
        onStarted = {
            sm.initTabs()
        },
        onDisposed = {},
    )
    val tabs = rememberSaveable(
        sm.tabFlow.value, sm.uiState.loadingState, sm.uiState.tabs.value.size
    ) {
        List(sm.uiState.tabs.value.size) { index ->
            HomeSkylineTab(
                index = index.toUShort(),
                screenModel = sm,
                state = sm.uiState.tabStates[index]
                        as StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>,
                paddingValues = insets,
                icon = {
                    if(sm.uiState.tabs.value[index].avatar != null) {
                        OutlinedAvatar(
                            url = sm.uiState.tabs.value[index].avatar!!,
                            size = 20.dp,
                            avatarShape = AvatarShape.Rounded,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
            )
        }
    }
    val tabsCreated = remember(tabs.size, sm.uiState.loadingState) {
        tabs.isNotEmpty() && sm.uiState.loadingState == UiLoadingState.Idle
    }
    if (tabsCreated) {
        Navigator(tabs.first()) { nav ->
            TabbedScreenScaffold(
                navBar = { navBar(navigator) },
                topContent = {
                    HomeTabRow(
                        tabs = tabs,
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
                content = {
                    insets = it

                    SlideTabTransition(nav)
                }
            )
        }
    } else LoadingCircle()

}

@Composable
fun SlideTabTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    ),
    content:  ScreenTransitionContent = { it.Content() }
) {

    ScreenTransition(
        navigator = navigator,
        modifier = modifier,
        content = content,
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
                        tab.icon()
                        Text(
                            text = tab.state.value.feed.title,
                            //style = MaterialTheme.typography.titleSmall,
                        )
                    } }
            )
        }
    }
}



data class HomeSkylineTab(
    val index: UShort,
    val screenModel: TabbedMainScreenModel,
    val state: StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>,
    val paddingValues: PaddingValues = PaddingValues(0.dp),
    val icon: @Composable () -> Unit = {},
): NavTab {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        TabbedSkylineFragment(
            screenModel, state, paddingValues,
            refresh = { cursor ->
                screenModel.refreshTab(index.toInt(), cursor)
            },
        )
    }

    override val key: ScreenKey = "${state.value.uri.atUri}${hashCode()}"

    @OptIn(ExperimentalResourceApi::class, ExperimentalCoilApi::class)
    override val options: TabOptions
        @Composable
        get() {
            /*val (avatar, icon) = screenModel
                .getFeedInfo(screenModel.uriForTab(index = index.toInt()))
                ?.let { feedInfo ->
                    feedInfo.avatar to feedInfo.icon
                } ?: (null to null)
            val tabIcon = if(avatar != null) rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(avatar)
                    .crossfade(true)
                    .build(),
            ) else if(icon != null) rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(icon)
                    .crossfade(true)
                    .build(),
                )
            else null*/

            return TabOptions(
                index = index,
                title = state.value.feed.title,
                //icon = tabIcon,
            )
        }
}