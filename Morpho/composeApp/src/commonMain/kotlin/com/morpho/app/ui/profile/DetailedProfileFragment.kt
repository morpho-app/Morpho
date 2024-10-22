package com.morpho.app.ui.profile


import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morpho.app.model.bluesky.BskyLabelService
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.uidata.Event
import org.jetbrains.compose.resources.ExperimentalResourceApi


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalResourceApi::class
)
@Composable
expect fun DetailedProfileFragment(
    profile: DetailedProfile,
    modifier: Modifier = Modifier,
    myProfile: Boolean = false,
    isTopLevel:Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()),
    onBackClicked: () -> Unit = {},
    eventCallback: (Event) -> Unit = {},
)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalResourceApi::class
)
@Composable
expect fun LabelerProfileFragment(
    labeler: BskyLabelService,
    modifier: Modifier = Modifier,
    isSubscribed: Boolean,
    isTopLevel:Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()),
    onBackClicked: () -> Unit,
    eventCallback: (Event) -> Unit,
)

@Composable
fun LabelerButtons(
    modifier: Modifier = Modifier,
    subscribed: Boolean = false,
    onSubscribeClicked: () -> Unit = {},
    onUnsubscribeClicked: () -> Unit = {},
    onMenuClicked: () -> Unit = {},
) {
    var isSubscribed by remember { mutableStateOf(subscribed) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 2.dp)
    ) {
        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = if(isSubscribed) "Unsubscribe" else "Subscribe",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = MaterialTheme.typography.labelLarge
                        .fontSize.times(0.9)
                )
            },
            icon = {
            },
            onClick = {
                if(isSubscribed) onUnsubscribeClicked() else onSubscribeClicked()
                isSubscribed = !isSubscribed
            },
            shape = ButtonDefaults.filledTonalShape,
            modifier = modifier
                .heightIn(min = 30.dp, max = 48.dp)
        )
        ProfileMenuButton(onClick = onMenuClicked)
    }
}