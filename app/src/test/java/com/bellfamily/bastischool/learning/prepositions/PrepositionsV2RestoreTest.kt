package com.bellfamily.bastischool.learning.prepositions

import com.bellfamily.bastischool.audio.*
import com.bellfamily.bastischool.learning.content.BundledContentRepository
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Test

/** Independent revision-2 text fixture: deliberately does not call the current question builder. */
class PrepositionsV2RestoreTest {
    private val words = listOf(
        "on|auf|on the rock|auf dem Stein", "under|unter|under the table|unter dem Tisch",
        "behind|hinter|behind the rock|hinter dem Stein", "next_to|neben|next to the rock|neben dem Stein",
        "in|in|in the box|in der Kiste", "between|zwischen|between the two rocks|zwischen den beiden Steinen",
        "above|über|above the cloud|über der Wolke", "below|unterhalb|below the cloud|unterhalb der Wolke",
        "inside|drinnen|inside the cave|in der Höhle", "outside|draußen|outside the cave|außerhalb der Höhle",
        "in_front_of|vor|in front of the rock|vor dem Stein", "near|in der Nähe|near the rock|in der Nähe des Steins",
        "far_from|weit weg|far from the rock|weit weg vom Stein").map { it.split('|') }.associateBy { it[0] }
    private val content = BundledContentRepository(ContentVersion(1, 2), words.values.map {
        PrepositionDefinition(ContentId("position.${it[0]}"), ContentText.plain(it[0].replace('_', ' '), it[1]))
    }, emptyList())
    private fun plan(id: SessionId, offset: Int = 28): SessionPlan {
        val tasks = (0..4).map { i ->
            val scene = PrepositionsArtwork.scenes[(offset + i) % 52]
            val row = words.getValue(scene.relation.key)
            val chosen = mutableListOf(row[0])
            val excluded = listOf(setOf("in", "inside"), setOf("under", "below"),
                setOf("on", "above"), setOf("next_to", "near"))
            // Intentionally includes outside/front on cave scenes, which v2 allowed.
            for (key in listOf("outside", "in_front_of") + words.keys) {
                if (chosen.size < 4 && chosen.all { it != key && setOf(it, key) !in excluded }) chosen += key
            }
            val choices = chosen.map { ContentId("position.$it") }
            val names = choices.map { content.find(it)!!.text }
            val subject = scene.animal.subject
            ChoiceTask(TaskInstanceId(id, i + 1), ChoiceQuestion(scene.taskId,
                SkillId("skill.spatial.${row[0]}"), scene.context, 1,
                ContentText.plain("Where is ${subject.en}? Look at the picture. Choose: ${names.joinToString(", ") { it.speech.en }}.",
                    "Wo ist ${subject.de}? Schau dir das Bild an. Wähle: ${names.joinToString(", ") { it.speech.de }}."),
                choices, ContentId("position.${row[0]}"),
                ContentText.plain("Great! ${subject.en.replaceFirstChar { it.uppercase() }} is ${row[2]}.",
                    "Super! ${subject.de.replaceFirstChar { it.uppercase() }} ist ${row[3]}."),
                ContentText.plain("Try again!", "Nochmal versuchen!"), ContentText.plain(row[2], row[3])))
        }
        return SessionPlan(id, PrepositionsContent.activity, 2, content.version, SessionPolicy(RoundLength.FIVE),
            tasks, ContentText.plain("Adventure complete! Well done!", "Abenteuer geschafft! Sehr gut!"))
    }

