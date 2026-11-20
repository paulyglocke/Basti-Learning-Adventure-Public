package com.bellfamily.bastischool.ui.numberstories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.numberstories.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.vocabulary.VocabularyArtwork

@Composable
fun NumberStoriesChooser(language:ContentLanguage,onStart:(StoryConfig)->Unit,modifier:Modifier=Modifier) {
    val de=language==ContentLanguage.GERMAN
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("number-stories-chooser"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        NativeActivityTitle(NumberStories.title.display[language])
        Text(if(de)"Höre eine Geschichte. Baue die neue Gruppe."else"Listen to a story. Build the new group.",style=MaterialTheme.typography.bodyLarge)
        StoryMode.entries.forEach { mode ->
            Surface(shape=RoundedCornerShape(20.dp),color=Color(0xFFF0F5E8)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text(mode.title.display[language],style=MaterialTheme.typography.headlineSmall)
                    Row(Modifier.fillMaxWidth().height(100.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        StoryScene.entries.forEach { scene ->
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                StoryBackdrop(scene,Modifier.fillMaxSize())
                                AnimalPicture(scene.objectId,Modifier.fillMaxSize().padding(18.dp))
                            }
                        }
                    }
                    Text(if(de){if(mode==StoryMode.ADD)"Dinosaurier kommen dazu. Äpfel werden dazugelegt."else"Dinosaurier gehen weg. Äpfel werden weggenommen."}
                        else{if(mode==StoryMode.ADD)"Dinosaurs arrive. Apples are added."else"Dinosaurs leave. Apples are taken away."})
                    StoryRange.entries.forEach { range->
                        val config=StoryConfig(mode,range)
                        NativeActionButton("${mode.title.display[language]} · ${if(de)"bis"else"up to"} ${range.limit}",NativeActionRole.PRIMARY,{onStart(config)},
                            Modifier.fillMaxWidth().testTag("number-stories-start-${config.key}"))
                    }
                }
            }
        }
    }
}

@Composable
fun NumberStoriesScreen(vm:NumberStoriesViewModel,config:StoryConfig,language:ContentLanguage,onLevels:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={},onStopSound:()->Unit={}) {
    NumberStoriesContent(vm.state,config,language,vm.busy,vm.saveFailed,vm.audioFailed,vm::toggle,vm::undo,vm::check,vm::next,
        {onStopSound();vm.replay()},vm::help,{onStopSound();vm.again()},vm::retrySave,onLevels,onHome,modifier,onPop)
}

