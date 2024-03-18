package com.morpho.app.util
import android.icu.text.BreakIterator
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import java.nio.charset.StandardCharsets

val graphemeBoundary: BreakIterator = BreakIterator.getCharacterInstance()

fun countGraphemes(text: String): Int {
    graphemeBoundary.setText(text)
    var count = 0
    while (graphemeBoundary.next() != BreakIterator.DONE) {
        count++
    }
    return count
}


fun utf8Length(string: String): Int {
    return string.encodeUtf8().size
}

