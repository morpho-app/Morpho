package com.morpho.app.util


import okio.ByteString.Companion.encodeUtf8
/*import org.jetbrains.skia.BreakIterator


val graphemeBoundary: BreakIterator = BreakIterator.makeCharacterInstance()

fun countGraphemes(text: String): Int {
    graphemeBoundary.setText(text)
    var count = 0
    while (graphemeBoundary.next() != BreakIterator.DONE) {
        count++
    }
    return count
}*/


fun utf8Length(string: String): Int {
    return string.encodeUtf8().size
}