@Composable
internal fun NumberStoriesContent(state:NumberStories.State?,config:StoryConfig,language:ContentLanguage,busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,
    onToggle:(Int)->Unit,onUndo:()->Unit,onCheck:()->Unit,onNext:()->Unit,onReplay:()->Unit,onHelp:()->Unit,onAgain:()->Unit,onRetry:()->Unit,
    onLevels:()->Unit,onHome:()->Unit,modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,deText:String)=if(de)deText else en
    val ready=state!=null&&state.config==config&&state.language==language&&!busy&&!saveFailed
    if(state?.completed==true&&state.config==config) {
        NativeCompletionScreen(state.id.value,language,NumberStories.complete.display[language],ready,"number-stories-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed) {
            NativeActionButton(t("Choose an activity","Aufgabe auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("number-stories-levels"))
        }
        return
    }
    val work:@Composable (Modifier)->Unit={workModifier->
        key(state?.id?.value,state?.index,config) {
            Column(workModifier.verticalScroll(rememberScrollState()).padding(vertical=4.dp).testTag("number-stories-work"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                if(state==null||state.config!=config)Text(t("Opening your group…","Deine Gruppe wird geöffnet…"))else {
                    NativeQuestionProgress(state.index+1,state.tasks.size,language)
                    Text(NumberStories.story(state.task,config).display[language],Modifier.testTag("number-stories-story"),style=MaterialTheme.typography.titleLarge)
                    Text(t("How many now? Change your group, then check.","Wie viele sind es jetzt? Verändere deine Gruppe und prüfe."))
                    NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("number-stories-replay"),ready)
                    if(state.current.support.hint)NativeSupportMessage(NumberStories.hint(state.task,config).display[language],Modifier.testTag("number-stories-hint"))
                    Text(t("Your group","Deine Gruppe"),style=MaterialTheme.typography.titleMedium)
                    Box(Modifier.widthIn(max=680.dp).fillMaxWidth()) {
                        StoryBackdrop(state.task.scene,Modifier.matchParentSize())
                        Column(Modifier.fillMaxWidth().padding(8.dp).testTag("number-stories-board"),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(8.dp),contentAlignment=Alignment.CenterStart) {
                                Surface(shape=RoundedCornerShape(12.dp),color=Color(0xFFFFFCF3)) {
                                    Text(state.task.scene.title.display[language],Modifier.padding(8.dp),style=MaterialTheme.typography.titleMedium)
                                }
                            }
                        (0 until config.range.limit).toList().chunked(5).forEach { row->
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                row.forEach {slot->
                                    val occupied=state.mask and(1 shl slot)!=0
                                    val description=t("Space ${slot+1}: ${if(occupied)state.task.scene.noun(1,language)else"empty"}. ","Feld ${slot+1}: ${if(occupied)state.task.scene.noun(1,language)else"leer"}. ")+
                                        if(state.current.solved)t("Group checked.","Gruppe geprüft.")else if(occupied)t("Tap to take it away.","Tippe, um es wegzunehmen.")else t("Tap to add one.","Tippe, um eins dazuzulegen.")
                                    Surface(onClick={onToggle(slot)},enabled=ready&&!state.current.solved,
                                        modifier=Modifier.weight(1f).height(80.dp).testTag("number-stories-slot-$slot").semantics{
                                            contentDescription=description;stateDescription=if(occupied)t("Occupied","Belegt")else t("Empty","Leer")
                                        },shape=RoundedCornerShape(12.dp),color=if(occupied)Color(0xFFFFF3DB)else Color(0xFFFFFCF3),border=BorderStroke(2.dp,Color(0xFF658F86))) {
                                        Box(Modifier.fillMaxSize().padding(5.dp),contentAlignment=Alignment.Center) {
                                            if(occupied)AnimalPicture(state.task.scene.objectId,Modifier.fillMaxSize())
                                            else Text("+",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.clearAndSetSemantics{})
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                    if(state.count==0)Text(t("Empty group: zero","Leere Gruppe: null"),Modifier.testTag("number-stories-zero"))
                    Text(t("Tap an empty space to add one. Tap a picture to take it away.","Tippe auf ein leeres Feld, um eins dazuzulegen. Tippe auf ein Bild, um es wegzunehmen."))
                    if(state.current.solved) {
                        Text("${state.task.start} ${if(config.mode==StoryMode.ADD)"+"else"−"} ${state.task.amount} = ${state.answer}",Modifier.testTag("number-stories-equation"),style=MaterialTheme.typography.headlineLarge)
                        NativeSupportMessage(NumberStories.feedback(state).display[language],Modifier.testTag("number-stories-feedback"))
                        NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("number-stories-next"),ready)
                    }else {
                        if(state.needsCorrection)NativeSupportMessage(NumberStories.feedback(state).display[language],Modifier.testTag("number-stories-feedback"))
                        NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,onCheck,Modifier.fillMaxWidth().testTag("number-stories-check"),ready&&!state.needsCorrection&&state.current.attempts<10000)
                        NativeActionButton(t("Undo last move","Letzten Schritt rückgängig machen"),NativeActionRole.SECONDARY,onUndo,Modifier.fillMaxWidth().testTag("number-stories-undo"),ready&&state.undoMasks.isNotEmpty())
                        NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,onHelp,Modifier.testTag("number-stories-help"),ready)
                    }
                }
                if(saveFailed) {
                    NativeSupportMessage(t("Your saved group is kept. Please try again.","Deine gespeicherte Gruppe bleibt erhalten. Versuche es erneut."))
                    NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("number-stories-retry"),!busy)
                }
                if(audioFailed)Text(t("Speech is unavailable. You can keep building. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Du kannst weiterbauen. Prüfe installierte Offline-Stimmen."))
                NativeActionButton(t("Choose an activity","Aufgabe auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("number-stories-levels"))
                NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
            }
        }
    }
    BoxWithConstraints(modifier.fillMaxSize().padding(12.dp).testTag("number-stories-game")) {
        val wide=maxWidth>=600.dp
        Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("${NumberStories.title.display[language]} · ${if(de)"bis"else"up to"} ${config.range.limit}",style=MaterialTheme.typography.titleLarge)
            if(wide&&state!=null&&state.config==config)Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                StartingGroup(state,language,Modifier.weight(.38f).fillMaxHeight())
                work(Modifier.weight(.62f).fillMaxHeight())
            }else {
                if(state!=null&&state.config==config)StartingGroup(state,language,Modifier.fillMaxWidth())
                work(Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

/** Stable initial quantity stays visible; it is never an editable piece or a submitted answer. */
@Composable
private fun StartingGroup(state:NumberStories.State,language:ContentLanguage,modifier:Modifier) {
    val de=language==ContentLanguage.GERMAN
    BoxWithConstraints(modifier) {
        val rows=state.config.range.limit/5
        val font=LocalDensity.current.fontScale.coerceAtLeast(1f)
        val cell=if(maxHeight.value.isFinite())((maxHeight-32.dp*font-12.dp)/rows).coerceIn(48.dp,64.dp)else 56.dp
        Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("${if(de)"Am Anfang"else"At the start"}: ${state.task.start} · ${NumberStories.change(state.task,state.config).display[language]}",Modifier.testTag("number-stories-change"),style=MaterialTheme.typography.bodyLarge)
            Column(Modifier.fillMaxWidth().testTag("number-stories-starting-group").semantics(mergeDescendants=true){
                contentDescription=if(de)"Anfangsgruppe: ${state.task.start} ${state.task.scene.noun(state.task.start,language)}. Bleibt als Beispiel sichtbar."else"Starting group: ${state.task.start} ${state.task.scene.noun(state.task.start,language)}. Stays visible as an example."
            },verticalArrangement=Arrangement.spacedBy(6.dp)) {
                (0 until state.config.range.limit).toList().chunked(5).forEach { row->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                        row.forEach {slot->
                            Surface(Modifier.weight(1f).height(cell).testTag("number-stories-given-$slot"),shape=RoundedCornerShape(10.dp),color=Color(0xFFFFF0CF),border=BorderStroke(1.dp,Color(0xFFA88955))) {
                                if(slot<state.task.start)AnimalPicture(state.task.scene.objectId,Modifier.fillMaxSize().padding(3.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimalPicture(animal:ContentId,modifier:Modifier=Modifier)=BundledArtwork(VocabularyArtwork.paths.getValue(animal),modifier,256)
