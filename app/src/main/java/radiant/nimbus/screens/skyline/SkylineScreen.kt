package radiant.nimbus.screens.skyline

import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetFeedGeneratorQueryParams
import app.bsky.feed.GetFeedQueryParams
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.Skyline
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.ui.common.SkylineFragment
import radiant.nimbus.ui.common.SkylineTopBar
import radiant.nimbus.ui.elements.OutlinedAvatar
import kotlin.math.max

@RootNavGraph(start = true)
@Destination
@Composable
fun SkylineScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: SkylineViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        val uri = viewModel.state.feedUri
        if (uri != null) {

        } else{
            viewModel.getSkyline(mainViewModel.apiProvider)
        }

        mainViewModel.userPreferences?.savedFeeds?.pinned?.map { feedUri ->
            when(val response = mainViewModel.apiProvider.api.getFeedGenerator(GetFeedGeneratorQueryParams(feedUri))) {
                is AtpResponse.Failure -> {
                    Log.e("Skyline", "Error getting feed info: $response")}
                is AtpResponse.Success -> {
                    viewModel.pinnedFeeds += FeedTab(response.response.view.displayName, response.response.view.uri)
                }
            }
        }
    }
    SkylineView(
        navigator = navigator,
        viewModel = viewModel,
        refresh = {cursor ->
                viewModel.getSkyline(mainViewModel.apiProvider, cursor)
        },
        feedRefresh = { uri, cursor ->
            viewModel.getSkyline(mainViewModel.apiProvider, GetFeedQueryParams(uri), cursor)
        },
        apiProvider = mainViewModel.apiProvider,
        navBar = { mainViewModel.navBar?.let { it(0) } },
        mainButton = { onClicked ->
            OutlinedAvatar(url = mainViewModel.currentUser?.avatar.orEmpty(),
                modifier = Modifier.size(55.dp),
                onClicked = onClicked,
                outlineSize = 0.dp
            )
        }
    )
}

@Serializable
data class FeedTab(
    val title: String,
    val uri: AtUri,
    var cursor: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkylineView(
    navigator: DestinationsNavigator,
    viewModel: SkylineViewModel,
    apiProvider: ApiProvider,
    refresh: (String?) -> Unit = {},
    feedRefresh: (AtUri, String?) -> Unit = {_,_ ->},
    navBar: @Composable () -> Unit = {},
    mainButton: @Composable ((() -> Unit) -> Unit)? = null,
){
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        if(selectedTab > 0) {
            viewModel.getSkyline(apiProvider, GetFeedQueryParams(
                viewModel.pinnedFeeds[selectedTab-1].uri,
                cursor = viewModel.pinnedFeeds[selectedTab-1].cursor
            ))
        }
    }
    ScreenBody(
        modifier = Modifier,
        topContent = {
            SkylineTopBar(viewModel.pinnedFeeds, tabIndex =  selectedTab,
                onChanged = {
                    selectedTab = it
                    if (selectedTab > 0) {
                        viewModel.getSkyline(
                            apiProvider, GetFeedQueryParams(
                                viewModel.pinnedFeeds[it - 1].uri,
                                cursor = viewModel.pinnedFeeds[it - 1].cursor
                            )
                        )
                    } else {
                        viewModel.getSkyline(apiProvider, viewModel.skylinePosts.value.cursor)
                    }
                },
                mainButton = mainButton,
                onButtonClicked = {

                }
            )
        },
        navBar = navBar,
        contentWindowInsets = WindowInsets.navigationBars,
    ) {insets ->

        when(selectedTab) {
            0 -> {
                SkylineFragment(
                    navigator = navigator,
                    postFlow = viewModel.skylinePosts,
                    onItemClicked = {
                        navigator.navigate(PostThreadScreenDestination(it))
                    },
                    onProfileClicked = {
                        navigator.navigate(ProfileScreenDestination(it))
                    },
                    refresh = refresh,
                    contentPadding = insets,
                    onUnClicked = {type, rkey ->  apiProvider.deleteRecord(type, rkey)},
                    onRepostClicked = {
                        apiProvider.createRecord(RecordUnion.Repost(it))
                        /* TODO: Add dialog/quote post option */
                    },
                    onReplyClicked = { },
                    onMenuClicked = { },
                    onLikeClicked = {
                        apiProvider.createRecord(RecordUnion.Like(it))
                    },
                )
            }
            else -> {
                viewModel.feedPosts[viewModel.pinnedFeeds[selectedTab-1].uri]?.let { it: MutableStateFlow<Skyline> ->
                    SkylineFragment(
                        navigator = navigator,
                        postFlow = it,
                        onItemClicked = {
                            navigator.navigate(PostThreadScreenDestination(it))
                        },
                        onProfileClicked = {
                            navigator.navigate(ProfileScreenDestination(it))
                        },
                        refresh = { cursor->
                            feedRefresh(viewModel.pinnedFeeds[max(selectedTab-1, 0)].uri, cursor)
                        },
                        contentPadding = insets,
                        onUnClicked = {type, rkey ->  apiProvider.deleteRecord(type, rkey)},
                        onRepostClicked = {
                            apiProvider.createRecord(RecordUnion.Repost(it))
                            /* TODO: Add dialog/quote post option */
                        },
                        onReplyClicked = { },
                        onMenuClicked = { },
                        onLikeClicked = {
                            apiProvider.createRecord(RecordUnion.Like(it))
                        },
                    )
                }
            }
        }
    }


}



