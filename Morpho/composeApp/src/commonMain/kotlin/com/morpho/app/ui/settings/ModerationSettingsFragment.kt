package com.morpho.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.WrappedLazyColumn
import org.koin.compose.getKoin

@Composable
fun ModerationSettingsFragment(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    navigator: Navigator = LocalNavigator.currentOrThrow,
) {
    WrappedLazyColumn(
        modifier = modifier
    ) {
        item {
            PersonalModSettings(
                agent = agent,
                distinguish = true,
                navigator = navigator,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        item {
            BuiltinContentFilters(
                agent = agent,
                distinguish = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        item {
            AdditionalLabelerSettings(
                agent = agent,
                distinguish = true,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

    }
}