package com.morpho.app.screens.settings

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent
import com.morpho.app.data.ContentLabelService
import com.morpho.app.data.MorphoAgent
import com.morpho.app.screens.base.tabbed.SettingsTab
import com.morpho.app.screens.base.tabbed.TabbedNavBar
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.ui.common.TabbedScreenScaffold
import com.morpho.app.ui.settings.AccessibilitySettings
import com.morpho.app.ui.settings.AppearanceSettings
import com.morpho.app.ui.settings.FeedPreferences
import com.morpho.app.ui.settings.LanguageSettings
import com.morpho.app.ui.settings.ModerationSettingsFragment
import com.morpho.app.ui.settings.SettingsFragment
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
public fun CurrentSettingsScreen(
    sm: TabbedMainScreenModel,
    parentNav: Navigator = LocalNavigator.currentOrThrow,
    modifier: Modifier
) {
    val navigator = LocalNavigator.currentOrThrow
    val currentScreen = navigator.lastItem as SettingsScreen

    navigator.saveableState("currentScreen") {
        currentScreen.Content(
            sm = sm,
            parentNav = parentNav,
            modifier = modifier
        )
    }
}


abstract class SettingsScreen: Screen {
    open val title: String = "Settings"

    val navBar: @Composable (@Contextual Navigator) -> Unit = { n ->
        TabbedNavBar(SettingsTab.options.index, n)
    }

    @Composable
    abstract fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    )

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    final override fun Content() =
        Content(TabbedMainScreenModel(koinInject<MorphoAgent>(), koinInject<ContentLabelService>()),
            LocalNavigator.currentOrThrow, Modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    title: String = "Settings",
    navigator: Navigator = LocalNavigator.currentOrThrow
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        }
    )
}

@Parcelize
@Serializable
data object SettingsRootPage: SettingsScreen(), Parcelable {
    override val title: String = "Settings"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, nav ->
                SettingsFragment(
                    modifier = Modifier.padding(insets),
                    navigator = nav!!,
                    sm = sm,
                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "SettingsRootPage_$uniqueScreenKey"
}

@Parcelize
@Serializable
data object AccessibilitySettingsScreen: SettingsScreen(), Parcelable {
    override val title: String = "Accessibility"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, _ ->
                AccessibilitySettings(
                    agent = sm.agent,
                    modifier = Modifier.padding(insets),
                    distinguish = false,
                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "AccessibilitySettings_$uniqueScreenKey"

}

@Parcelize
@Serializable
data object AppearanceSettingsScreen: SettingsScreen(), Parcelable {
    override val title: String = "Appearance"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, _ ->
                AppearanceSettings(
                    agent = sm.agent,
                    modifier = Modifier.padding(insets),
                    distinguish = false,
                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "AppearanceSettings_$uniqueScreenKey"
}

@Parcelize
@Serializable
data object NotificationsSettingsScreen: SettingsScreen(), Parcelable {
    override val title: String = "Notifications"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, nav ->
                SettingsFragment(
                    sm = sm,
                    modifier = Modifier.padding(insets),
                    navigator = nav!!
                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "NotificationsSettings_$uniqueScreenKey"
}

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun SettingsScreenTransition(
    navigator: Navigator,
    sm: TabbedMainScreenModel,
    parentNav: Navigator,
    modifier: Modifier,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    ),
    content: ScreenTransitionContent = {
        CurrentSettingsScreen(sm, parentNav, modifier)
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


@Parcelize
@Serializable
data object ModerationSettingsScreen: SettingsScreen(), Parcelable {
    override val title: String = "Moderation"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, nav ->
                ModerationSettingsFragment(
                    agent = sm.agent,
                    modifier = Modifier.padding(insets),
                    navigator = nav!!

                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "ModerationSettings_$uniqueScreenKey"
}

@Parcelize
@Serializable
data object LanguageSettingsScreen: SettingsScreen(), Parcelable {
    override val title: String = "Language"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, _ ->
                LanguageSettings(
                    agent = sm.agent,
                    modifier = Modifier.padding(insets),
                    distinguish = false,
                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "LanguageSettings_$uniqueScreenKey"
}

@Parcelize
@Serializable
data object ThreadSettingsScreen: SettingsScreen(), Parcelable {
    override val title: String = "Thread"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, nav ->
                SettingsFragment(
                    sm = sm,
                    modifier = Modifier.padding(insets),
                    navigator = nav!!
                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "ThreadSettings_$uniqueScreenKey"
}

@Parcelize
@Serializable
data object FeedSettingsScreen: SettingsScreen(), Parcelable {
    override val title: String = "Feed"

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content(
        sm: TabbedMainScreenModel,
        parentNav: Navigator,
        modifier: Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow
        TabbedScreenScaffold(
            navBar = { navBar(parentNav) },
            content = { insets, _ ->
                FeedPreferences(
                    agent = sm.agent,
                    modifier = Modifier.padding(insets),
                    distinguish = false,
                )
            },
            topContent = {
                SettingsTopBar(title = title, navigator = navigator)
            },
            state = navigator,
            modifier = modifier,
        )
    }

    override val key: ScreenKey
        get() = "FeedSettings_$uniqueScreenKey"
}
