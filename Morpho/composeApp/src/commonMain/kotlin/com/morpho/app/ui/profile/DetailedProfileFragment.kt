package com.morpho.app.ui.profile


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.morpho.app.model.bluesky.DetailedProfile


@OptIn(
    ExperimentalMaterial3Api::class,
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
)