package com.bellfamily.bastischool.learning.content

import com.bellfamily.bastischool.learning.models.*
import org.junit.Assert.*
import org.junit.Test

class ContentRepositoryTest {
    private val repository = CoreContent.repository()
    private fun rebuild(records: List<ContentDefinition>, order: List<ContentId> = WeekdayIds.canonicalOrder) =
        BundledContentRepository(CoreContent.version, records, order, repository.images())

    @Test fun bilingualFieldsAreRequiredWithoutFallback() {
        for (blank in listOf("", " ", "\n\t")) {
            assertThrows(IllegalArgumentException::class.java) { LocalizedText(blank, "Montag") }
            assertThrows(IllegalArgumentException::class.java) { LocalizedText("Monday", blank) }
            assertThrows(IllegalArgumentException::class.java) {
                ContentText(LocalizedText("Monday", "Montag"), LocalizedText("Monday", blank))
            }
        }
        val text = LocalizedText("large", "größer – weiß!")
        assertEquals("large", text[ContentLanguage.ENGLISH])
        assertEquals("größer – weiß!", text[ContentLanguage.GERMAN])
    }

    @Test fun decoratedDisplayAndSpeechAreIndependentAuthoredValues() {
        val text = ContentText(LocalizedText("Yes! 🎉", "Ja! 🎉"), LocalizedText("Yes!", "Ja!"))
        assertEquals("Ja! 🎉", text.display[ContentLanguage.GERMAN])
        assertEquals("Ja!", text.speech[ContentLanguage.GERMAN])
        assertEquals(ContentText.plain("Monday", "Montag").display, ContentText.plain("Monday", "Montag").speech)
    }

    @Test fun semanticIdentityAndVersionRejectInvalidValues() {
        for (id in listOf("", "Monday", "day.Monday", "day..monday", "day.monday.png/", " day.monday", "skill.spatial.under")) {
            assertThrows(IllegalArgumentException::class.java) { ContentId(id) }
        }
        assertEquals(ContentId("animal.komodo_dragon"), ContentId("animal.komodo_dragon"))
        assertThrows(IllegalArgumentException::class.java) { ContentVersion(0, 1) }
        assertThrows(IllegalArgumentException::class.java) { ContentVersion(1, -1) }
        assertThrows(IllegalArgumentException::class.java) {
            ColourDefinition(ContentId("day.green"), ContentText.plain("green", "grün"))
        }
    }

