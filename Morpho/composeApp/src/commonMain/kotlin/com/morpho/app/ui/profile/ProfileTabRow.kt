package com.morpho.app.ui.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Handle

//import com.morpho.app.screens.profile.ProfileViewModel

enum class ProfileTabs {
    Posts,
    PostsReplies,
    Media,
    Feeds,
    Lists,
    Likes,
    Labeler,
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTabRow(
    modifier: Modifier = Modifier,
    selected: ProfileTabs = ProfileTabs.Posts,
    id: AtIdentifier = Handle("me"),
    onTabChanged: (AtUri)-> Unit,
    ownProfile: Boolean = false,
    isLabeler: Boolean = false,
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
        if (isLabeler) {
            Tab(
                selected = true,
                onClick = {
                    selectedTab = ProfileTabs.Labeler
                    onTabChanged(AtUri.profileModServiceUri(id))
                },

                ) {
                Text(
                    text = "Labels",
                    modifier = tabModifier
                )
            }
            Tab(
                selected = false,
                onClick = {
                    selectedTab = ProfileTabs.Lists
                    onTabChanged(AtUri.profileUserListsUri(id))
                },
            ) {
                Text(
                    text = "Lists",
                    modifier = tabModifier
                )
            }
            Tab(
                selected = false,
                onClick = {
                    selectedTab = ProfileTabs.Posts
                    onTabChanged(AtUri.profilePostsUri(id))
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
                    onTabChanged(AtUri.profileRepliesUri(id))
                },

                ) {
                Text(
                    text = "Replies",
                    modifier = tabModifier
                )
            }
            Tab(
                selected = false,
                onClick = {
                    selectedTab = ProfileTabs.Feeds
                    onTabChanged(AtUri.profileFeedsListUri(id))
                },
            ) {
                Text(
                    text = "Feeds",
                    modifier = tabModifier
                )
            }
        } else {
            Tab(
                selected = true,
                onClick = {
                    selectedTab = ProfileTabs.Posts
                    onTabChanged(AtUri.profilePostsUri(id))
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
                    onTabChanged(AtUri.profileRepliesUri(id))
                },

                ) {
                Text(
                    text = "Replies",
                    modifier = tabModifier
                )
            }
            //Spacer(modifier = Modifier.width(2.dp))
            Tab(
                selected = false,
                onClick = {
                    selectedTab = ProfileTabs.Media
                    onTabChanged(AtUri.profileMediaUri(id))
                },
            ) {
                Text(
                    text = "Media",
                    modifier = tabModifier
                )
            }
            if (ownProfile) {
                Tab(
                    selected = false,
                    onClick = {
                        selectedTab = ProfileTabs.Likes
                        onTabChanged(AtUri.profileLikesUri(id))
                    },
                ) {
                    Text(
                        text = "Feeds",
                        modifier = tabModifier
                    )
                }
            }

            Tab(
                selected = false,
                onClick = {
                    selectedTab = ProfileTabs.Feeds
                    onTabChanged(AtUri.profileFeedsListUri(id))
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
                    onTabChanged(AtUri.profileUserListsUri(id))
                },
            ) {
                Text(
                    text = "Lists",
                    modifier = tabModifier
                )
            }
        }



    }
}

@OptIn(ExperimentalMaterial3Api::class)
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