package com.bellfamily.bastischool.ui.verbmotion

import com.bellfamily.bastischool.learning.models.ContentLanguage
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class VerbMotionModelTest {
    @Test fun jumpHasPushOffTuckedAirbornePoseLandingAndVisibleRecovery() {
        val v=VerbMotionSample.JUMP
        assertEquals(0,VerbMotionTimeline.sample(v,0).frame)
        assertEquals(3,VerbMotionTimeline.sample(v,730).frame)
        val air=VerbMotionTimeline.sample(v,1300)
        assertEquals(5,air.frame);assertTrue(air.lift>.2f)
        val land=VerbMotionTimeline.sample(v,1900)
        assertEquals(7,land.frame);assertEquals(0f,land.lift,.0001f)
        assertEquals(land.x,VerbMotionTimeline.sample(v,0).x,0f)
        assertEquals(0,VerbMotionTimeline.sample(v,2500).frame)
        assertTrue((0L until v.cycleMillis).all {VerbMotionTimeline.sample(v,it).alpha==1f})
    }
    @Test fun wingsActuallyChange() {
        assertEquals((0..7).toSet(),(0L..1599L).map {VerbMotionTimeline.sample(VerbMotionSample.FLY,it).frame}.toSet())
    }
    @Test fun snapHasEightPosesAnticipationFastClosureAndDampedRecoil() {
        val v=VerbMotionSample.SNAP
        val samples=(0L until v.cycleMillis).map {VerbMotionTimeline.sample(v,it)}
        assertEquals((0..7).toSet(),samples.map {it.frame}.toSet())
        assertEquals(setOf(0f),samples.map {it.lift}.toSet())
        val opening=samples.count {it.frame in 1..3}
        val closing=samples.count {it.frame in 5..6}
        assertTrue(closing<opening/3)
        assertEquals(4,samples[1200].frame);assertEquals(7,samples[1580].frame)
        assertTrue(samples[1100].x<.5f);assertTrue(samples[1580].x>.52f)
        assertEquals(.5f,samples[2300].x,0f)
        assertTrue(samples.all {it.x in .48f.. .53f})
        assertEquals(listOf(0,2,4,7),v.pictureFrames)
    }
    @Test fun cyclesAreDeterministicBoundedAndExactlyRepeatable() {
        VerbMotionSample.entries.forEach {v->
            assertEquals("Every initial paused scene must show its animal",1f,VerbMotionTimeline.sample(v,0).alpha,0f)
            (0L until v.cycleMillis.toLong() step 7).forEach {t->
                val pose=VerbMotionTimeline.sample(v,t)
                assertEquals(pose,VerbMotionTimeline.sample(v,t+v.cycleMillis*1000L))
                assertTrue(pose.frame in 0 until v.frameCount);assertTrue(pose.alpha in 0f..1f)
                assertTrue(pose.x in .2f.. .8f);assertTrue(pose.lift in -.03f.. .36f)
            }
        }
    }
    @Test fun bilingualSamplesHaveFourCaptionedPosesAndSharedOfflineWebpAssets() {
        VerbMotionSample.entries.forEach {v->
            ContentLanguage.entries.forEach {lang->
                assertTrue(v.speech(lang).isNotBlank());assertEquals(4,v.stages.map {it[lang]}.distinct().size)
            }
            val bytes=File("src/main/assets/${v.asset}").readBytes()
            assertEquals("RIFF",String(bytes,0,4,Charsets.US_ASCII))
            assertEquals("WEBP",String(bytes,8,4,Charsets.US_ASCII))
            assertTrue(v.pictureFrames.all {it in 0 until v.frameCount})
        }
    }
}
