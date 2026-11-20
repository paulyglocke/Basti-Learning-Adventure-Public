package com.bellfamily.bastischool.ui.numberstories

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.bellfamily.bastischool.learning.numberstories.StoryScene

/** Language-neutral native scenery. All countable objects belong to the board. */
@Composable
internal fun StoryBackdrop(scene:StoryScene,modifier:Modifier=Modifier) {
    Canvas(modifier.clipToBounds()) {
        if(scene==StoryScene.POND) {
            drawRect(Color(0xFFDDECCC))
            drawOval(Color(0xFF83BFC6),Offset(size.width*.42f,-size.height*.1f),Size(size.width*.7f,size.height*.7f))
            drawOval(Color(0xFFCAE9DF),Offset(size.width*.5f,size.height*.02f),Size(size.width*.5f,size.height*.42f),style=Stroke(3f))
            drawOval(Color(0xFFB9D5A1),Offset(-size.width*.2f,size.height*.6f),Size(size.width*1.4f,size.height*.8f))
        }else {
            drawRect(Color(0xFFF7E5C7))
            val cell=24f*density
            for(x in 0..(size.width/cell).toInt())for(y in 0..(size.height/cell).toInt()) {
                if((x+y)%2==0)drawRect(Color(0xFFE4B39C),Offset(x*cell,y*cell),Size(cell,cell))
            }
            drawRect(Color(0xFFA16C58),style=Stroke(2f*density))
        }
    }
}
