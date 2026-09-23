package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class VocabularyContentTest {
    @get:Rule val temp=TemporaryFolder()
    @Test fun exactReviewedWordsHaveSeparateBilingualSpeechAndExplicitTemporaryVisuals() {
        val expected=mapOf("crocodile" to ("Crocodile" to "Krokodil"),"dinosaur" to ("Dinosaur" to "Dinosaurier"),
            "fish" to ("Fish" to "Fisch"),"horse" to ("Horse" to "Pferd"),"snake" to ("Snake" to "Schlange"),"whale" to ("Whale" to "Wal"))
        assertEquals(expected.keys.map {ContentId("animal.$it")}.toSet(),VocabularyContent.items.map {it.id}.toSet())
        VocabularyContent.items.forEach {item ->
            val names=expected.getValue(item.id.value.substringAfter('.'))
            assertEquals(names.first,item.text.display.en);assertEquals(names.second,item.text.display.de)
            assertEquals(item.id,item.visual.animal);assertTrue(item.visual.glyph.isNotBlank())
            assertEquals(ContentId("category.animals"),item.category)
            assertTrue(VocabularyContent.repository.find(item.category) is VocabularyCategory)
            for(lang in ContentLanguage.entries) {
                assertEquals(item.text.display[lang],item.text.speech[lang])
                assertFalse(item.findPrompt.speech[lang].contains(item.visual.glyph))
                assertTrue(item.example.speech[lang].isNotBlank())
            }
        }
        assertTrue(VocabularyContent.repository.images().isEmpty()) // No fabricated image paths.
    }
    @Test fun missingBilingualFieldsDuplicateItemsReferencesAndVisualMismatchReject() {
        val items=VocabularyContent.items
        val categories=listOf(VocabularyContent.repository.find(ContentId("category.animals")) as VocabularyCategory)
        assertThrows(IllegalArgumentException::class.java){LocalizedText("word","")}
        assertThrows(IllegalArgumentException::class.java){LocalizedText("","Wort")}
        assertThrows(IllegalArgumentException::class.java){VocabularyPack(items+items.first(),categories)}
        assertThrows(IllegalArgumentException::class.java){VocabularyPack(items,emptyList())}
        assertThrows(IllegalArgumentException::class.java){VocabularyPack(items.take(3),categories)}
        assertThrows(IllegalArgumentException::class.java){VocabularyPack(items,categories+categories)}
        assertThrows(IllegalArgumentException::class.java){items.first().copy(visual=TemporaryAnimalVisual.FISH)}
        assertThrows(IllegalArgumentException::class.java){VocabularySelection(ContentId("day.monday"))}
    }
    @Test fun bothMappingsAreDeterministicBoundedAndAvoidUnnecessaryRepetition() {
        for(phase in listOf(VocabularyPhase.FIND,VocabularyPhase.NAME)) for(round in RoundLength.entries) for(seed in 0L..20L) {
            fun generate()=(VocabularyContent.generate(phase,SessionId("words"),round,seed) as GenerationResult.Generated).plan
            val first=generate();val second=generate()
            val en=SessionReducer.start(first,ContentLanguage.ENGLISH,VocabularyContent.repository).state
            assertArrayEquals(SessionCheckpoint.encode(en),SessionCheckpoint.encode(SessionReducer.start(second,ContentLanguage.ENGLISH,VocabularyContent.repository).state))
            assertEquals(round.count,first.tasks.map {it.id}.distinct().size)
            assertEquals(minOf(round.count,6),first.tasks.take(6).map {it.question.correct}.distinct().size)
            first.tasks.forEach {task ->
                val q=task.question
                assertEquals(4,q.choices.distinct().size);assertEquals(1,q.choices.count {it==q.correct})
                q.choices.forEach {assertEquals(VocabularyContent.item(q.correct).category,VocabularyContent.item(it).category)}
                assertEquals(VocabularyContent.item(q.correct).text,q.hint)
                assertTrue(q.definition.value.endsWith(q.correct.value.substringAfter('.')))
                if(phase==VocabularyPhase.FIND)assertEquals(VocabularyContent.item(q.correct).findPrompt,q.instruction)
            }
            val de=SessionReducer.reduce(en,SessionAction.Language(ContentLanguage.GERMAN)).state
            assertSame(en.plan,de.plan);assertEquals(en.task.id,de.task.id)
            VocabularyContent.validate(phase,de)
        }
    }
    @Test fun invalidChoiceAndIncompatibleActivityContractsReject() {
        val ids=VocabularyContent.items.take(4).map {it.id}
        assertThrows(IllegalArgumentException::class.java){VocabularyContent.question(VocabularyPhase.FIND,ids[0],List(4){ids[0]})}
        assertThrows(IllegalArgumentException::class.java){VocabularyContent.question(VocabularyPhase.NAME,ids[0],ids.drop(1)+ContentId("day.monday"))}
        val plan=(VocabularyContent.generate(VocabularyPhase.FIND,SessionId("wrong-mode"),RoundLength.FIVE,1) as GenerationResult.Generated).plan
        val state=SessionReducer.start(plan,ContentLanguage.ENGLISH,VocabularyContent.repository).state
        assertThrows(IllegalArgumentException::class.java){VocabularyContent.validate(VocabularyPhase.NAME,state)}
    }
    @Test fun browsingCheckpointRestoresEverySelectionAndPhaseWithoutQuiz() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        assertEquals(VocabularySelection(),VocabularySelectionStore(disk).read())
        for(id in VocabularyContent.items.map {it.id}) for(phase in VocabularyPhase.entries) {
            val state=VocabularySelection(id,phase)
            VocabularySelectionStore(disk).write(state)
            assertEquals(state,VocabularySelectionStore(disk).read())
        }
    }
    @Test fun malformedOrIncompatibleBrowsingCheckpointIsPreserved() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        val store=VocabularySelectionStore(disk)
        store.write(VocabularySelection())
        val valid=disk.access {it.read()!!}
        for(bytes in listOf(byteArrayOf(1),valid+byteArrayOf(0),valid.copyOf().also {it[3]=2})) {
            disk.access {it.replace(bytes)}
            assertThrows(Exception::class.java) {store.read()}
            assertArrayEquals(bytes,disk.access {it.read()})
        }
    }
}
