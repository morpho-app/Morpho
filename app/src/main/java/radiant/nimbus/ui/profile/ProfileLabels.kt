package radiant.nimbus.ui.profile

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import radiant.nimbus.model.BskyLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileLabels(
    modifier: Modifier = Modifier,
    labels: ImmutableList<BskyLabel>
) {
    FlowRow(
        modifier = modifier
    ) {
        labels.forEach {
            ProfileLabel(
                label = it,
                modifier = modifier

            )
        }
    }
}