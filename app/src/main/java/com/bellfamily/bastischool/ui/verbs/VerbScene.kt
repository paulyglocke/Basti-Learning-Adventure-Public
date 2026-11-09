package com.bellfamily.bastischool.ui.verbs

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.bellfamily.bastischool.learning.verbs.*
import kotlin.math.*

/** Native rendering of the repaired authored scene; geometry uses the same 640×360 stage. */
internal class VerbScene(private val c:Canvas) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private fun colour(value:String){paint.color=Color.parseColor(value);paint.style=Paint.Style.FILL}
    private fun rect(x:Float,y:Float,w:Float,h:Float,color:String){colour(color);c.drawRect(x,y,x+w,y+h,paint)}
    private fun oval(x:Float,y:Float,rx:Float,ry:Float,color:String){colour(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,paint)}
    private fun line(x:Float,y:Float,ex:Float,ey:Float,color:String,width:Float=3f){colour(color);paint.strokeWidth=width;paint.strokeCap=Paint.Cap.ROUND;c.drawLine(x,y,ex,ey,paint)}
    private fun log(x:Float,y:Float,w:Float=85f){rect(x,y-8,w,16f,"#946540");oval(x,y,8f,8f,"#C6A478");oval(x+w,y,8f,8f,"#B18B5D");line(x+12,y-3,x+w-8,y-3,"#704A2C",2f)}
    private fun ball(x:Float,y:Float){oval(x,y,17f,17f,"#E3A945");line(x-12,y-12,x+12,y+12,"#FFF1B8")}
    private fun leaf(x:Float,y:Float){oval(x,y,15f,7f,"#5E9B50");line(x-12,y,x+13,y,"#376A38",1.5f)}
    private fun mouse(x:Float,y:Float){oval(x,y,17f,9f,"#89939E");oval(x+12,y-8,7f,7f,"#89939E");oval(x+12,y-8,4f,4f,"#E8B1B9");oval(x+20,y-1,2f,2f,"#26394A");line(x-15,y,x-40,y-12,"#89939E")}
    fun draw(lesson:VerbLesson,bitmap:Bitmap,width:Float,height:Float,fraction:Float,forcedFrame:Int?=null) {
        val m=lesson.motion;val p=fraction.coerceIn(0f,.9999f);val a=VerbTimeline.pose(m,p)
        val water=m.mode in VerbTimeline.waterModes;val night=m.mode in setOf("hang","perchSound")
        c.save();c.clipRect(0f,0f,width,height);rect(0f,0f,width,height,if(water)"#DDF0FA" else "#FFF5DF")
        val scale=min(width/640,height/360);c.translate((width-640*scale)/2,(height-360*scale)/2);c.scale(scale,scale)
        rect(0f,0f,640f,360f,if(night)"#243D63" else if(water)"#DDF0FA" else "#FFF5DF")
        if(water){rect(0f,165f,640f,195f,"#82C9E4");line(0f,165f,640f,165f,"#F6FDFF")}
        else if(m.mode !in setOf("fly","flap","flutter","buzz","hang","web","perchSound")){
            rect(0f,300f,640f,60f,"#DCE8BA");line(0f,300f,640f,300f,"#80975B",2f)
        }
        if(m.mode in setOf("fly","flap","flutter","buzz")){oval(110f,70f,45f,10f,"#FFFFFF");oval(520f,110f,45f,10f,"#FFFFFF")}
        when(m.mode) {
            "dive" -> {colour("#99A49D");c.drawPath(Path().apply {moveTo(95f,165f);lineTo(165f,140f);lineTo(215f,165f);lineTo(220f,205f);lineTo(90f,205f);close()},paint)}
            "hang","perchSound" -> {val y=if(m.mode=="hang")68f else 280f;line(130f,y,510f,y,"#76543F",14f);oval(550f,55f,20f,20f,"#FFF1BF")}
            "copy" -> line(210f,280f,430f,280f,"#956C48",10f)
            "climb","scratch" -> {val x=if(m.mode=="climb")430f else 420f;line(x,45f,x,318f,"#956C48",35f);if(m.mode=="climb"){line(x,95f,520f,65f,"#956C48",14f);oval(485f,45f,70f,25f,"#91B66A")}}
            "leaves" -> {line(390f,90f,570f,90f,"#906B46",8f);leaf(397f,90f);leaf(425f,78f);leaf(451f,96f)}
            "graze" -> repeat(9){i->line(385f+i*7,300f,381f+i*7,282f-(i%3)*4,"#5B994B")}
            "peck" -> repeat(3){i->oval(390f+i*9,296f,4f,2f,"#C99542")}
            "dig" -> {oval(420f,303f,38f,6+min(1f,p*2)*9,"#796046");repeat(6){i->val u=(p*3+i/6f)%1;oval(408-u*65,285-sin(u*PI).toFloat()*38,4f,3f,"#AA8661")}}
            "dam" -> {
                val built=(p/.84f).coerceIn(0f,1f);val level=270-45*built
                rect(365f,level,275f,306-level,"#9FD7E8");rect(310f,286f,55f,20f,"#9FD7E8")
                repeat(3){row->repeat(4){i->val x=450+i*48f-(p*110)%48;line(x,level+10+row*9,x+25,level+10+row*9,"#E2F7FC",2f)}}
                repeat(min(5,(p*6).toInt())){i->log(365f,286-i*16f,65f)}
            }
            "web" -> {
                line(170f,320f,195f,170f,"#9A7752",7f);line(195f,170f,232f,103f,"#9A7752",5f);line(195f,170f,232f,237f,"#9A7752",5f)
                line(470f,320f,445f,170f,"#9A7752",7f);line(445f,170f,408f,103f,"#9A7752",5f);line(445f,170f,408f,237f,"#9A7752",5f)
                val n=((p/.8f).coerceIn(0f,1f)*24).toInt()
                repeat(8){i->if(i<n)line(320f,170f,320+cos(i*PI/4).toFloat()*125,170+sin(i*PI/4).toFloat()*95,"#9BABB4",2f)}
                for(ring in 1..3)repeat(8){i->if(8+(ring-1)*5+i*.6<n){val r=ring/3f;line(320+cos(i*PI/4).toFloat()*125*r,170+sin(i*PI/4).toFloat()*95*r,320+cos((i+1)*PI/4).toFloat()*125*r,170+sin((i+1)*PI/4).toFloat()*95*r,"#9BABB4",1.5f)}}
            }
            "fetch" -> {if(p<.48f)ball(472f,283f);if(p>.93f)ball(105f,283f)}
            "chase" -> mouse(a.x+a.size*.52f,292f)
        }
        val frame=forcedFrame?:a.frame;val cw=bitmap.width/m.columns;val ch=bitmap.height/m.rows
        val top=if(m.mode=="hang")68-a.size*.02f else a.base-a.size*.85f
        c.save();c.translate(a.x,top);if(a.flip)c.scale(-1f,1f);colour("#FFFFFF")
        c.drawBitmap(bitmap,Rect(frame%m.columns*cw,frame/m.columns*ch,(frame%m.columns+1)*cw,(frame/m.columns+1)*ch),RectF(-a.size/2,0f,a.size/2,a.size),paint);c.restore()
        when(m.mode) {
            "ink" -> if(p>.27f){val u=((p-.27f)/.5f).coerceIn(0f,1f);repeat(5){i->oval(a.x+a.size*.25f+u*70+i*9,a.base-a.size*.30f+sin(i*2f)*u*24,8+u*(14+i*2),7+u*(12+i*2),"#D926333C")}}
            "fire" -> if(p>.25f&&p<.75f){val u=sin((p-.25f)/.5f*PI).toFloat();val x=if(a.frame==1)392f else 417f;val y=167f;colour("#EC9540");c.drawPath(Path().apply {moveTo(x,y);quadTo(x+70*u,y-40*u,x+115*u,y);quadTo(x+60*u,y+35*u,x,y);close()},paint);colour("#FFE5A0");c.drawPath(Path().apply {moveTo(x,y);lineTo(x+80*u,y);lineTo(x+25*u,y+12*u);close()},paint)}
            "fetch" -> if(p>=.48f&&p<.93f)ball(a.x-a.size*.32f,a.base-a.size*.50f)
            "web" -> line(a.x-a.size*.2f,a.base-a.size*.2f,320+cos(p*PI*2).toFloat()*90,170+sin(p*PI*2).toFloat()*70,"#8399A6",1.5f)
            "stomp" -> if(p>.48f&&p<.7f){val u=(p-.48f)/.22f;oval(340f,301f,10+u*28,3f,"#B9AA81")}
        }
        if(water)rect(0f,190f,640f,170f,"#2967BEDC")
        c.restore()
    }
}
