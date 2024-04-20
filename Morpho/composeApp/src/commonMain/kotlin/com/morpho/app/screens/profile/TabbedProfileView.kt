package com.morpho.app.screens.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import coil3.annotation.ExperimentalCoilApi
import com.morpho.app.model.bluesky.BskyLabelService
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.Profile
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.base.tabbed.TabScreen
import com.morpho.app.ui.common.TabbedSkylineFragment
import com.morpho.app.ui.profile.DetailedProfileFragment
import com.morpho.app.ui.profile.ProfileTabRow
import com.morpho.app.ui.profile.ProfileTabs
import com.morpho.butterfly.AtUri
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabbedProfileTopBar(profile: Profile?, ownProfile: Boolean, scrollBehavior: TopAppBarScrollBehavior,
                        switchTab:(AtUri) -> Unit) {
    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        when(profile != null) {
            true -> {
                when(profile) {
                    is DetailedProfile -> DetailedProfileFragment(
                        profile = profile,
                        myProfile = ownProfile,
                        isTopLevel = true,
                        scrollBehavior = scrollBehavior,
                        onBackClicked = {
                            navigator.pop()
                        },
                    )
                    is BskyLabelService -> { TODO("Make different title card for label services")}
                    else -> { /* Shouldn't happen */ }
                }


                ProfileTabRow(
                    id = profile.did,
                    selected = ProfileTabs.Posts,
                    onTabChanged = { uri -> switchTab(uri) },
                    ownProfile = ownProfile,
                    isLabeler = profile is BskyLabelService,
                )
            }
            false -> {
                // Loading
            }
        }

    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabScreen.TabbedProfileContent(
    screenModel: TabbedProfileViewModel,
    ownProfile: Boolean,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    LifecycleEffect(
        onStarted = {

        },
        onDisposed = {},
    )
    val tabs = rememberSaveable {
        screenModel.profileUiState.tabs.mapIndexed { index, entry ->
            ProfileSkylineTab(
                index = index.toUShort(),
                screenModel = screenModel,
                state = screenModel.profileUiState.tabMap[entry.uri],
                paddingValues = paddingValues,
                ownProfile = ownProfile,
            )
        }
    }
    TabNavigator(
        tabs.first(),
        tabDisposable = { TabDisposable(it, tabs = tabs) }
    ) {
        CurrentTab()
    }


}


data class ProfileSkylineTab(
    val index: UShort,
    val screenModel: TabbedProfileViewModel,
    val state: ContentCardState.ProfileTimeline<MorphoDataItem>?,
    val paddingValues: PaddingValues = PaddingValues(0.dp),
    val ownProfile: Boolean = false,
): Tab {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        TabbedSkylineFragment(screenModel, state, paddingValues)
    }


    @OptIn(ExperimentalResourceApi::class, ExperimentalCoilApi::class)
    override val options: TabOptions
        @Composable
        get() {
            /* Curious if this works for tab icons
            val icon = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .fallback(ImageRequest.Builder(LocalPlatformContext.current)
                        .data(imageResource(Res.drawable.placeholder_pfp).asSkiaBitmap())
                        .build().fallbackFactory)
                    .data(state.profile.avatar)
                    .crossfade(true)
                    .build(),
            )
            */

            val name = rememberSaveable {
                if (state?.profile?.displayName != null && state.profile.displayName!!.isNotEmpty()) {
                    state.profile.displayName!!
                } else { state?.profile?.handle?.handle.orEmpty() }
            }
            return TabOptions(
                index = index,
                title = name,
                //icon = icon,
            )
        }

}
