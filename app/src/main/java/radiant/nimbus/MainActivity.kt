package radiant.nimbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.Modifier
import app.bsky.actor.ProfileViewDetailed
import com.atproto.label.Label
import radiant.nimbus.extensions.lifecycleViewModels
import radiant.nimbus.ui.theme.NimbusTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import radiant.nimbus.model.BskyLabel
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.model.Moment
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.screens.profile.ProfileScreen
import radiant.nimbus.screens.profile.ProfileState
import radiant.nimbus.screens.profile.ProfileView
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle
import sh.christian.ozone.api.Uri

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by lifecycleViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NimbusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DestinationsNavHost(navGraph = NavGraphs.root)

                }
            }
        }
    }
}