package com.morpho.app.util
import android.icu.text.BreakIterator
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder

val graphemeBoundary: BreakIterator = BreakIterator.getCharacterInstance()

fun countGraphemes(text: String): Int {
    graphemeBoundary.setText(text)
    var count = 0
    while (graphemeBoundary.next() != BreakIterator.DONE) {
        count++
    }
    return count
}

val utf8: CharsetEncoder = Charset.forName("UTF-8").newEncoder()
val ascii: CharsetEncoder = Charset.forName("US-ASCII").newEncoder()

fun encodeUtf8(string: String): ByteBuffer? {
    utf8.reset()
    if (utf8.canEncode(string)) {
        return utf8.encode(CharBuffer.wrap(string))
    } else return null
}

fun encodeAscii(string: String): ByteBuffer? {
    ascii.reset()
    if (ascii.canEncode(string)) {
        return ascii.encode(CharBuffer.wrap(string))
    } else return null
}

fun utf8Length(string: String): Int? {
    ascii.reset()
    // Double check perf on this vs just iterating through until you find something that isn't ASCII
    if (ascii.canEncode(string))  return string.length

    val encoded = encodeUtf8(string)
    if (encoded != null) return encoded.position()
    return null // Some sort of encoding error, might need more explicit error-checking/handling in prev function
}