    @Test fun all52V2DefinitionsRestoreByteExactlyAndRejectChangedAuthoredText() {
        for (offset in 0..51) {
            val plan = plan(SessionId("v2-$offset"), offset)
            val state = SessionReducer.start(plan, ContentLanguage.GERMAN, content).state
            val bytes = SessionCheckpoint.encode(state)
            val restored = PrepositionsContent.restore(bytes) as SessionRestoreResult.Restored
            assertArrayEquals(bytes, SessionCheckpoint.encode(restored.state))
        }
        val old = plan(SessionId("tampered"))
        val q = old.tasks[0].question
        // Valid encoding with new words falsely claiming to be a v2 question must fail validation.
        val changed = ChoiceQuestion(q.definition, q.skill, q.context, q.difficulty, q.instruction,
            q.choices, q.correct, q.correctFeedback, q.wrongFeedback, ContentText.plain("below the cloud", "unter der Wolke"))
        val forged = SessionPlan(old.id, old.activity, 2, old.contentVersion, old.policy,
            old.tasks.mapIndexed { i, task -> if (i == 0) ChoiceTask(task.id, changed) else task }, old.completionText)
        val bytes = SessionCheckpoint.encode(SessionReducer.start(forged, ContentLanguage.GERMAN, content).state)
        assertEquals(SessionRestoreResult.Rejected(CheckpointRejection.MALFORMED), PrepositionsContent.restore(bytes))
    }

    private class Memory : ProgressStorage {
        var bytes: ByteArray? = null
        override fun <T> access(block: (ProgressTransaction) -> T) = block(object : ProgressTransaction {
            override fun read() = bytes?.clone()
            override fun replace(bytes: ByteArray) { this@Memory.bytes = bytes.clone() }
        })
    }
    private var fail = false
    private val records = mutableListOf<ProgressEvent>()
    private val progress = object : ProgressRepository {
        override fun append(event: ProgressEvent): ProgressWriteResult {
            if (event !in records) records += event
            if (fail) return ProgressWriteResult.Failed(ProgressFailure.IO) // ambiguous persisted write
            return ProgressWriteResult.Saved(StoredProgressEvent(records.indexOf(event) + 1L, 0, event), true)
        }
        override fun read(query: ProgressQuery) = ProgressReadResult.Events(emptyList())
    }
    private fun restored(disk: Memory) = PrepositionsHost(disk, progress).also {
        assertTrue(it.open(SessionId("ignored"), RoundLength.TEN, 99, ContentLanguage.ENGLISH).isEmpty())
    }
    private fun answer(host: DurableSessionHost, correct: Boolean) {
        val s = host.state!!
        host.dispatch(SessionAction.Answer(s.nextAttempt!!,
            if (correct) s.task.question.correct else s.task.question.choices.first { it != s.task.question.correct }))
    }

    @Test fun oldActiveCompletedAndPendingJournalsRetainStateAndAgainStartsReviewedV3() {
        val disk = Memory()
        var host: DurableSessionHost = DurableSessionHost(disk, progress, PrepositionsContent.activity, 2, content,
            { id, _, _ -> GenerationResult.Generated(plan(id)) }, {})
        host.open(SessionId("old-round"), RoundLength.FIVE, 0, ContentLanguage.ENGLISH)
        host.dispatch(SessionAction.Hint(host.state!!.task.id))
        host.dispatch(SessionAction.Replay(host.state!!.task.id))
        host.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        fail = true; answer(host, false)
        val snapshot = SessionCheckpoint.encode(host.state!!)
        val pending = disk.bytes!!.clone()
        host = restored(disk) // delivery still failing: checkpoint AND journal stay exact
        assertArrayEquals(pending, disk.bytes)
        assertArrayEquals(snapshot, SessionCheckpoint.encode(host.state!!))
        fail = false; host.retryWrites()
        assertEquals(1, records.filterIsInstance<AttemptEvent>().size)
        host.dispatch(SessionAction.Retry(AttemptId(host.state!!.task.id, 1)))
        assertEquals(1, host.state!!.current.attempts)
        val retry = SessionCheckpoint.encode(host.state!!)
        host = restored(disk)
        assertArrayEquals(retry, SessionCheckpoint.encode(host.state!!))
        assertTrue(host.state!!.current.support.hint); assertEquals(1, host.state!!.current.support.replays)
        repeat(5) { i ->
            answer(host, true)
            if (i == 4) fail = true
            host.dispatch(SessionAction.Next(host.state!!.task.id))
        }
        val complete = SessionCheckpoint.encode(host.state!!)
        host = restored(disk)
        assertArrayEquals(complete, SessionCheckpoint.encode(host.state!!))
        fail = false; host.retryWrites()
        assertEquals(1, records.filterIsInstance<CompletionEvent>().size)
        val acknowledged = SessionCheckpoint.encode(host.state!!)
        host = restored(disk)
        assertArrayEquals(acknowledged, SessionCheckpoint.encode(host.state!!))
        assertEquals(2, host.state!!.plan.activityRevision)
        host.newRound(SessionId("reviewed-round"), RoundLength.FIVE, 8, ContentLanguage.GERMAN)
        assertEquals(3, host.state!!.plan.activityRevision)
        assertEquals(ContentVersion(1, 3), host.state!!.plan.contentVersion)
        assertEquals(SessionPhase.ACTIVE, host.state!!.phase)
        assertEquals(0, host.state!!.current.attempts)
        PrepositionsContent.validate(host.state!!)
    }

