package radiant.nimbus.ui.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.screens.profile.ProfileTabs
import radiant.nimbus.screens.profile.ProfileViewModel

@Composable
fun ProfileTabRow(
    selected: ProfileTabs = ProfileTabs.Posts,
    apiProvider: ApiProvider,
    model: ProfileViewModel,
    onTabChanged: (ProfileTabs)-> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(selected) }
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        edgePadding = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val tabModifier = Modifier
            .padding(
                bottom = 12.dp,
                top = 6.dp,
                start = 6.dp,
                end = 6.dp
            )
        Tab(
            selected = true,
            onClick = {
                selectedTab = ProfileTabs.Posts
                onTabChanged(ProfileTabs.Posts)
                model.getProfileFeed(ProfileTabs.Posts, apiProvider)
            },

            ) {
            Text(
                text = "Posts",
                modifier = tabModifier
            )
        }

        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.PostsReplies
                onTabChanged(ProfileTabs.PostsReplies)
                model.getProfileFeed(ProfileTabs.PostsReplies, apiProvider)
            },

            ) {
            Text(
                text = "Posts & Replies",
                modifier = tabModifier
            )
        }
        //Spacer(modifier = Modifier.width(2.dp))
        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.Media
                onTabChanged(ProfileTabs.Media)
                model.getProfileFeed(ProfileTabs.Media, apiProvider)
            },
        ) {
            Text(
                text = "Media",
                modifier = tabModifier
            )
        }

        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.Feeds
                onTabChanged(ProfileTabs.Feeds)
                model.getProfileFeed(ProfileTabs.Feeds, apiProvider)
            },
        ) {
            Text(
                text = "Feeds",
                modifier = tabModifier
            )
        }

        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.Lists
                onTabChanged(ProfileTabs.Lists)
                model.getProfileFeed(ProfileTabs.Lists, apiProvider)
            },
        ) {
            Text(
                text = "Lists",
                modifier = tabModifier
            )
        }
    }
}

@Composable
fun PreviewProfileTabRow(
    selected: ProfileTabs = ProfileTabs.Posts,
    onTabChanged: (ProfileTabs)-> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(selected) }
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        edgePadding = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val tabModifier = Modifier
            .padding(
                bottom = 12.dp,
                top = 6.dp,
                start = 6.dp,
                end = 6.dp
            )
        Tab(
            selected = true,
            onClick = {
                selectedTab = ProfileTabs.Posts
                onTabChanged(ProfileTabs.Posts)
                //model.getProfileFeed(ProfileTabs.Posts, apiProvider)
            },

            ) {
            Text(
                text = "Posts",
                modifier = tabModifier
            )
        }

        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.PostsReplies
                onTabChanged(ProfileTabs.PostsReplies)
                //model.getProfileFeed(ProfileTabs.PostsReplies, apiProvider)
            },

            ) {
            Text(
                text = "Posts & Replies",
                modifier = tabModifier
            )
        }
        //Spacer(modifier = Modifier.width(2.dp))
        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.Media
                onTabChanged(ProfileTabs.Media)
                //model.getProfileFeed(ProfileTabs.Media, apiProvider)
            },
        ) {
            Text(
                text = "Media",
                modifier = tabModifier
            )
        }

        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.Feeds
                onTabChanged(ProfileTabs.Feeds)
                //model.getProfileFeed(ProfileTabs.Feeds, apiProvider)
            },
        ) {
            Text(
                text = "Feeds",
                modifier = tabModifier
            )
        }

        Tab(
            selected = false,
            onClick = {
                selectedTab = ProfileTabs.Lists
                onTabChanged(ProfileTabs.Lists)
                //model.getProfileFeed(ProfileTabs.Lists, apiProvider)
            },
        ) {
            Text(
                text = "Lists",
                modifier = tabModifier
            )
        }
    }
}