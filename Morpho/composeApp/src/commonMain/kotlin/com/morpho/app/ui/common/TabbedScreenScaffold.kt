package com.morpho.app.ui.common

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uistate.ContentCardState

@Composable
expect fun <T> TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, T?) -> Unit,
    topContent: @Composable () -> Unit,
    state: T?,
    modifier: Modifier,
)

@ExperimentalMaterial3Api
@Composable
expect fun <T: Event> TabbedProfileScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues,ContentCardState<T>?) -> Unit,
    topContent: @Composable (TopAppBarScrollBehavior) -> Unit,
    state: ContentCardState<out T>?,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    nestedScrollConnection: NestedScrollConnection = scrollBehavior.nestedScrollConnection,
)

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun SlideTabTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    ),
    content: ScreenTransitionContent = {
        CurrentScreen()
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