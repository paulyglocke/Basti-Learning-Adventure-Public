package com.bellfamily.bastischool.ui.verbmotion

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt

internal data class VerbAtlas(val image: ImageBitmap, val columns:Int, val rows:Int) {
    init {
        require(columns in 2..4 && rows in 2..4)
        require(image.width in columns..2048 && image.height in rows..2048)
        require(image.width%columns==0 && image.height%rows==0)
    }
    val cellWidth get() = image.width/columns
    val cellHeight get() = image.height/rows
}

@Composable
internal fun rememberVerbAtlas(sample: VerbMotionSample): State<Result<VerbAtlas>?> {
    val assets = LocalContext.current.applicationContext.assets
    val state = remember(sample) { mutableStateOf<Result<VerbAtlas>?>(null) }
    LaunchedEffect(assets, sample) {
        state.value = withContext(Dispatchers.IO) {
            runCatching {
                assets.open(sample.asset).use { stream ->
                    val bitmap = requireNotNull(BitmapFactory.decodeStream(stream))
                    require(bitmap.hasAlpha())
                    VerbAtlas(bitmap.asImageBitmap(),sample.atlasColumns,sample.atlasRows)
                }
            }
        }
    }
    return state
}

@Composable
internal fun VerbMotionStage(sample: VerbMotionSample, atlas: VerbAtlas, pose: VerbPose,
    description: String, modifier: Modifier = Modifier, still: Boolean = false, pictureIndex:Int=pose.frame) {
    require(pose.frame in 0 until atlas.columns*atlas.rows)
    Canvas(modifier.testTag(if(still) "verb-pose-$pictureIndex" else "verb-stage")
        .semantics { contentDescription = description }) {
        drawRect(if(sample == VerbMotionSample.FLY) Color(0xFFE2F1FA) else Color(0xFFFFF5DF))
        if (!still && sample == VerbMotionSample.FLY) {
            // Stationary air context; the wings, rather than moving scenery, carry the action.
            listOf(.14f to .20f, .78f to .28f).forEach { (x,y) ->
                drawOval(Color.White, Offset(size.width*x, size.height*y), Size(size.width*.15f, size.height*.07f))
            }
        }
        if (!still && sample != VerbMotionSample.FLY) {
            val ground = size.height * .86f
            drawRect(Color(0xFFDBE8BA), Offset(0f, ground), Size(size.width, size.height-ground))
            drawLine(Color(0xFF80975C), Offset(0f, ground), Offset(size.width, ground), 2f)
        }
        val aspect=atlas.cellWidth.toFloat()/atlas.cellHeight
        val width: Float
        val height: Float
        val left: Float
        val top: Float
        if (still) {
            width = min(size.width, size.height*aspect)
            height = width/aspect
            left = (size.width-width)/2
            top = (size.height-height)/2
        } else {
            width = when(sample) {
                VerbMotionSample.JUMP -> min(size.height*.65f*aspect, size.width*.54f)
                VerbMotionSample.FLY -> min(size.height*.90f*aspect, size.width*.78f)
                VerbMotionSample.SNAP -> min(size.height*1.16f*aspect, size.width*.88f)
            }
            height=width/aspect
            left = size.width*pose.x - width/2
            top = if(sample == VerbMotionSample.FLY) (size.height-height)/2 - pose.lift*size.height
                else size.height*.86f - height*.86f - pose.lift*size.height
        }
        drawImage(atlas.image,
            srcOffset=IntOffset((pose.frame%atlas.columns)*atlas.cellWidth,(pose.frame/atlas.columns)*atlas.cellHeight),
            srcSize=IntSize(atlas.cellWidth,atlas.cellHeight),
            dstOffset=IntOffset(left.roundToInt(),top.roundToInt()),
            dstSize=IntSize(width.roundToInt(),height.roundToInt()), alpha=pose.alpha)
    }
}
