package com.morpho.app.util
import android.icu.text.BreakIterator

//fun splitGraphemes(string: String): List<String> {
//    val graphemes: MutableList<String> = mutableListOf()
//    string.forEachIndexed { index, c ->
//        if (c.is)
//    }
//}

fun countGraphemes(text: String): Int {
    val boundary = BreakIterator.getCharacterInstance()
    boundary.setText(text)
    var count = 0
    while (boundary.next() != BreakIterator.DONE) {
        count++
    }
    return count
}