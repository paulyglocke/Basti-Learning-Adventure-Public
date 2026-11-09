package com.bellfamily.bastischool.learning.verbs

import kotlin.math.*

data class ActionPose(val frame:Int,val x:Float=320f,val base:Float=300f,val size:Float=270f,val flip:Boolean=false)
object VerbTimeline {
    val waterModes=setOf("swim","dive","ink","float","paddle")
    fun smooth(value:Float):Float {val t=value.coerceIn(0f,1f);return t*t*(3-2*t)}
    fun stillTimes(m:VerbMotion):List<Float> = when(m.mode) {
        "snap" -> listOf(0f,600f/2800,1300f/2800,1640f/2800)
        "jump","hop" -> listOf(.14f,.28f,.51f,.74f)
        "fetch" -> listOf(.12f,.38f,.68f,.94f)
        else -> listOf(0f,.28f,.56f,.84f)
    }
    fun frame(m:VerbMotion,p:Float):Int {
        val n=when(m.mode) {
            "snap" -> return when {p<350f/2800->0;p<600f/2800->1;p<850f/2800->2;p<1100f/2800->3;p<1500f/2800->4;p<1540f/2800->5;p<1580f/2800->6;p<1780f/2800->7;else->0}
            "hop" -> when {p<.12f->0;p<.20f->1;p<.28f->2;p<.38f->3;p<.62f->4;p<.72f->5;p<.82f->6;else->7}
            "jump" -> when {p<.12f->0;p<.20f->1;p<.25f->2;p<.32f->3;p<.42f->4;p<.61f->5;p<.70f->6;p<.81f->7;else->0}
            "curl" -> if(p<.68f) min(6,(p/.68f*7).toInt()) else if(p<.9f) 6 else 7
            // Stabilise integer cycle boundaries against Float's 11.999999 at phase .2.
            "fetch" -> return when {p<.30f->m.frames[floor(p.toDouble()/.30*18+1e-6).toInt()%6];p<.4f->14;p<.48f->15;p<.88f->m.frames[floor((p.toDouble()-.48)/.4*18+1e-6).toInt()%6];p<.96f->14;else->15}
            "stomp" -> if(m.subject.value=="animal.dinosaur")when{p<.15f->0;p<.32f->1;p<.5f->2;else->3}
                else when {p<.2f->0;p<.5f->1;p<.7f->2;else->3}
            else -> min(m.frames.lastIndex,(p*m.frames.size).toInt())
        }
        return m.frames[n]
    }
    fun pose(m:VerbMotion,fraction:Float):ActionPose {
        val p=fraction.coerceIn(0f,.9999f);var x=320f;var base=300f;var size=270f;var flip=false
        when(m.mode) {
            "jump","hop" -> {size=220f;val u=((p-.20f)/.5f).coerceIn(0f,1f);base-=sin(PI*u).toFloat()*(if(m.mode=="hop")58 else 80)}
            "fly","flap","flutter","buzz" -> {size=260f;base=285f}
            in waterModes -> {
                size=240f;base=295f
                when(m.mode) {
                    "ink"->{size=205f;base=346f}
                    "dive"->{size=170f;x=180+170*((p-.15f)/.65f).coerceIn(0f,1f);base=165+125*((p-.2f)/.6f).coerceIn(0f,1f)}
                    "float"->{size=255f;base=175+sin(p*PI*2).toFloat()*3}
                    "paddle"->base=225f
                }
                if(m.mode in setOf("swim","paddle"))x=180+280*smooth(p)
            }
            "hang"->{size=215f;base=225f}
            "pouch"->size=330f
            "perchSound"->base=280f
            "pounce"->{size=250f;val u=((p-.2f)/.5f).coerceIn(0f,1f);x=200+240*smooth(u);base-=sin(u*PI).toFloat()*65}
            "climb"->{size=230f;x=355f;base=315-100*p}
            "dam"->{size=250f;x=285f}
            "web"->{size=190f;base=280f}
            "fetch"->{size=235f;when {p<.3f->x=170+280*smooth(p/.3f);p<.48f->x=450f;p<.88f->{x=450-280*smooth((p-.48f)/.4f);flip=true};else->{x=170f;flip=true}}}
            "run","chase","carry","crawl","waddle","sideways","slither" -> x=170+(if(m.mode=="chase")260 else 300)*smooth(p)
            "snap"->{val ms=p*2800;when {ms>=350&&ms<1500->x-=640*.014f*smooth((ms-350)/750);ms>=1500&&ms<1580->x=640*(.486f+.038f*smooth((ms-1500)/80));ms>=1580&&ms<2080->{val u=(ms-1580)/500;x=640*(.5f+.024f*exp(-10*u)*cos(2*PI*u).toFloat())}}}
        }
        return ActionPose(frame(m,p),x,base,size,flip)
    }
}
