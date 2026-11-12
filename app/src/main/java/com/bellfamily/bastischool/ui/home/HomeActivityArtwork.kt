package com.bellfamily.bastischool.ui.home

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.ui.common.BundledArtwork

/** Exercise identity, independent of the category banners used on the root screen. */
internal data class HomeActivityArt(val asset:String?=null,val diagram:String?=null)
internal object HomeActivityArtwork {
    val entries=mapOf(
        "quantityChange" to HomeActivityArt(diagram="one-step-quantity"),
        "patternBuilder" to HomeActivityArt(diagram="repeating-pattern"),
        "memoryPairs" to HomeActivityArt(diagram="memory-pairs"),
        "tellme" to HomeActivityArt(asset="SceneDescriptions/countryside_farm/canonical/farm_008_farmyard_friends.png"),
        "vocabulary" to HomeActivityArt(diagram="word-pictures"),
        "verbExplorer" to HomeActivityArt(asset="Animals/canonical/eagle.png"),
        "letters" to HomeActivityArt(diagram="initial-letter"),
        "count" to HomeActivityArt(diagram="numerals"),
        "time" to HomeActivityArt(asset="Seasons/blossoming_lakeside_spring_meadow.png"),
        "verbs" to HomeActivityArt(asset="Animals/canonical/frog.png"),
        "subitising" to HomeActivityArt(diagram="dot-group"),
        "quantityMatch" to HomeActivityArt(diagram="numeral-group"),
        "compareQuantity" to HomeActivityArt(diagram="compare-groups"),
        "numberOrder" to HomeActivityArt(diagram="number-order"),
        "clock" to HomeActivityArt(diagram="clock-face"),
        "addTogether" to HomeActivityArt(diagram="small-addition"),
        "addChallenge" to HomeActivityArt(diagram="bigger-addition"),
        "subtraction" to HomeActivityArt(diagram="take-away"),
        "mixedAddition" to HomeActivityArt(diagram="mixed-addition"),
        "makeFive" to HomeActivityArt(diagram="five-frame"),
        "buildFive" to HomeActivityArt(diagram="bond-five"),
        "buildTen" to HomeActivityArt(diagram="bond-ten"),
        "countingTray" to HomeActivityArt(diagram="dinosaur-tray"),
        "math" to HomeActivityArt(diagram="story-maths"),
        "positions" to HomeActivityArt(asset="Prepositions/scenes/scene_prepositions_dinosaur_on.png"),
        "mixed" to HomeActivityArt(asset="SceneDescriptions/jungle_rainforest/canonical/jungle_001_actions.png"),
        "washHands" to HomeActivityArt(asset="wash_hands/soap.jpg"),
        "exploreHabitats" to HomeActivityArt(asset="SceneDescriptions/ocean_underwater/canonical/ocean_001_reef_actions.png"),
        "matchHabitats" to HomeActivityArt(diagram="animal-home"),
        "animalGroups" to HomeActivityArt(diagram="land-water"),
        "colourSort" to HomeActivityArt(diagram="colour-groups"),
        "follow" to HomeActivityArt(asset="school_actions/book_desk.png")
    )
}

@Composable
internal fun HomeActivityThumbnail(id:String,modifier:Modifier=Modifier) {
    val art=HomeActivityArtwork.entries.getValue(id)
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF6E7))) {
        if(art.asset!=null) BundledArtwork(art.asset,Modifier.fillMaxSize(),256)
        else when(art.diagram) {
            "word-pictures" -> Row(Modifier.fillMaxSize()) {
                BundledArtwork("school_actions/board/object_book.png",Modifier.weight(1f).fillMaxHeight(),256)
                BundledArtwork("vocabulary/food/apple.jpg",Modifier.weight(1f).fillMaxHeight(),256)
            }
            "initial-letter" -> {
                BundledArtwork("Animals/canonical/dinosaur.png",Modifier.fillMaxSize().padding(top=20.dp),256)
                Diagram(art.diagram)
            }
            "dinosaur-tray" -> {
                BundledArtwork("Animals/canonical/dinosaur.png",Modifier.fillMaxSize().padding(bottom=20.dp),256)
                Diagram(art.diagram)
            }
            "animal-home" -> {
                BundledArtwork("Animals/canonical/whale.png",Modifier.fillMaxSize().padding(bottom=16.dp),256)
                Diagram(art.diagram)
            }
            "land-water" -> Row(Modifier.fillMaxSize()) {
                BundledArtwork("Animals/canonical/rabbit.png",Modifier.weight(1f).fillMaxHeight().background(Color(0xFFE4ECCC)),256)
                BundledArtwork("Animals/canonical/dolphin.png",Modifier.weight(1f).fillMaxHeight().background(Color(0xFFD3ECFA)),256)
            }
            else -> Diagram(requireNotNull(art.diagram))
        }
    }
}

