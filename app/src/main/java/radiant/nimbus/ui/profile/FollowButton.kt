package radiant.nimbus.ui.profile

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FollowButton(
    modifier: Modifier = Modifier,
    following: Boolean = false,
) {

    val label: String = if (following) {
        "Following"
    } else {
        "Follow"
    }

    ExtendedFloatingActionButton(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontSize = MaterialTheme.typography.labelLarge
                    .fontSize.times(0.9)
            )
        },
        icon = {

            Icon(
                imageVector =
                if (following) {
                    Icons.Filled.Check
                } else {
                    Icons.Filled.Add
                },
                contentDescription = "$label Icon",
                modifier = Modifier.size(19.dp)
            )
        },
        onClick = { /*TODO*/ },
        shape = ButtonDefaults.filledTonalShape,
        modifier = modifier
            .heightIn(min = 30.dp, max = 48.dp)
    )
}