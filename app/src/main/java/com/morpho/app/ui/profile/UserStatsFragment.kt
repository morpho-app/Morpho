package com.morpho.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.morpho.app.model.DetailedProfile

@Composable
@OptIn(ExperimentalLayoutApi::class)
public fun UserStatsFragment(
    profile: DetailedProfile,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.End,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            //.offset(y = 125.dp)
            .heightIn(20.dp, 30.dp)
    ) {
        TextButton(
            onClick = { /*TODO*/ },
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
            modifier = Modifier
                .heightIn(min = 20.dp, max = 48.dp)
                .defaultMinSize(minWidth = 10.dp)
        ) {
            Text(
                text = "${profile.followersCount}",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = " Followers",
                fontSize = MaterialTheme.typography.labelMedium.fontSize.times(0.9),
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        TextButton(
            onClick = { /*TODO*/ },
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
            modifier = Modifier
                .heightIn(min = 20.dp, max = 48.dp)
                .defaultMinSize(minWidth = 10.dp)
        ) {
            Text(
                text = "${profile.followsCount}",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = " Following",
                fontSize = MaterialTheme.typography.labelMedium.fontSize.times(0.9),
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        TextButton(
            onClick = { /*TODO*/ },
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
            modifier = Modifier
                .heightIn(min = 20.dp, max = 48.dp)
                .defaultMinSize(minWidth = 5.dp)
        ) {
            Text(
                text = "${profile.postsCount}",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            )
            Text(
                text = " Posts",
                fontSize = MaterialTheme.typography.labelMedium.fontSize.times(0.9),
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Start
            )
        }
    }
}