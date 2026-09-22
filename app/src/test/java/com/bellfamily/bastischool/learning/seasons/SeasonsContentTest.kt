package com.bellfamily.bastischool.learning.seasons

import com.bellfamily.bastischool.learning.content.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SeasonsContentTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun deterministicFiveAndTenUseAllSeasonsAndOrderedAuthoredChoices() {
        for(round in RoundLength.entries) {
            fun generate() = (SeasonsContent.generate(SessionId("seasons"),round,42) as GenerationResult.Generated).plan
            val first=generate();val second=generate()
            assertEquals(round.count,first.tasks.size)
            assertEquals(round.count,first.tasks.map {it.id}.toSet().size)
            assertEquals(SeasonIds.canonicalOrder.toSet(),first.tasks.take(4).map {it.question.correct}.toSet())
            val en=SessionReducer.start(first,ContentLanguage.ENGLISH,SeasonsContent.repository).state
            val same=SessionReducer.start(second,ContentLanguage.ENGLISH,SeasonsContent.repository).state
            assertArrayEquals(SessionCheckpoint.encode(en),SessionCheckpoint.encode(same))
            first.tasks.forEach { task ->
                assertEquals(SeasonIds.canonicalOrder.toSet(),task.question.choices.toSet())
                for(lang in ContentLanguage.entries) {
                    val names=task.question.choices.map {SeasonsContent.season(it).text.speech[lang]}
                    assertTrue(task.question.instruction.speech[lang].contains(names.joinToString(", ")))
                    assertEquals(SeasonsContent.narration(task.question.correct),task.question.hint)
                }
            }
            SeasonsContent.validate(en)
        }
    }
    @Test fun everyExploreSelectionUsesCanonicalContentInBothLanguages() {
        val core=CoreContent.repository()
        SeasonIds.canonicalOrder.forEach { id ->
            val selected=SeasonsSelection().select(id)
            val expected=core.find(id) as SeasonDefinition
            assertEquals(expected,selected.season)
            assertEquals(core.image(expected.illustration),selected.image)
            for(lang in ContentLanguage.entries) assertEquals(expected.spokenDescription[lang],SeasonsContent.narration(id).speech[lang])
        }
        assertThrows(IllegalArgumentException::class.java) { SeasonsSelection(ContentId("day.monday")) }
        assertThrows(IllegalArgumentException::class.java) { SeasonsContent.question(SeasonIds.SPRING,listOf(SeasonIds.SPRING)) }
    }
    @Test fun browsingCheckpointRestoresEverySelectionAndPhaseWithoutQuiz() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        assertEquals(SeasonsSelection(),SeasonsSelectionStore(disk).read())
        for(id in SeasonIds.canonicalOrder) for(phase in SeasonsPhase.entries) {
            val state=SeasonsSelection(id,phase)
            SeasonsSelectionStore(disk).write(state)
            assertEquals(state,SeasonsSelectionStore(disk).read())
        }
    }
    @Test fun malformedOrIncompatibleBrowsingCheckpointIsPreserved() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        val store=SeasonsSelectionStore(disk)
        store.write(SeasonsSelection())
        val valid=disk.access {it.read()!!}
        for(bytes in listOf(byteArrayOf(1),valid+byteArrayOf(0),valid.copyOf().also {it[3]=2})) {
            disk.access {it.replace(bytes)}
            assertThrows(Exception::class.java) {store.read()}
            assertArrayEquals(bytes,disk.access {it.read()})
        }
    }
}