    @Test fun weekdaysHaveStableIdsAuthoredLabelsAndIndependentColourCues() {
        val days = repository.weekdays()
        assertEquals(listOf("day.monday", "day.tuesday", "day.wednesday", "day.thursday", "day.friday", "day.saturday", "day.sunday"), days.map { it.id.value })
        assertEquals(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"), days.map { it.text.display.en })
        assertEquals(listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"), days.map { it.text.display.de })
        assertEquals(listOf("green", "red", "yellow", "blue", "purple", "orange", "pink"), days.map { it.colourCue.value.removePrefix("colour.") })
        days.forEach { day ->
            assertEquals(day.text.display, day.text.speech)
            assertTrue(repository.find(day.colourCue) is ColourDefinition)
            assertNotEquals(day.id, day.colourCue)
        }
    }

    @Test fun allSevenDaysHaveCyclicPreviousAndNextFromCanonicalData() {
        val ids = WeekdayIds.canonicalOrder
        ids.forEachIndexed { i, id ->
            assertEquals(ids[(i + 1) % 7], repository.nextDay(id).id)
            assertEquals(ids[(i + 6) % 7], repository.previousDay(id).id)
            assertEquals(id, repository.previousDay(repository.nextDay(id).id).id)
        }
        assertEquals(WeekdayIds.SUNDAY, repository.previousDay(WeekdayIds.MONDAY).id)
        assertEquals(WeekdayIds.MONDAY, repository.nextDay(WeekdayIds.SUNDAY).id)
    }

    @Test fun lookupIsDeterministicAndDoesNotDependOnInsertionOrder() {
        val reversed = rebuild(repository.all().reversed())
        assertEquals(repository.version, reversed.version)
        assertEquals(repository.all(), reversed.all())
        assertEquals(repository.weekdays(), reversed.weekdays())
        repository.all().forEach { assertEquals(it, reversed.find(it.id)) }
        assertEquals(repository.all(), CoreContent.repository().all())
        assertNull(repository.find(ContentId("animal.unknown")))
        assertThrows(IllegalArgumentException::class.java) { repository.nextDay(ContentId("colour.green")) }
        assertThrows(IllegalArgumentException::class.java) { repository.previousDay(ContentId("day.unknown")) }
    }

    @Test fun duplicateIdsAreRejectedInsteadOfSilentlyOverwritten() {
        val invalid = repository.all() + repository.weekdays().first()
        assertTrue(ContentValidator.validate(invalid, WeekdayIds.canonicalOrder).any { it == "Duplicate content ID: day.monday" })
        assertThrows(IllegalArgumentException::class.java) { rebuild(invalid) }
    }

    @Test fun missingOrWrongTypeColourReferencesAreRejectedWithOwnerAndTarget() {
        val monday = repository.weekdays().first()
        for (reference in listOf(ContentId("colour.missing"), WeekdayIds.TUESDAY)) {
            val invalid = repository.all().map { if (it.id == monday.id) monday.copy(colourCue = reference) else it }
            val errors = ContentValidator.validate(invalid, WeekdayIds.canonicalOrder)
            assertTrue(errors.any { "day.monday" in it && reference.value in it })
            assertThrows(IllegalArgumentException::class.java) { rebuild(invalid) }
        }
    }

    @Test fun missingUnknownDuplicateAndReorderedWeekdaysAreRejected() {
        val ids = WeekdayIds.canonicalOrder
        for (order in listOf(ids.dropLast(1), ids + ids.first(), ids.reversed(), ids.dropLast(1) + ContentId("day.unknown"))) {
            assertThrows(IllegalArgumentException::class.java) { rebuild(repository.all(), order) }
        }
        assertThrows(IllegalArgumentException::class.java) { rebuild(repository.all().filterNot { it.id == WeekdayIds.SUNDAY }) }
        val extra = WeekdayDefinition(ContentId("day.extra"), ContentText.plain("Extra", "Extra"), ContentId("colour.red"))
        assertThrows(IllegalArgumentException::class.java) { rebuild(repository.all() + extra) }
    }

    @Test fun metadataChangesCannotChangeDayIdentityOrSequence() {
        val monday = repository.weekdays().first()
        val changed = monday.copy(text = ContentText.plain("Monday!", "Montag!"), colourCue = ContentId("colour.red"))
        val other = rebuild(repository.all().map { if (it.id == monday.id) changed else it })
        assertEquals(monday.id, other.weekdays().first().id)
        assertEquals(WeekdayIds.TUESDAY, other.nextDay(monday.id).id)
        assertEquals(WeekdayIds.SUNDAY, other.previousDay(monday.id).id)
    }

    @Test fun repositorySnapshotCannotBeChangedThroughInputOrReturnedCollections() {
        val records = repository.all().toMutableList()
        val order = WeekdayIds.canonicalOrder.toMutableList()
        val snapshot = rebuild(records, order)
        records.clear(); order.clear()
        assertEquals(18, snapshot.all().size)
        assertEquals(7, snapshot.weekdays().size)
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.all() as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.weekdays() as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (WeekdayIds.canonicalOrder as MutableList).clear() }
    }

    @Test fun nonCalendarCatalogDoesNotRequireOrInventWeekdays() {
        val colour = repository.find(ContentId("colour.green"))!!
        val coloursOnly = rebuild(listOf(colour), emptyList())
        assertTrue(coloursOnly.weekdays().isEmpty())
        assertEquals(colour, coloursOnly.find(colour.id))
        assertThrows(IllegalArgumentException::class.java) { coloursOnly.nextDay(WeekdayIds.MONDAY) }
    }
}
