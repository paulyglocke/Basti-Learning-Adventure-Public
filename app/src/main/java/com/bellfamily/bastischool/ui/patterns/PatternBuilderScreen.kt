package com.bellfamily.bastischool.ui.patterns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.patterns.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.vocabulary.VocabularyArtwork

@Composable
fun PatternChooser(language:ContentLanguage,onStart:(PatternLevel)->Unit,modifier:Modifier=Modifier) {
    val de=language==ContentLanguage.GERMAN
    val animals=listOf(ContentId("animal.dinosaur"),ContentId("animal.snake"),ContentId("animal.whale"))
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("pattern-chooser"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        NativeActivityTitle(PatternBuilder.title.display[language])
        Text(if(de)"Schau, was sich wiederholt. Lege die nächste Gruppe."else "Spot what repeats. Build the next group.",style=MaterialTheme.typography.bodyLarge)
        PatternLevel.entries.forEach { level ->
            Surface(shape=RoundedCornerShape(20.dp),color=Color(0xFFF0F5E8)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("${level.key.uppercase()} · ${level.title.display[language]}",style=MaterialTheme.typography.headlineSmall)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        level.rule.forEach { PatternPicture(animals[it],Modifier.weight(1f).height(88.dp)) }
                    }
                    Text(if(de)"Mit Tierbildern und Formen."else "With animal pictures and shapes.")
                    NativeActionButton("Start: ${level.title.display[language]}",NativeActionRole.PRIMARY,{onStart(level)},
                        Modifier.fillMaxWidth().testTag("pattern-start-${level.key}"))
                }
            }
        }
    }
}

@Composable
fun PatternBuilderScreen(vm:PatternBuilderViewModel,level:PatternLevel,language:ContentLanguage,onLevels:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={},onStopSound:()->Unit={}) {
    PatternBuilderContent(vm.state,level,language,vm.busy,vm.saveFailed,vm.audioFailed,vm::add,vm::remove,vm::undo,vm::check,vm::next,
        {onStopSound();vm.replay()},vm::listen,vm::help,{onStopSound();vm.again()},vm::retrySave,onLevels,onHome,modifier,onPop)
}

