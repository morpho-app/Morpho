package com.morpho.app.com.morpho.app

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.morpho.app.ui.post.PlaceholderSkylineItem
import com.morpho.app.ui.post.PostFragmentRole

@Preview
@Composable
fun PreviewPlaceholderSkylineItem() {
    //MorphoTheme {
        Column {
            Column {
                PlaceholderSkylineItem()
                PlaceholderSkylineItem(role = PostFragmentRole.PrimaryThreadRoot)
                PlaceholderSkylineItem(role = PostFragmentRole.ThreadBranchStart)
                PlaceholderSkylineItem(role = PostFragmentRole.ThreadBranchMiddle)
                PlaceholderSkylineItem(role = PostFragmentRole.ThreadBranchEnd)
                PlaceholderSkylineItem(role = PostFragmentRole.ThreadRootUnfocused)
                PlaceholderSkylineItem(role = PostFragmentRole.ThreadEnd)
                PlaceholderSkylineItem(elevate = true)
            }
        }
    //}
}