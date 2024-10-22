package com.morpho.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.jetpack.ProvideNavigatorLifecycleKMPSupport
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.morpho.app.data.ContentLabelService
import com.morpho.app.data.DarkModeSetting
import com.morpho.app.data.MorphoAgent
import com.morpho.app.screens.base.tabbed.TabbedBaseScreen
import com.morpho.app.screens.login.LoginScreen
import com.morpho.app.screens.main.tabbed.TabbedMainScreenModel
import com.morpho.app.ui.theme.MorphoTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalResourceApi::class, ExperimentalVoyagerApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
@Preview
fun App() {
    KoinContext {
        //ProvideNavigatorLifecycleKMPSupport {
            val agent = koinInject<MorphoAgent>()
            val labelService = koinInject<ContentLabelService>()
            val screenModel = koinInject<TabbedMainScreenModel>(
                parameters = { parametersOf(agent, labelService) }
            )
            val loggedIn by screenModel.isLoggedIn
                .collectAsState(initial = screenModel.isLoggedIn.value)

            val morphoPrefs by derivedStateOf { agent.morphoPrefs }
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val darkTheme by morphoPrefs.mapLatest {
                when(it.darkMode){
                    DarkModeSetting.SYSTEM -> isSystemInDarkTheme
                    DarkModeSetting.LIGHT -> false
                    DarkModeSetting.DARK -> true
                    null -> isSystemInDarkTheme
                }
            }.collectAsState(when(morphoPrefs.value.darkMode){
                DarkModeSetting.SYSTEM -> isSystemInDarkTheme
                DarkModeSetting.LIGHT -> false
                DarkModeSetting.DARK -> true
                null -> isSystemInDarkTheme
            })
            MorphoTheme(darkTheme = darkTheme) {
                TabNavigator(
                    tab = if (loggedIn) {
                        TabbedBaseScreen
                    } else {
                        LoginScreen
                    },
                    disposeNestedNavigators = true,
                ) {
                    LaunchedEffect(loggedIn) {
                        if(loggedIn) {
                            it.current = TabbedBaseScreen
                        } else {
                            it.current = LoginScreen
                        }
                    }
                    CurrentTab()
                }
            }
        // }
    }
}