package radiant.nimbus.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ScreenLayout {
    SingleColumn,
    TwoColumn,
}

enum class NavBarLocation {
    BottomFull,
    BottomPartial,
    SideRail,
    SideDrawer,
}

/**
 * Generates a column composable, that adds 12.dp screen horizontal padding,
 * which the standard across the app.
 */
@Composable
fun ScreenBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 0.dp),
    layout: ScreenLayout = ScreenLayout.SingleColumn,
    navBarLocation: NavBarLocation = NavBarLocation.BottomFull,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    snackbarHost: @Composable () -> Unit = { },
    topContent: @Composable () -> Unit = { },
    navBar: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets.navigationBars,
    content2: @Composable (PaddingValues) -> Unit = {},
    content1: @Composable (PaddingValues) -> Unit,
) {
    when(navBarLocation) {
        NavBarLocation.BottomFull -> {
            when(layout) {
                ScreenLayout.SingleColumn -> Scaffold(
                    modifier = Modifier.padding(contentPadding),
                    topBar = topContent,
                    snackbarHost = snackbarHost,
                    bottomBar = navBar,
                    contentWindowInsets = contentWindowInsets,
                    ) { padding ->
                    content1(padding)
                }
                ScreenLayout.TwoColumn -> Scaffold(
                    modifier = Modifier.padding(contentPadding),
                    topBar = topContent,
                    snackbarHost = snackbarHost,
                    bottomBar = navBar,
                    contentWindowInsets = contentWindowInsets,
                ) { padding ->
                    Row() {
                        content1(padding)
                        content2(padding)
                    }
                }
            }
        }
        NavBarLocation.BottomPartial -> {
            when(layout) {
                ScreenLayout.SingleColumn -> Scaffold(
                    modifier = Modifier.padding(contentPadding),
                    topBar = topContent,
                    snackbarHost = snackbarHost,
                    bottomBar = navBar,
                    contentWindowInsets = contentWindowInsets,
                ) { padding ->
                    content1(padding)
                }
                ScreenLayout.TwoColumn -> Row() {
                    Scaffold(
                        modifier = Modifier.padding(contentPadding),
                        topBar = topContent,
                        snackbarHost = snackbarHost,
                        bottomBar = navBar,
                        contentWindowInsets = contentWindowInsets,
                    ) { padding ->
                        content1(padding)
                    }
                    content2(contentPadding)
                }
            }
        }
        NavBarLocation.SideRail -> {
            when(layout) {
                ScreenLayout.SingleColumn -> Row {
                    navBar.invoke()
                    Scaffold(
                        modifier = Modifier.padding(contentPadding),
                        topBar = topContent,
                        snackbarHost = snackbarHost,
                        contentWindowInsets = contentWindowInsets,
                    ) { padding ->
                        content1(padding)
                    }
                }
                ScreenLayout.TwoColumn -> Row() {
                    navBar.invoke()
                    Scaffold(
                        modifier = Modifier.padding(contentPadding),
                        topBar = topContent,
                        snackbarHost = snackbarHost,
                        contentWindowInsets = contentWindowInsets,
                    ) { padding ->
                        content1(padding)
                    }
                    content2(contentPadding)
                }
            }
        }
        NavBarLocation.SideDrawer -> {
            when(layout) {
                ScreenLayout.SingleColumn -> TODO()
                ScreenLayout.TwoColumn -> TODO()
            }
        }
    }
}