package com.morpho.app.util


import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import kotlin.experimental.and
import kotlin.math.max
import kotlin.math.min

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

fun String.utf16FacetIndex(utf8: ByteString, start: Int, end: Int): Pair<Int, Int> {
    val utf8FacetText = utf8.substring(start, end)
    //println("utf8FacetText: '${utf8FacetText.utf8()}'")
    //println("utf8Start: ${utf8.indexOf(utf8FacetText)}, utf8End: ${utf8.indexOf(utf8FacetText) + utf8FacetText.size}")
    val utf16FacetText = utf8FacetText.utf8()
    val utf16Start = this.indexOf(utf16FacetText)
    val utf16End = utf16Start + utf16FacetText.length
    //println("utf16Start: $utf16Start, utf16End: $utf16End")
    return Pair(utf16Start, utf16End)
}

fun String.utf16FacetIndex(start: Int, end: Int): Pair<Int, Int> {
    val utf8 = this.encodeUtf8()
    val utf8FacetText = utf8.substring(start, end-1)
    //println("utf8FacetText: '${utf8FacetText.utf8()}'")
    //println("utf8Start: ${utf8.indexOf(utf8FacetText)}, utf8End: ${utf8.indexOf(utf8FacetText) + utf8FacetText.size}")
    val utf16FacetText = utf8FacetText.utf8()
    val utf16Start = this.indexOf(utf16FacetText)
    val utf16End = utf16Start + utf16FacetText.length
    //println("utf16Start: $utf16Start, utf16End: $utf16End")
    return Pair(utf16Start, utf16End)
}

fun String.utf8Slice(indices: OpenEndRange<Int>): String {
    val utf8 = this.encodeUtf8()
    return try {
        utf8.substring(indices.start, max(indices.start, min(indices.endExclusive, utf8.size - 1))).utf8()
    } catch (e: IllegalStateException) {
        utf8.substring(indices.start, utf8.size - 1).utf8()
    }
}



fun String.utf8Split(index: Int): Pair<String, String> {
    val utf8 = this.encodeUtf8()
    return Pair(utf8.substring(0, min(index-1, utf8.size - 1)).utf8(), utf8.substring(index).utf8())
}