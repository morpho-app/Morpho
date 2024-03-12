package com.morpho.app.util

data class BlueskyText(
    val text: String,

)

fun getGraphemeLength(value: String): Long {
    return value.codePoints().count()
}