private val artInk=Color(0xFF25455A)
private val artBlue=Color(0xFF3387B3)
private val artGreen=Color(0xFF43805D)
private fun DrawScope.label(value:String,x:Float,y:Float,height:Float=22f) {
    val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=artInk.toArgb();textSize=height;isFakeBoldText=true;textAlign=Paint.Align.CENTER}
    drawIntoCanvas {it.nativeCanvas.drawText(value,x,y,paint)}
}
private fun DrawScope.dots(count:Int,x:Float,y:Float,color:Color=artBlue) {
    repeat(count) {i->drawCircle(color,4.8f,Offset(x+(i%3)*13f,y+(i/3)*13f))}
}
@Composable
private fun Diagram(kind:String) {
    Canvas(Modifier.fillMaxSize()) {
        scale(size.width/100f,size.height/100f,Offset.Zero) {
            when(kind) {
                "bond-five", "bond-ten" -> {
                    val total=if(kind=="bond-five")5 else 10
                    label(if(total==5)"5" else "10",50f,27f,23f)
                    repeat(total){i->
                        val x=13f+(i%5)*17f;val y=if(total==5)55f else 43f+(i/5)*25f
                        drawOval(if(i<3)Color(0xFFF4D69A)else Color(0xFFB8DDD0),Offset(x,y),Size(10f,17f))
                        drawOval(artInk,Offset(x,y),Size(10f,17f),style=Stroke(1.2f))
                    }
                }

                "one-step-quantity" -> {
                    repeat(3) { i -> drawCircle(artBlue,7f,Offset(18f+i*30f,25f)) }
                    label("+1",50f,56f,18f)
                    repeat(4) { i -> drawCircle(artBlue,7f,Offset(12f+i*25f,78f)) }
                }
                "repeating-pattern" -> {
                    repeat(4) { i ->
                        val x=14f+i*24f
                        if(i==3){drawRect(artInk,Offset(x-9f,35f),Size(18f,28f),style=Stroke(1.4f));label("?",x,55f,20f)}
                        else if(i%2==0)drawCircle(artBlue,9f,Offset(x,49f))
                        else drawRect(artBlue,Offset(x-9f,40f),Size(18f,18f))
                    }
                }
                "memory-pairs" -> {
                    repeat(4) { i ->
                        val x=10f+(i%2)*44f;val y=9f+(i/2)*44f
                        drawRoundRect(if(i==0||i==3)Color(0xFFE4F1DD)else Color(0xFF245A67),Offset(x,y),Size(34f,38f),androidx.compose.ui.geometry.CornerRadius(5f))
                        if(i==0||i==3) dots(3,x+5f,y+10f,artGreen)
                        else drawCircle(Color(0xFFF4D79E),7f,Offset(x+17f,y+19f),style=Stroke(2f))
                    }
                }
                "numerals" -> {
                    repeat(3) {group->
                        val center=23f+group*27f
                        label("${group+1}",center,40f)
                        repeat(group+1) {dot->drawCircle(artBlue,3.5f,Offset(center+(dot-group/2f)*9f,65f))}
                    }
                }
                "dot-group" -> {dots(3,30f,36f);dots(2,30f,62f)}
                "numeral-group" -> {label("3",25f,57f,36f);dots(3,58f,39f);drawLine(artInk,Offset(49f,25f),Offset(49f,75f),2f)}
                "compare-groups" -> {dots(2,18f,38f);dots(4,61f,38f);drawLine(artInk,Offset(49f,23f),Offset(49f,76f),2f)}
                "number-order" -> {label("2",18f,57f,30f);label("?",50f,57f,30f);label("4",82f,57f,30f)}
                "clock-face" -> {
                    drawCircle(Color.White,40f,Offset(50f,50f));drawCircle(artInk,40f,Offset(50f,50f),style=Stroke(3f))
                    label("12",50f,27f,13f);label("3",79f,55f,13f);label("6",50f,84f,13f);label("9",21f,55f,13f)
                    drawLine(artBlue,Offset(50f,50f),Offset(50f,32f),4f);drawLine(artGreen,Offset(50f,50f),Offset(72f,50f),3f)
                }
                "small-addition" -> {dots(2,16f,34f);label("+",50f,48f);dots(2,72f,34f);label("2 + 2",50f,78f,18f)}
                "bigger-addition" -> {label("7 + 5",50f,38f,27f);repeat(12){i->drawCircle(if(i<7)artBlue else artGreen,3f,Offset(20f+(i%6)*12f,61f+(i/6)*12f))}}
                "take-away" -> {dots(4,31f,32f);drawLine(Color(0xFFA9514E),Offset(27f,41f),Offset(35f,49f),3f);label("4 − 1",50f,79f,23f)}
                "mixed-addition" -> {label("3 + 2",50f,37f,24f);label("7 + 5",50f,73f,24f)}
                "five-frame","dinosaur-tray" -> {repeat(5){i->drawRect(artInk,Offset(7f+i*18f,if(kind=="five-frame")36f else 76f),Size(15f,22f),style=Stroke(1.5f));if(i<3)drawCircle(artGreen,5f,Offset(14.5f+i*18f,if(kind=="five-frame")47f else 87f))}}
                "story-maths" -> {label("4 + 3",50f,36f,24f);label("8 − 2",50f,74f,24f)}
                "initial-letter" -> label("D",19f,31f,30f)
                "animal-home" -> {drawLine(artBlue,Offset(4f,85f),Offset(96f,85f),3f);drawLine(artBlue,Offset(8f,92f),Offset(92f,92f),2f)}
                "colour-groups" -> {listOf(Color(0xFFC14D49),artBlue,artGreen,Color(0xFFDCA62C)).forEachIndexed {i,c->val x=if(i%2==0)30f else 70f;val y=if(i<2)30f else 70f;drawCircle(c,12f,Offset(x,y));drawRect(c,Offset(x+9f,y+6f),Size(9f,9f))}}
            }
        }
    }
}
