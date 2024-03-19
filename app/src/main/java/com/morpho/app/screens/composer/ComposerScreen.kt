package com.morpho.app.screens.composer

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.morpho.app.MainViewModel
import com.morpho.app.components.ScreenBody
import com.morpho.app.extensions.activityViewModel


//@Destination(style = DestinationStyleBottomSheet::class)
@Composable
fun ColumnScope.ComposerScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: ComposerViewModel = hiltViewModel()
) {
    ComposerView()
}

@Composable
fun ComposerView(
){
    ScreenBody(modifier = Modifier.fillMaxSize()) {
    }
}

@Composable
@Preview(showBackground = true)
fun ComposerPreview(){
    ComposerView()
}