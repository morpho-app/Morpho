package com.morpho.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.morpho.app.ui.post.PlaceholderSkylineItem
import com.morpho.app.ui.post.PostFragmentRole
import com.morpho.app.ui.theme.MorphoTheme

@Preview
@Composable
fun PreviewPlaceholderSkylineItem() {
    MorphoTheme {
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
}