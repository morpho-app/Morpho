package radiant.nimbus.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import radiant.nimbus.R
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.ui.elements.RichText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
public fun DetailedProfileFragment(
    profile: DetailedProfile,
    //onValueChange: (DetailedProfile) -> Unit,
    modifier: Modifier = Modifier,
    myProfile: Boolean = false,
    isTopLevel:Boolean = false,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 12.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val (appbar, userStats, banner, labels) = createRefs()
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profile.banner.orEmpty())
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.test_banner),
                contentDescription = "Profile Banner for ${profile.displayName} ${profile.handle}",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    // Set image size to 40 dp
                    .fillMaxWidth()
                    .height(120.dp)
                    .constrainAs(banner) {
                        top.linkTo(parent.top)
                    }

            )
            ProfileLabels(
                labels = profile.labels,
                modifier = Modifier
                    .constrainAs(labels) {
                        top.linkTo(anchor = parent.top, margin = 12.dp)
                        end.linkTo(anchor = parent.end, margin = 8.dp)
                    }
            )

            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            LargeTopAppBar(
                title = {
                    ConstraintLayout(//constraintSet = ,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                    ) {
                        val (avatar, buttons) = createRefs()
                        val avatarGuide = createGuidelineFromStart(.1f)
                        val centreGuide = createGuidelineFromTop(.6f)

                        OutlinedAvatar(
                            url = profile.avatar.orEmpty(),
                            contentDescription = "Avatar for ${profile.displayName} ${profile.handle}",
                            modifier = Modifier
                                .size(80.dp)
                                .constrainAs(avatar) {
                                    centerAround(avatarGuide)

                                }
                        )
                        ProfileButtons(
                            myProfile = myProfile,
                            modifier = Modifier
                                .constrainAs(buttons) {
                                    centerAround(centreGuide)
                                    end.linkTo(parent.end, 12.dp)
                                }
                        )
                    }
                },
                navigationIcon = {
                    if (isTopLevel) {
                        IconButton(
                            onClick = { /* doSomething() */ },
                            modifier = Modifier
                                .size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                //tint =
                            )
                        }
                    }
                },
                actions = {

                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.constrainAs(appbar) {

                }
            )
            SelectionContainer(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .constrainAs(userStats) {
                        bottom.linkTo(appbar.bottom, (-15).dp)
                        end.linkTo(parent.end)
                    }
            ) {
                UserStatsFragment(
                    profile = profile,
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                )
            }

        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 0.dp)
        ) {
            val name = profile.displayName ?: profile.handle.handle
            SelectionContainer {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            SelectionContainer {
                Text(
                    text = " @${profile.handle}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SelectionContainer {
                RichText(profile)
            }
        }

    }
}