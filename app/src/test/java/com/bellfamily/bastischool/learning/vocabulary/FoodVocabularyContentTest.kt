package com.bellfamily.bastischool.learning.vocabulary

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.vocabulary.VocabularyArtwork
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FoodVocabularyContentTest {
    @get:Rule val temp=TemporaryFolder()

    @Test fun foodRoundsCoverWordsBeforeRepeatingAndRestoreWithoutChangingQuestions() {
        for(phase in listOf(VocabularyPhase.FIND,VocabularyPhase.NAME)) for(round in RoundLength.entries) for(seed in 0L..40L) {
            fun generatePlan()=(VocabularyContent.generate(phase,VocabularyContent.food,SessionId("food-round"),round,seed) as GenerationResult.Generated).plan
            val plan=generatePlan()
            val targets=plan.tasks.map {it.question.correct}
            assertEquals(minOf(8,round.count),targets.take(8).distinct().size)
            assertTrue(targets.zipWithNext().none {(a,b)->a==b})
            assertEquals(round.count,plan.tasks.map {it.question.correct to it.question.choices.toSet()}.distinct().size)
            plan.tasks.forEach { task ->
                assertEquals(4,task.question.choices.distinct().size)
                task.question.choices.forEach { assertEquals(VocabularyContent.food,VocabularyContent.item(it).category) }
                assertTrue(task.question.correct in task.question.choices)
            }
            val state=SessionReducer.start(plan,ContentLanguage.GERMAN,VocabularyContent.foodPack.repository).state
            val same=SessionReducer.start(generatePlan(),ContentLanguage.GERMAN,VocabularyContent.foodPack.repository).state
            assertArrayEquals(SessionCheckpoint.encode(state),SessionCheckpoint.encode(same))
            val restored=SessionCheckpoint.restore(SessionCheckpoint.encode(state),VocabularyContent.activity(phase,VocabularyContent.food),
                VocabularyContent.REVISION,VocabularyContent.foodPack.repository) as SessionRestoreResult.Restored
            VocabularyContent.validate(phase,VocabularyContent.food,restored.state)
            assertArrayEquals(SessionCheckpoint.encode(state),SessionCheckpoint.encode(restored.state))
            assertTrue(SessionCheckpoint.restore(SessionCheckpoint.encode(state),VocabularyContent.activity(phase,VocabularyContent.classroom),
                VocabularyContent.REVISION,VocabularyContent.classroomPack.repository) is SessionRestoreResult.Rejected)
        }
    }

    @Test fun eightBilingualWordsHaveMappedArtworkAndSeparateActivityIdentities() {
        val expected=mapOf("apple" to ("Apple" to "Apfel"),"banana" to ("Banana" to "Banane"),
            "bread" to ("Bread" to "Brot"),"water" to ("Water" to "Wasser"),"milk" to ("Milk" to "Milch"),
            "cheese" to ("Cheese" to "Käse"),"orange" to ("Orange" to "Orange"),"sandwich" to ("Sandwich" to "Sandwich"))
        assertEquals(expected.keys.map {ContentId("food.$it")}.toSet(),VocabularyContent.foodPack.items.map {it.id}.toSet())
        VocabularyContent.foodPack.items.forEach { item ->
            val names=expected.getValue(item.id.value.substringAfter('.'))
            assertEquals(names.first,item.text.display.en);assertEquals(names.second,item.text.display.de)
            assertEquals(item.artwork,VocabularyArtwork.paths[item.id])
            for(lang in ContentLanguage.entries) {
                assertTrue(item.example.speech[lang].isNotBlank());assertTrue(item.findPrompt.speech[lang].isNotBlank())
            }
        }
        for(phase in listOf(VocabularyPhase.FIND,VocabularyPhase.NAME)) {
            assertNotEquals(VocabularyContent.activity(phase),VocabularyContent.activity(phase,VocabularyContent.food))
            assertNotEquals(VocabularyContent.activity(phase,VocabularyContent.classroom),VocabularyContent.activity(phase,VocabularyContent.food))
        }
    }

    @Test fun browsingStillReadsLegacyAnimalsAndPersistsEveryFoodSelection() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        val legacy=ByteArrayOutputStream().also { bytes ->
            DataOutputStream(bytes).use {it.writeInt(1);it.writeUTF("animal.horse");it.writeUTF("EXPLORE")}
        }.toByteArray()
        disk.access {it.replace(legacy)}
        val store=VocabularySelectionStore(disk)
        assertEquals(VocabularySelection(ContentId("animal.horse")),store.read())
        for(item in VocabularyContent.foodPack.items) for(phase in VocabularyPhase.entries) {
            val selection=VocabularySelection(item.id,phase,VocabularyContent.food)
            store.write(selection);assertEquals(selection,store.read())
        }
        assertThrows(IllegalArgumentException::class.java) {VocabularySelection(ContentId("food.apple"))}
    }
}
