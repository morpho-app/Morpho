package com.morpho.app.util


import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import kotlin.experimental.and

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

/**
 * Determines if this byte is a single-byte utf-8 character.
 */
fun Byte.isSingleByte(): Boolean {
    return (this and 0b10000000.toByte()) == 0.toByte()
}

fun utf16FacetIndex(text: String, utf8: ByteString, start: Int, end: Int): Pair<Int, Int> {
    val utf8FacetText = utf8.substring(start, end)
    //println("utf8FacetText: '${utf8FacetText.utf8()}'")
    //println("utf8Start: ${utf8.indexOf(utf8FacetText)}, utf8End: ${utf8.indexOf(utf8FacetText) + utf8FacetText.size}")
    val utf16FacetText = utf8FacetText.utf8()
    val utf16Start = text.indexOf(utf16FacetText)
    val utf16End = utf16Start + utf16FacetText.length
    //println("utf16Start: $utf16Start, utf16End: $utf16End")
    return Pair(utf16Start, utf16End)
}