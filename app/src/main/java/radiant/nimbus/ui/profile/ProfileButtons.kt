package radiant.nimbus.ui.profile

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileButtons(
    modifier: Modifier = Modifier,
    myProfile: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 2.dp)
    ) {
        if (myProfile) {
            EditProfileButton(
                modifier = modifier
                    .padding(horizontal = 5.dp)
            )
        } else {
            FollowButton(
                modifier = modifier
                    .padding(horizontal = 5.dp)
            )
        }
        ProfileMenuButton()
    }

}