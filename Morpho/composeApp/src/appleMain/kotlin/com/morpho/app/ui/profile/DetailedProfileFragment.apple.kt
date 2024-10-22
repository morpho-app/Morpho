package com.morpho.app.ui.profile

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.morpho.app.model.bluesky.BskyLabelService
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.uidata.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DetailedProfileFragment(
    profile: DetailedProfile,
    modifier: Modifier,
    myProfile: Boolean,
    isTopLevel: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClicked: () -> Unit,
    eventCallback: (Event) -> Unit,
) {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun LabelerProfileFragment(
    labeler: BskyLabelService,
    modifier: Modifier,
    isSubscribed: Boolean,
    isTopLevel: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClicked: () -> Unit,
    eventCallback: (Event) -> Unit,
) {
}