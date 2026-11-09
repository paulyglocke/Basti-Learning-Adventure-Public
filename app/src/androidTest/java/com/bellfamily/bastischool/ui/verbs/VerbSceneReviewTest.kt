package com.bellfamily.bastischool.ui.verbs

import android.graphics.*
import androidx.test.platform.app.InstrumentationRegistry
import com.bellfamily.bastischool.saveNativeReviewBitmap
import com.bellfamily.bastischool.learning.verbs.*
import org.junit.Assert.*
import org.junit.Test

class VerbSceneReviewTest {
    @Test fun all53NativeStoryboardsRenderAndUnderwaterAnimalsRemainVisible() {
        val assets=InstrumentationRegistry.getInstrumentation().targetContext.assets
        VerbContent.lessons.forEachIndexed {index,lesson ->
            val bitmap=assets.open(lesson.motion.asset).use{requireNotNull(BitmapFactory.decodeStream(it))}
            val output=Bitmap.createBitmap(1280,800,Bitmap.Config.ARGB_8888)
            val canvas=Canvas(output);canvas.drawColor(Color.WHITE)
            repeat(4){i ->
                canvas.save();canvas.translate((i%2)*640f,(i/2)*400f)
                VerbScene(canvas).draw(lesson,bitmap,640f,360f,VerbTimeline.stillTimes(lesson.motion)[i],lesson.motion.still[i])
                val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.rgb(32,62,74);textSize=20f}
                canvas.drawText("${i+1}. ${lesson.stages[i].en}",12f,388f,paint)
                canvas.restore()
            }
            saveNativeReviewBitmap("storyboard-${index+1}",output)
            if(lesson.motion.mode in VerbTimeline.waterModes){
                val empty=Bitmap.createBitmap(bitmap.width,bitmap.height,Bitmap.Config.ARGB_8888)
                val scene=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888)
                val blank=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888)
                VerbScene(Canvas(scene)).draw(lesson,bitmap,640f,360f,.56f)
                VerbScene(Canvas(blank)).draw(lesson,empty,640f,360f,.56f)
                var different=0
                for(y in 170 until 360 step 2)for(x in 0 until 640 step 2)if(scene.getPixel(x,y)!=blank.getPixel(x,y))different++
                assertTrue("${lesson.label.display.en}: water must not hide the animal",different>50)
                empty.recycle();scene.recycle();blank.recycle()
            }
            // Drawing a stage may never erase its surrounding UI.
            assertEquals(Color.WHITE,output.getPixel(1270,790))
            bitmap.recycle();output.recycle()
        }
    }
}