@Composable
internal fun PatternBuilderContent(state:PatternBuilder.State?,level:PatternLevel,language:ContentLanguage,busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,
    onAdd:(ContentId)->Unit,onRemove:(Int)->Unit,onUndo:()->Unit,onCheck:()->Unit,onNext:()->Unit,onReplay:()->Unit,onListen:(ContentId)->Unit,
    onHelp:()->Unit,onAgain:()->Unit,onRetry:()->Unit,onLevels:()->Unit,onHome:()->Unit,modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,deText:String)=if(de)deText else en
    val ready=state!=null&&state.level==level&&state.language==language&&!busy&&!saveFailed
    if(state?.completed==true&&state.level==level) {
        NativeCompletionScreen(state.id.value,language,PatternBuilder.complete.display[language],ready,"pattern-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed) {
            NativeActionButton(t("Choose a pattern","Muster auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("pattern-levels"))
        }
        return
    }
    val work: @Composable (Modifier)->Unit = { workModifier ->
        key(state?.id?.value,state?.index,level) {
            Column(workModifier.verticalScroll(rememberScrollState()).padding(vertical=4.dp).testTag("pattern-work"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("${level.key.uppercase()} · ${level.title.display[language]}",style=MaterialTheme.typography.titleLarge)
                if(state==null||state.level!=level)Text(t("Opening your pattern…","Dein Muster wird geöffnet…"))else {
                    NativeQuestionProgress(state.index+1,state.tasks.size,language)
                    Text(PatternBuilder.instruction.display[language])
                    NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("pattern-replay"),ready)
                    if(state.current.support.hint)NativeSupportMessage(PatternBuilder.hint(state.task).display[language],Modifier.testTag("pattern-hint"))
                    Text(t("Build the next group","Lege die nächste Gruppe"),style=MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        state.placed.forEachIndexed { index,id ->
                            val mismatched=state.needsCorrection&&state.firstMismatch==index
                            val description=if(id==null)t("Empty space ${index+1}","Leeres Feld ${index+1}")else
                                t("Space ${index+1}: ${PatternBuilder.label(id).display[language]}. Tap to remove.","Feld ${index+1}: ${PatternBuilder.label(id).display[language]}. Tippe zum Entfernen.")
                            Surface(onClick={onRemove(index)},enabled=ready&&!state.current.solved&&id!=null,
                                modifier=Modifier.weight(1f).height(104.dp).testTag("pattern-slot-$index").semantics{
                                    contentDescription=description
                                    stateDescription=when {state.current.solved->t("Pattern checked","Muster geprüft");mismatched->t("Have another look","Schau noch einmal hin");id==null->t("Empty","Leer");else->t("Placed","Gelegt")}
                                },shape=RoundedCornerShape(16.dp),color=if(state.current.solved)Color(0xFFE4F1DD)else Color(0xFFFFFBEE),
                                border=BorderStroke(if(mismatched||state.current.solved)3.dp else 2.dp,if(mismatched)Color(0xFFA47A2E)else Color(0xFF658F86))) {
                                Box(Modifier.fillMaxSize().padding(8.dp),contentAlignment=Alignment.Center) {
                                    if(id==null)Text("?",style=MaterialTheme.typography.headlineLarge,modifier=Modifier.clearAndSetSemantics {})
                                    else PatternPicture(id,Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                    Text(t("Tap a picture below to fill the next empty space. Tap a placed picture to remove it.",
                        "Tippe unten auf ein Bild, um das nächste leere Feld zu füllen. Tippe auf ein gelegtes Bild, um es zu entfernen."))
                    val fontScale=LocalDensity.current.fontScale.coerceAtLeast(1f)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        state.task.choices.forEach { id ->
                            val label=PatternBuilder.label(id).display[language]
                            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
                                Surface(onClick={onAdd(id)},enabled=ready&&!state.current.solved&&!state.full,
                                    modifier=Modifier.fillMaxWidth().height(124.dp*fontScale).testTag("pattern-choice-${id.value}").semantics{contentDescription=t("Add $label","$label dazulegen")},
                                    shape=RoundedCornerShape(16.dp),color=Color(0xFFEAF1F4),border=BorderStroke(2.dp,Color(0xFF819B9C))) {
                                    Column(Modifier.fillMaxSize().padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                        PatternPicture(id,Modifier.weight(1f).fillMaxWidth())
                                        Text(label,style=MaterialTheme.typography.titleMedium)
                                    }
                                }
                                NativeActionButton(t("Listen","Anhören"),NativeActionRole.SECONDARY,{onListen(id)},Modifier.fillMaxWidth().testTag("pattern-listen-${id.value}"),ready)
                            }
                        }
                    }
                    if(state.current.solved) {
                        NativeSupportMessage(PatternBuilder.correct.display[language],Modifier.testTag("pattern-feedback"))
                        NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("pattern-next"),ready)
                    }else {
                        if(state.needsCorrection)NativeSupportMessage(PatternBuilder.retry.display[language],Modifier.testTag("pattern-feedback"))
                        NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,onCheck,Modifier.fillMaxWidth().testTag("pattern-check"),ready&&state.full&&!state.needsCorrection)
                        NativeActionButton(t("Undo last picture","Letztes Bild zurücknehmen"),NativeActionRole.SECONDARY,onUndo,Modifier.fillMaxWidth().testTag("pattern-undo"),ready&&state.placed.any{it!=null})
                        NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,onHelp,Modifier.testTag("pattern-help"),ready)
                    }
                }
                if(saveFailed) {
                    NativeSupportMessage(t("Your saved pattern is kept. Please try again.","Dein gespeichertes Muster bleibt erhalten. Versuche es erneut."))
                    NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("pattern-retry"),!busy)
                }
                if(audioFailed)Text(t("Speech is unavailable. You can keep building. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Du kannst weiterbauen. Prüfe installierte Offline-Stimmen."))
                NativeActionButton(t("Choose a pattern","Muster auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("pattern-levels"))
                NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
            }
        }
    }
    BoxWithConstraints(modifier.fillMaxSize().padding(12.dp).testTag("pattern-game")) {
        val wide=maxWidth>=600.dp
        Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            NativeActivityTitle(PatternBuilder.title.display[language])
            if(wide&&state!=null&&state.level==level) {
                Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    PatternExample(state,language,false,Modifier.weight(.38f).fillMaxHeight())
                    work(Modifier.weight(.62f).fillMaxHeight())
                }
            }else {
                if(state!=null&&state.level==level)PatternExample(state,language,true,Modifier.fillMaxWidth())
                work(Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

/** A fixed, visible reference while the separate construction workspace scrolls. */
@Composable
private fun PatternExample(state:PatternBuilder.State,language:ContentLanguage,inline:Boolean,modifier:Modifier) {
    val unit=state.task.unit
    val group: @Composable (Int,Modifier)->Unit = { groupIndex,groupModifier ->
        Row(groupModifier.height(64.dp).testTag("pattern-example-$groupIndex").semantics(mergeDescendants=true){
            contentDescription=(if(language==ContentLanguage.GERMAN)"Beispielgruppe ${groupIndex+1}: "else "Example group ${groupIndex+1}: ")+unit.joinToString(", "){PatternBuilder.label(it).display[language]}
        },horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            unit.forEachIndexed { index,id ->
                Surface(Modifier.weight(1f).fillMaxHeight().testTag("pattern-given-$groupIndex-$index"),shape=RoundedCornerShape(12.dp),
                    color=if(state.current.support.hint&&groupIndex==0)Color(0xFFFFF0C9)else Color(0xFFEAF1F4),border=BorderStroke(1.dp,Color(0xFF819B9C))) {
                    PatternPicture(id,Modifier.fillMaxSize().padding(4.dp))
                }
            }
        }
    }
    Column(modifier,verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(if(language==ContentLanguage.GERMAN){if(inline)"Schau dir das Muster an"else"Beispiel"}else{if(inline)"Look at the pattern"else"Example"},style=MaterialTheme.typography.bodyMedium)
        if(inline)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            group(0,Modifier.weight(1f));group(1,Modifier.weight(1f))
        }else {group(0,Modifier.fillMaxWidth());group(1,Modifier.fillMaxWidth())}
    }
}

@Composable
internal fun PatternPicture(id:ContentId,modifier:Modifier=Modifier) {
    val shape=PatternShape.entries.firstOrNull{it.id==id}
    if(shape==null)BundledArtwork(VocabularyArtwork.paths.getValue(id),modifier,256)
    else Canvas(modifier.clearAndSetSemantics {}) {
        val side=minOf(size.width,size.height)*.72f
        val left=(size.width-side)/2;val top=(size.height-side)/2
        val fill=Color(0xFF5E9AB4);val ink=Color(0xFF284D62)
        when(shape) {
            PatternShape.CIRCLE->{drawCircle(fill,side/2,Offset(size.width/2,size.height/2));drawCircle(ink,side/2,Offset(size.width/2,size.height/2),style=Stroke(2.dp.toPx()))}
            PatternShape.SQUARE->{drawRect(fill,Offset(left,top),Size(side,side));drawRect(ink,Offset(left,top),Size(side,side),style=Stroke(2.dp.toPx()))}
            PatternShape.TRIANGLE->{val path=Path().apply{moveTo(size.width/2,top);lineTo(left+side,top+side);lineTo(left,top+side);close()};drawPath(path,fill);drawPath(path,ink,style=Stroke(2.dp.toPx()))}
        }
    }
}
