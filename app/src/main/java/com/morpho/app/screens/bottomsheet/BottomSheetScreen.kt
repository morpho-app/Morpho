package morpho.app.screens.bottomsheet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.morpho.app.MainViewModel
import morpho.app.components.ScreenBody
import morpho.app.extensions.activityViewModel

@Destination//(style = DestinationStyleBottomSheet::class)
@Composable
fun BottomSheetScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: BottomSheetViewModel = hiltViewModel()
) {
    BottomSheetView()
}

@Composable
fun BottomSheetView(
){
    ScreenBody(modifier = Modifier.fillMaxSize()) {
    }
}

@Composable
@Preview(showBackground = true)
fun BottomSheetPreview(){
    BottomSheetView()
}