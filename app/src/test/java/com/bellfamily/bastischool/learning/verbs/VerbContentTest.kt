package com.bellfamily.bastischool.learning.verbs

import com.bellfamily.bastischool.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class VerbContentTest {
    @get:Rule val temp=TemporaryFolder()
    @Test fun nativePathsAndPhysicalFramesMatchAllRetainedReviewedWebSamples() {
        val rows=File("../content/verb-motion-parity.tsv").readLines().drop(1)
        assertEquals(53*13,rows.size)
        rows.forEach {line->
            val cells=line.split('\t');val lesson=VerbContent.byId.getValue(cells[0]);val pose=VerbTimeline.pose(lesson.motion,cells[1].toFloat())
            assertEquals(line,cells[2].toInt(),pose.frame)
            assertEquals(line,cells[3].toFloat(),pose.x,.01f)
            assertEquals(line,cells[4].toFloat(),pose.base,.01f)
            assertEquals(line,cells[5].toFloat(),pose.size,.01f)
            assertEquals(line,cells[6].toBoolean(),pose.flip)
        }
    }
    @Test fun catalogueAndSourcePosesAreCompleteAndOffline() {
        assertEquals(53,VerbContent.lessons.size);assertEquals(53,VerbContent.byId.size)
        assertEquals(39,VerbContent.lessons.map{it.animal}.distinct().size)
        VerbContent.lessons.forEach {v->
            assertTrue(File("src/main/assets",v.motion.asset).isFile)
            assertEquals(4,v.stages.size)
            assertEquals(setOf(v.animal)+v.distractors,(listOf(v.animal)+v.distractors).toSet())
            assertEquals(4,(listOf(v.animal)+v.distractors).distinct().size)
            (listOf(v.animal)+v.distractors).forEach {assertTrue(File("src/main/assets",VerbContent.animals.getValue(it).artwork).isFile)}
            for(p in 0..1000){val pose=VerbTimeline.pose(v.motion,p/1000f);assertTrue(pose.frame in 0 until v.motion.columns*v.motion.rows);assertTrue(pose.x.isFinite()&&pose.base.isFinite())}
        }
    }
    @Test fun seededRoundsNeverRepeatQuestionOrTargetAnimalAndReachEveryLesson() {
        val seen=mutableSetOf<String>()
        for(round in RoundLength.entries)for(seed in 0L..250){
            val plan=(VerbContent.generate(SessionId("seeded"),round,seed) as GenerationResult.Generated).plan
            val again=(VerbContent.generate(SessionId("seeded"),round,seed) as GenerationResult.Generated).plan
            assertEquals(plan.tasks.map{it.question.definition},again.tasks.map{it.question.definition})
            assertEquals(plan.tasks.map{it.question.choices},again.tasks.map{it.question.choices})
            assertEquals(round.count,plan.tasks.map{VerbContent.lesson(it.question).animal}.distinct().size)
            seen+=plan.tasks.map{VerbContent.lesson(it.question).id.value}
            VerbContent.validate(SessionReducer.start(plan,ContentLanguage.ENGLISH,VerbContent.repository).state)
        }
        assertEquals(VerbContent.byId.keys,seen)
    }
    @Test fun lessonSupportLanguageAndCorrectLockSurviveDurableRecoveryWithoutDuplicateProgress() {
        val disk=AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit)
        val progress=FileProgressRepository(AtomicProgressStorage(temp.newFolder(),JvmAtomicCommit),ProgressClock{1})
        var host=VerbContent.host(disk,progress)
        host.open(SessionId("verb-round"),RoundLength.FIVE,42,ContentLanguage.ENGLISH)
        val task=host.state!!.task
        host.dispatch(SessionAction.Hint(task.id))
        host.dispatch(SessionAction.Language(ContentLanguage.GERMAN))
        host=VerbContent.host(disk,progress)
        assertTrue(host.open(SessionId("ignored"),RoundLength.TEN,99,ContentLanguage.ENGLISH).isEmpty())
        assertEquals(task.id,host.state!!.task.id);assertEquals(task.question.choices,host.state!!.task.question.choices)
        assertEquals(ContentLanguage.GERMAN,host.state!!.language);assertTrue(host.state!!.current.support.hint)
        val answer=SessionAction.Answer(host.state!!.nextAttempt!!,task.question.correct)
        host.dispatch(answer);host.dispatch(answer)
        host=VerbContent.host(disk,progress);host.open(SessionId("ignored-again"),RoundLength.FIVE,13,ContentLanguage.ENGLISH)
        assertTrue(host.state!!.current.locked);assertEquals(1,host.state!!.score)
        assertEquals(1,(progress.read() as ProgressReadResult.Events).records.size)
        repeat(5) {
            val s=host.state!!
            if(!s.current.locked)host.dispatch(SessionAction.Answer(s.nextAttempt!!,s.task.question.correct))
            host.dispatch(SessionAction.Next(s.task.id))
        }
        assertEquals(SessionPhase.COMPLETED,host.state!!.phase)
        assertEquals(6,(progress.read() as ProgressReadResult.Events).records.size)
    }
    @Test fun nativeLessonAndOptionsReturnToSameOwnerAndLegacyBundlesRemainReadable() {
        for(root in listOf(ShellNavigation().openVerbLibrary(),ShellNavigation().openVerbQuiz())){
            val lesson=root.openVerbLesson("verb.jump")
            assertEquals(root,lesson.returnFromVerb());assertEquals(BackAction.VERB_RETURN,lesson.backAction)
            val options=lesson.openOptions()
            val restored=ShellNavigation.restore(options.screen.name,options.optionsOrigin.name,options.mode,null,options.homePage,options.verbId)
            assertEquals(lesson,restored.closeOptions());assertFalse(lesson.ownsWebSession)
        }
        val legacy=ShellNavigation.restore("OPTIONS","WEB","verbs","{}")
        assertTrue(legacy.ownsWebSession);assertEquals(ShellScreen.WEB,legacy.closeOptions().screen)
        assertTrue(ShellNavigation.restore("VERB_LESSON",null,"verbs",null,verbId="verb.missing").recoveryFailed)
    }
    @Test fun meaningfulSceneAnchorsAndExpressiveTimingsStayVisible() {
        val bat=VerbContent.byId.getValue("verb.sleep_upside_down").motion
        val dive=VerbContent.byId.getValue("verb.dive_underwater").motion
        assertTrue(68-VerbTimeline.pose(bat,.56f).size*.02f>=0)
        val underwater=VerbTimeline.pose(dive,.999f)
        assertTrue(underwater.base-underwater.size*.85f>165f-25) // animal baseline enters the water
        val snap=VerbContent.byId.getValue("verb.snap_its_jaws").motion
        assertEquals(4,VerbTimeline.frame(snap,1400f/2800));assertEquals(7,VerbTimeline.frame(snap,1600f/2800))
    }
}
