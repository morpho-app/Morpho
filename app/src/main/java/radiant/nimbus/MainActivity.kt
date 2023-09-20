package radiant.nimbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import radiant.nimbus.extensions.lifecycleViewModels
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.destinations.LoginScreenDestination
import radiant.nimbus.ui.theme.NimbusTheme
import sh.christian.ozone.api.AtIdentifier

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by lifecycleViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.supervisors.plus(viewModel.apiProvider)
        viewModel.supervisors.forEach { supervisor ->
            with(supervisor) {
                lifecycleScope.launch(SupervisorJob()) {
                    onStart()
                }
            }
        }

        val authInfo = runBlocking { viewModel.apiProvider.loginRepository.auth().first() }
        viewModel.apiProvider.loginRepository.auth = authInfo

        setContent {
            NimbusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    if (authInfo == null) {
                        DestinationsNavHost(navGraph = NavGraphs.root, startRoute = LoginScreenDestination)

                    } else {
                        viewModel.currentUser = AtIdentifier(authInfo.did.did)
                        DestinationsNavHost(navGraph = NavGraphs.root)//, startRoute = ProfileScreenDestination)
                    }

                }
            }
        }
    }
}