    @Test fun restoredOldGuidedListenUsesOldPhrasesAndCurrentRoundsUseNewPhrases() {
        val old = SessionReducer.start(plan(SessionId("old-listen")), ContentLanguage.GERMAN, content).state
        val state = (PrepositionsContent.restore(SessionCheckpoint.encode(old)) as SessionRestoreResult.Restored).state
        val scene = PrepositionsContent.scene(state.task)
        assertEquals(PositionRelation.BELOW, scene.relation)
        assertEquals("unterhalb der Wolke", answerPhrase(scene, scene.relation.id, state.language, 2))
        assertEquals("unter der Wolke", answerPhrase(scene, scene.relation.id, state.language, 3))
        assertTrue(positionDescription(scene, 2).de.contains("unterhalb der Wolke"))
        assertFalse(positionDescription(scene, 3).de.contains("unterhalb"))
        val id = SessionId("new-listen")
        val scenes = PrepositionsContent.scenes.filter { it.relation == PositionRelation.BELOW } +
            PrepositionsContent.scenes.first { it.relation == PositionRelation.ABOVE }
        val choices = listOf(PositionRelation.BELOW, PositionRelation.ABOVE, PositionRelation.BEHIND, PositionRelation.IN_FRONT_OF).map { it.id }
        val reviewedPlan = SessionPlan(id, PrepositionsContent.activity, 3, PrepositionsContent.version,
            SessionPolicy(RoundLength.FIVE), scenes.mapIndexed { i, s ->
                ChoiceTask(TaskInstanceId(id, i + 1), PrepositionsContent.question(s, choices))
            }, PrepositionsContent.completion)
        val reviewed = SessionReducer.start(reviewedPlan, ContentLanguage.GERMAN, PrepositionsContent.repository).state
        for (s in listOf(state, reviewed)) {
            val engine = FakeSpeechEngine()
            val audio = PrepositionsAudio(DefaultAudioController(engine, AudioMode.QUESTIONS))
            audio.visible(true); assertTrue(engine.spoken.isEmpty())
            val replay = SessionReducer.reduce(s, SessionAction.Replay(s.task.id))
            audio.effects(replay.state, replay.effects)
            repeat(6) { engine.emit(engine.spoken.last().id) }
            assertEquals(listOf("Wo ist der Vogel?", "Der Vogel ist…").map(::sanitizeSpeech) +
                s.task.question.choices.map { answerPhrase(scene, it, s.language, s.plan.activityRevision) + "." }, engine.spoken.map { it.text })
            assertEquals(6, engine.spoken.size)
            assertTrue(engine.spoken.all { it.context.language == ContentLanguage.GERMAN })
            assertEquals(if (s.plan.activityRevision == 2) "unterhalb der Wolke." else "unter der Wolke.", engine.spoken[2].text)
            audio.close()
        }
    }
}
