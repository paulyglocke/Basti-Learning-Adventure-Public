package com.bellfamily.bastischool.learning.colours

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.progress.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ColoursSelectionStoreTest {
    @get:Rule val temp = TemporaryFolder()
    private val storage get() = AtomicProgressStorage(temp.newFolder(), JvmAtomicCommit)

    @Test fun browseSelectionHasIndependentSmallDurableStateAndRejectsNonIntroColours() {
        val disk = storage
        val store = ColoursSelectionStore(disk)
        assertEquals(ColoursContent.red, store.read())
        store.write(ColoursContent.green)
        assertEquals(ColoursContent.green, ColoursSelectionStore(disk).read())
        assertThrows(IllegalArgumentException::class.java) { store.write(ContentId("colour.purple")) }
    }
}
