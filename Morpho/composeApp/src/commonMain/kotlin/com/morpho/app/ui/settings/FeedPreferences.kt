package com.morpho.app.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.bsky.actor.FeedViewPref
import com.morpho.app.data.MorphoAgent
import com.morpho.app.ui.elements.SettingsGroup
import com.morpho.app.ui.elements.SettingsItem
import org.koin.compose.getKoin

@Composable
fun FeedPreferences(
    agent: MorphoAgent = getKoin().get(),
    modifier: Modifier = Modifier,
    distinguish: Boolean = true,
) {
    val feedPrefs = agent.prefs.feedView ?: FeedViewPref(
        feed = "following",
        hideReplies = false,
        hideRepliesByUnfollowed = true,
        hideRepliesByLikeCount = 0,
        hideReposts = false,
        hideQuotePosts = false,
        lab_mergeFeedEnabled = true,
    )
    SettingsGroup(
        title = "Following Feed Preferences",
        modifier = modifier,
        distinguish = distinguish,
    ) {
        SettingsItem(
            text = AnnotatedString("Show replies"),
            description = AnnotatedString("Show any replies in the following feed at all?"),
        ) {
            var showReplies by mutableStateOf(feedPrefs.hideReplies != true)
            Row {
                Switch(
                    checked = showReplies,
                    onCheckedChange = {
                        showReplies = it
                        agent.setFeedViewPrefs(
                            feed = "following",
                            feedViewPref = feedPrefs.copy(hideReplies = !showReplies)
                        )
                    }
                )
                Text(
                    text = if(showReplies) "Show" else "Hide",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        if(feedPrefs.hideReplies != true) {
            SettingsItem(
                text = AnnotatedString("Show replies by unfollowed"),
                description = AnnotatedString("Show replies by people you don't follow, but who are replying to people you do follow?"),
            ) {
                var showRepliesByUnfollowed by mutableStateOf(feedPrefs.hideRepliesByUnfollowed != true)
                Row {
                    Switch(
                        checked = showRepliesByUnfollowed,
                        onCheckedChange = {
                            showRepliesByUnfollowed = it
                            agent.setFeedViewPrefs(
                                feed = "following",
                                feedViewPref = feedPrefs.copy(hideRepliesByUnfollowed = !showRepliesByUnfollowed)
                            )
                        }
                    )
                    Text(
                        text = if(showRepliesByUnfollowed) "Show" else "Hide",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        SettingsItem(
            text = AnnotatedString("Show reposts"),
            description = AnnotatedString("Show reposts in the following feed?"),
        ) {
            var showReposts by mutableStateOf(feedPrefs.hideReposts != true)
            Row {
                Switch(
                    checked = showReposts,
                    onCheckedChange = {
                        showReposts = it
                        agent.setFeedViewPrefs(
                            feed = "following",
                            feedViewPref = feedPrefs.copy(hideReposts = !showReposts)
                        )
                    }
                )
                Text(
                    text = if(showReposts) "Show" else "Hide",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        SettingsItem(
            text = AnnotatedString("Show quote posts"),
            description = AnnotatedString(
                "Show quote posts in the following feed? (reposts will still be visible, if set to show)"
            ),
        ) {
            var showQuotePosts by mutableStateOf(feedPrefs.hideQuotePosts != true)
            Row {
                Switch(
                    checked = showQuotePosts,
                    onCheckedChange = {
                        showQuotePosts = it
                        agent.setFeedViewPrefs(
                            feed = "following",
                            feedViewPref = feedPrefs.copy(hideQuotePosts = !showQuotePosts)
                        )
                    }
                )
                Text(
                    text = if(showQuotePosts) "Show" else "Hide",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        SettingsItem(
            text = AnnotatedString("Merge feeds into Following"),
            description = AnnotatedString("Occasionally show posts from your saved feeds in your following feed?"),
        ) {
            var mergeFeeds by mutableStateOf(feedPrefs.lab_mergeFeedEnabled != null)
            Row {
                Switch(
                    checked = mergeFeeds,
                    onCheckedChange = {
                        mergeFeeds = it
                        agent.setFeedViewPrefs(
                            feed = "following",
                            feedViewPref = feedPrefs.copy(lab_mergeFeedEnabled = mergeFeeds)
                        )
                    }
                )
                Text(
                    text = if(mergeFeeds) "Yes" else "No",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}