package morpho.app.ui.profile

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileMenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    SmallFloatingActionButton(
        onClick = onClick,
        shape = ButtonDefaults.filledTonalShape,
        modifier = modifier
            .sizeIn(
                minWidth = 25.dp,
                maxWidth = 30.dp,
                minHeight = 25.dp,
                maxHeight = 30.dp
            )
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Menu",
            modifier = Modifier
                .size(18.dp)
        )
    }
}