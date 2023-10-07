package radiant.nimbus.screens.skyline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.MainViewModel
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.ui.common.SkylineFragment

@Destination
@Composable
fun SkylineScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: SkylineViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.getSkyline(mainViewModel.apiProvider)
    }
    SkylineView(navigator,viewModel,
        refresh = {cursor ->
                viewModel.getSkyline(mainViewModel.apiProvider, cursor)
        }
    )
}

@Composable
fun SkylineView(
    navigator: DestinationsNavigator,
    viewModel: SkylineViewModel,
    refresh: (String?) -> Unit = {},
){

    ScreenBody(
        modifier = Modifier.fillMaxSize().heightIn(0.dp, 20000.dp)
    ) {
        SkylineFragment(
            postFlow = viewModel.skylinePosts,
            onItemClicked = {},
            refresh = refresh,
            modifier = Modifier
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SkylinePreview(){
    //SkylineView()
}