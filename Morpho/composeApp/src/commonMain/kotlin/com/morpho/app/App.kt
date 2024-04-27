package com.morpho.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.morpho.app.screens.base.BaseScreenModel
import com.morpho.app.screens.base.tabbed.TabbedBaseScreen
import com.morpho.app.screens.login.LoginScreen
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App() {
    KoinContext {
        MaterialTheme {
            val screenModel = koinInject<BaseScreenModel>()
            val loggedIn by derivedStateOf { screenModel.isLoggedIn }


            TabNavigator(
                tab = if(loggedIn) {
                    TabbedBaseScreen
                } else {
                    LoginScreen
                },
                tabDisposable = {
                    TabDisposable(
                        navigator = it,
                        tabs = listOf(TabbedBaseScreen, LoginScreen)
                    )
                }
            ) {
                LaunchedEffect(it.current) {
                    if(screenModel.isLoggedIn) {
                        it.current = TabbedBaseScreen
                    } else {
                    }
                }
                CurrentTab()
            }
        }
    }
}