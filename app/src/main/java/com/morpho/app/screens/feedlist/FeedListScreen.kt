package com.morpho.app.screens.feedlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.morpho.app.MainViewModel
import com.morpho.app.components.Center
import com.morpho.app.components.ScreenBody
import com.morpho.app.extensions.activityViewModel

@Destination
@Composable
fun FeedListScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: FeedListViewModel = hiltViewModel()
) {
    BackHandler {
        navigator.navigateUp()
    }
   FeedListView(navigator, navBar = {mainViewModel.navBar?.let {it(5)}})
}

@Composable
fun FeedListView(
    navigator: DestinationsNavigator,
    navBar: @Composable() () -> Unit = {},
){
    ScreenBody(modifier = Modifier.fillMaxSize(), navBar = navBar) {
        Center {
            Text("Feed List Not Implemented Yet")
        }
    }
}

@Composable
@Preview(showBackground = true)
fun FeedListPreview(){
    //FeedListView()
}