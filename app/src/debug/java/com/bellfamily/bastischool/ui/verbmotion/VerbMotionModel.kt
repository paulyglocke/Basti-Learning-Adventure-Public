package com.bellfamily.bastischool.ui.verbmotion

import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText
import com.bellfamily.bastischool.learning.models.LocalizedText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/** Review-only samples of the existing lessons, not a second production verb catalogue. */
internal enum class VerbMotionSample(
    val id: ContentId,
    val label: ContentText,
    val explanation: ContentText,
    val example: ContentText,
    val stages: List<LocalizedText>,
    val cycleMillis: Int
) {
    JUMP(ContentId("verb.jump"), ContentText.plain("Jump", "Springen"),
        ContentText.plain("Push your body up from the ground.", "Drücke dich vom Boden ab und bewege deinen Körper nach oben."),
        ContentText.plain("The rabbit pushes off, jumps through the air and lands.", "Das Kaninchen stößt sich ab, springt durch die Luft und landet."),
        listOf(LocalizedText("Crouch", "In die Hocke gehen"), LocalizedText("Push off", "Abstoßen"),
            LocalizedText("In the air", "In der Luft"), LocalizedText("Land", "Landen")), 2600),
    FLY(ContentId("verb.fly"), ContentText.plain("Fly", "Fliegen"),
        ContentText.plain("Move through the air.", "Bewege dich durch die Luft."),
        ContentText.plain("The eagle flaps its wings and flies through the air.", "Der Adler schlägt mit den Flügeln und fliegt durch die Luft."),
        listOf(LocalizedText("Wings up", "Flügel oben"), LocalizedText("Wings out", "Flügel ausgestreckt"),
            LocalizedText("Wings down", "Flügel unten"), LocalizedText("Wings lift again", "Flügel wieder anheben")), 1600),
    SNAP(ContentId("verb.snap_jaws"), ContentText.plain("Snap its jaws", "Mit dem Maul zuschnappen"),
        ContentText.plain("Close the mouth very quickly and strongly.", "Das Maul sehr schnell und kräftig zuschnappen."),
        ContentText.plain("The crocodile opens its mouth, then snaps it shut.", "Das Krokodil öffnet sein Maul und schnappt dann zu."),
        listOf(LocalizedText("Closed", "Geschlossen"), LocalizedText("Opening", "Öffnen"),
            LocalizedText("Wide open", "Weit geöffnet"), LocalizedText("Snap shut", "Zuschnappen")), 2800);

    val asset get() = "VerbMotion/${when(this){ JUMP->"rabbit"; FLY->"eagle"; SNAP->"croc" }}.webp"
    val atlasColumns get() = 4
    val atlasRows get() = if(this==JUMP) 4 else 2
    val frameCount get() = atlasColumns*atlasRows
    // Keep the four teaching steps concise; intermediate animation poses are not extra steps.
    val pictureFrames get() = when(this){ JUMP->listOf(1,3,5,7); FLY->listOf(0,3,5,7); SNAP->listOf(0,2,4,7) }
    fun speech(language: com.bellfamily.bastischool.learning.models.ContentLanguage) =
        "${label.speech[language]}. ${explanation.speech[language]} ${example.speech[language]}"
}

internal data class VerbPose(val frame: Int, val x: Float = .5f, val lift: Float = 0f, val alpha: Float = 1f)

/** Fixed key poses plus a smooth path. No random frames, morphing or per-frame generation. */
internal object VerbMotionTimeline {
    fun sample(verb: VerbMotionSample, millis: Long): VerbPose {
        require(millis >= 0)
        val t = (millis % verb.cycleMillis).toFloat()
        return when (verb) {
            VerbMotionSample.JUMP -> {
                val p=t/verb.cycleMillis
                val flight = ((p-.20f)/.5f).coerceIn(0f,1f)
                val frame = when { p<.12f->0; p<.20f->1; p<.25f->2; p<.32f->3;
                    p<.42f->4; p<.61f->5; p<.70f->6; p<.81f->7; else->0 }
                VerbPose(frame, .5f, .22f * sin(PI * flight).toFloat())
            }
            VerbMotionSample.FLY -> {
                val frame = (t/200).toInt().coerceIn(0,7)
                // Wing changes teach the movement. Keep the bird visible even before Play.
                VerbPose(frame)
            }
            VerbMotionSample.SNAP -> {
                val frame=when {
                    t<350 -> 0
                    t<600 -> 1 // Focus and anticipation before the opening.
                    t<850 -> 2
                    t<1100 -> 3
                    t<1500 -> 4 // Let the child see the wide-open jaw clearly.
                    t<1540 -> 5
                    t<1580 -> 6
                    t<1780 -> 7 // Firm closure: the entire closing stroke takes 80ms.
                    else -> 0
                }
                val x=when {
                    t<350 -> .5f
                    t<1500 -> .5f-.014f*smooth(((t-350)/750).coerceIn(0f,1f))
                    t<1580 -> .486f+.038f*smooth((t-1500)/80)
                    t<2080 -> {
                        val settle=(t-1580)/500
                        .5f+.024f*exp(-10f*settle)*cos(2f*PI.toFloat()*settle)
                    }
                    else -> .5f
                }
                VerbPose(frame,x)
            }
        }
    }
    private fun smooth(t:Float)=t*t*(3f-2f*t)
}
