package com.bellfamily.bastischool.audio

/** Defensive native boundary, not a substitute for authored speech or reviewed phoneme recordings. */
internal fun sanitizeSpeech(text: String): String {
    val result = StringBuilder()
    var offset = 0
    while (offset < text.length) {
        val cp = text.codePointAt(offset)
        offset += Character.charCount(cp)
        // Strip entire keycaps (including their digit), not ordinary authored numbers.
        if ((cp in 48..57 || cp == 35 || cp == 42) &&
            (text.startsWith("\u20e3", offset) || text.startsWith("\ufe0f\u20e3", offset))) {
            result.append(' ')
            continue
        }
        val decoration = cp in 0x1f000..0x1faff || cp in 0x2600..0x27bf ||
            cp in 0x2190..0x21ff || cp in 0x2300..0x23ff || cp in 0x2b00..0x2bff ||
            cp in 0x25a0..0x25ff || cp in 0xfe00..0xfe0f || cp in 0xe0020..0xe007f ||
            cp == 0x200d || cp == 0x20e3 || cp == 0x00a9 || cp == 0x00ae || cp == 0x2122 ||
            cp == 0x3030 || cp == 0x303d || cp == 0x3297 || cp == 0x3299
        if (decoration || Character.isWhitespace(cp) || Character.isSpaceChar(cp)) result.append(' ')
        else result.appendCodePoint(cp)
    }
    return result.toString().replace(Regex(" +"), " ").trim()
}
