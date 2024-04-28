package com.morpho.app.ui.profile


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.constraintlayout.compose.platform.annotation.SuppressLint
import com.morpho.app.model.bluesky.BskyLabel
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.uidata.Moment
import com.morpho.app.ui.common.TopAppBarPreview
import com.morpho.app.ui.theme.MorphoTheme
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview


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
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DetailedProfilePreview() {
    var selectedTab by rememberSaveable { mutableStateOf(ProfileTabs.Posts) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    MorphoTheme {

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                ) {

                    DetailedProfileFragment(
                        profile = testProfile,
                        myProfile = true,
                        isTopLevel = true,
                        scrollBehavior = scrollBehavior
                    )
                    PreviewProfileTabRow(selectedTab) {
                        selectedTab = it
                    }
                }

            },
            contentWindowInsets = WindowInsets.systemBars,
            modifier = Modifier.consumeWindowInsets(WindowInsets.systemBars)
        ) {
            TopAppBarPreview()
        }
    }


}

val testProfile = DetailedProfile(
    did = Did("did:plc:yfvwmnlztr4dwkb7hwz55r2g"),
    handle = Handle("nonbinary.computer"),
    displayName = "Orual",
    avatar = "https://av-cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreifpzcenp6rhmxohv3kkez4uv4ldjphiysmju6scwgne34nb245wra@jpeg",
    banner = "https://av-cdn.bsky.app/img/banner/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreihed7ohctbeer7l5fmpu4dqwxarfvxiyoik5tyd6lhelvp76yr2wm@jpeg",
    description = "Person who does computer and music things.\nthey/she",
    followersCount = 592,
    followsCount = 470,
    indexedAt = Moment(Instant.parse("2023-05-24T20:04:20-05")),
    labels = persistentListOf(
        BskyLabel("testLabel1"),
        BskyLabel("testLabel2")
    ),
    postsCount = 2743,
    mutedByMe = false,
    followingMe = false,
    followedByMe = false,
)