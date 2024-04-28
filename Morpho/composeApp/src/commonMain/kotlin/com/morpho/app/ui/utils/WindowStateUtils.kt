package com.morpho.app.ui.utils


/**
 * Different type of navigation supported by app depending on size and state.
 */
enum class NavigationType {
    BOTTOM_NAVIGATION, NAVIGATION_RAIL, PERMANENT_NAVIGATION_DRAWER
}

/**
 * Content shown depending on size and state of device.
 */
enum class ContentType {
    LIST_ONLY, LIST_AND_DETAIL
}

/**
 * Different type of window size class supported by app.
 */
enum class WindowSize {
    SMALL_PORTRAIT, SMALL_LANDSCAPE, FOLDABLE_BOOK, FOLDABLE_OPEN_PORTRAIT, FOLDABLE_OPEN_LANDSCAPE, TABLET_PORTRAIT, TABLET_LANDSCAPE, LARGE
}

expect fun getWindowSizeClass() : WindowSize