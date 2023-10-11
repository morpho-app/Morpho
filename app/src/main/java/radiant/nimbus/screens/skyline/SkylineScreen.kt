package radiant.nimbus.screens.skyline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetFeedQueryParams
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.ui.common.SkylineFragment

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
            viewModel.getSkyline(
                mainViewModel.apiProvider,
                GetFeedQueryParams(uri)
                )
        } else{
            viewModel.getSkyline(mainViewModel.apiProvider)
        }
    }
    SkylineView(
        navigator = navigator,
        viewModel = viewModel,
        refresh = {cursor ->
                viewModel.getSkyline(mainViewModel.apiProvider, cursor)
        },
        apiProvider = mainViewModel.apiProvider,
        navBar = { mainViewModel.navBar?.let { it(0) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkylineView(
    navigator: DestinationsNavigator,
    viewModel: SkylineViewModel,
    apiProvider: ApiProvider,
    refresh: (String?) -> Unit = {},
    navBar: @Composable () -> Unit = {},
){

    ScreenBody(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .heightIn(0.dp, 20000.dp),
        topContent = {
            var selectedTab by rememberSaveable { mutableIntStateOf(0) }
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {

                    Surface(
                        tonalElevation = 3.dp,
                        modifier = Modifier.offset(y = (-4).dp),
                        shape = MaterialTheme.shapes.small.copy(
                            bottomStart = CornerSize(0.dp),
                            topStart = CornerSize(0.dp),
                            topEnd = CornerSize(0.dp),
                        )
                    ){
                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier.padding(bottom = 5.dp, top = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier
                                    .size(20.dp)
                            )
                        }
                    }

                    SecondaryScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.padding(0.dp).offset(y = (-8).dp),
                        edgePadding = 10.dp,
                        //divider = {}
                    ) {

                        Tab(selected = selectedTab == 0,
                            onClick = { selectedTab = 0},
                            text = {
                                Text("Home")
                            }
                        )
                        Tab(selected = selectedTab == 1,
                            onClick = { selectedTab = 1},
                            text = {
                                Text("Feed 1")
                            }
                        )
                        Tab(selected = selectedTab == 2,
                            onClick = { selectedTab = 2},
                            text = {
                                Text("Feed 2")
                            }
                        )
                        Tab(selected = selectedTab == 3,
                            onClick = { selectedTab = 3},
                            text = {
                                Text("Feed 3")
                            }
                        )
                    }
                },
            )
        },
        navBar = navBar,
        contentWindowInsets = WindowInsets.navigationBars,
    ) {insets ->

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


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun TopAppBarPreview(){
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    TopAppBar(
        title = {},
        actions = {
            Column{
                IconButton(
                    onClick = { /* doSomething() */ },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 5.dp, top = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(30.dp)
                    )

                }
                HorizontalDivider(
                    Modifier
                        .offset(y = (-9.25).dp)
                        .width(60.dp),

                )
            }
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(0.dp)
                //divider = {}
            ) {
                Tab(selected = selectedTab == 0,
                    onClick = { selectedTab = 0},
                    text = {
                        Text("Home")
                    }
                )
                Tab(selected = selectedTab == 1,
                    onClick = { selectedTab = 1},
                    text = {
                        Text("Feed 1")
                    }
                )
                Tab(selected = selectedTab == 2,
                    onClick = { selectedTab = 2},
                    text = {
                        Text("Feed 2")
                    }
                )
                Tab(selected = selectedTab == 3,
                    onClick = { selectedTab = 3},
                    text = {
                        Text("Feed 3")
                    }
                )
            }
        },
    )
}