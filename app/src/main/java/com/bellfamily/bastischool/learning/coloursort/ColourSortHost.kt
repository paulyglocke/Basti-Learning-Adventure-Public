package com.bellfamily.bastischool.learning.coloursort

import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.learning.progress.*

/** Keeps the existing consumer API and unchanged CSR1 journal bytes. */
class ColourSortHost(disk: ProgressStorage, progress: ProgressRepository) : SortingHost(disk, progress, ColourSort) {
    companion object {
        internal fun encode(s: SortState, events: List<ProgressEvent>) = SortingHost.encode(ColourSort, s, events)
        internal fun decode(bytes: ByteArray) = SortingHost.decode(ColourSort, bytes)
    }
}
