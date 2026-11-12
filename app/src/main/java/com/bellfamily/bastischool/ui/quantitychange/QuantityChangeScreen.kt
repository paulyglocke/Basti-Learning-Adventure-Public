package com.bellfamily.bastischool.ui.quantitychange

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
import com.bellfamily.bastischool.learning.quantitychange.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.vocabulary.VocabularyArtwork

@Composable
fun QuantityChangeChooser(language:ContentLanguage,onStart:(ChangeConfig)->Unit,modifier:Modifier=Modifier) {
    val de=language==ContentLanguage.GERMAN
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("quantity-change-chooser"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        NativeActivityTitle(QuantityChange.title.display[language])
        Text(if(de)"Verändere eine Tiergruppe um genau ein Tier."else"Change an animal group by exactly one animal.",style=MaterialTheme.typography.bodyLarge)
        ChangeMode.entries.forEach { mode ->
            Surface(shape=RoundedCornerShape(20.dp),color=Color(0xFFF0F5E8)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text(mode.title.display[language],style=MaterialTheme.typography.headlineSmall)
                    Row(Modifier.fillMaxWidth().height(88.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        repeat(if(mode==ChangeMode.MORE)3 else 2) { AnimalPicture(ContentId("animal.dinosaur"),Modifier.weight(1f).fillMaxHeight()) }
                    }
                    Text(if(de){if(mode==ChangeMode.MORE)"Lege ein Tier dazu. Wie viele sind es jetzt?"else"Nimm ein Tier weg. Wie viele bleiben übrig?"}
                        else{if(mode==ChangeMode.MORE)"Add one animal. How many now?"else"Take one animal away. How many are left?"})
                    ChangeRange.entries.forEach { range->
                        val config=ChangeConfig(mode,range)
                        NativeActionButton("${mode.title.display[language]} · ${if(de)"bis"else"up to"} ${range.limit}",NativeActionRole.PRIMARY,{onStart(config)},
                            Modifier.fillMaxWidth().testTag("quantity-change-start-${config.key}"))
                    }
                }
            }
        }
    }
}

@Composable
fun QuantityChangeScreen(vm:QuantityChangeViewModel,config:ChangeConfig,language:ContentLanguage,onLevels:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={},onStopSound:()->Unit={}) {
    QuantityChangeContent(vm.state,config,language,vm.busy,vm.saveFailed,vm.audioFailed,vm::toggle,vm::undo,vm::check,vm::next,
        {onStopSound();vm.replay()},vm::help,{onStopSound();vm.again()},vm::retrySave,onLevels,onHome,modifier,onPop)
}

@Composable
internal fun QuantityChangeContent(state:QuantityChange.State?,config:ChangeConfig,language:ContentLanguage,busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,
    onToggle:(Int)->Unit,onUndo:()->Unit,onCheck:()->Unit,onNext:()->Unit,onReplay:()->Unit,onHelp:()->Unit,onAgain:()->Unit,onRetry:()->Unit,
    onLevels:()->Unit,onHome:()->Unit,modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,deText:String)=if(de)deText else en
    val ready=state!=null&&state.config==config&&state.language==language&&!busy&&!saveFailed
    if(state?.completed==true&&state.config==config) {
        NativeCompletionScreen(state.id.value,language,QuantityChange.complete.display[language],ready,"quantity-change-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed) {
            NativeActionButton(t("Choose an activity","Aufgabe auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("quantity-change-levels"))
        }
        return
    }
    val work:@Composable (Modifier)->Unit={workModifier->
        key(state?.id?.value,state?.index,config) {
            Column(workModifier.verticalScroll(rememberScrollState()).padding(vertical=4.dp).testTag("quantity-change-work"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                if(state==null||state.config!=config)Text(t("Opening your group…","Deine Gruppe wird geöffnet…"))else {
                    NativeQuestionProgress(state.index+1,state.tasks.size,language)
                    Text(t(if(config.mode==ChangeMode.MORE)"Add one animal. How many now?"else"Take one animal away. How many now?",
                        if(config.mode==ChangeMode.MORE)"Lege ein Tier dazu. Wie viele sind es jetzt?"else"Nimm ein Tier weg. Wie viele sind es jetzt?"),style=MaterialTheme.typography.titleLarge)
                    NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("quantity-change-replay"),ready)
                    if(state.current.support.hint)NativeSupportMessage(QuantityChange.hint(config).display[language],Modifier.testTag("quantity-change-hint"))
                    Text(t("Your group","Deine Gruppe"),style=MaterialTheme.typography.titleMedium)
                    Column(Modifier.widthIn(max=680.dp).fillMaxWidth().testTag("quantity-change-board"),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        (0 until config.range.limit).toList().chunked(5).forEach { row->
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                row.forEach {slot->
                                    val occupied=state.mask and(1 shl slot)!=0
                                    val description=t("Space ${slot+1}: ${if(occupied)"animal"else"empty"}. ","Feld ${slot+1}: ${if(occupied)"Tier"else"leer"}. ")+
                                        if(state.current.solved)t("Group checked.","Gruppe geprüft.")else if(occupied)t("Tap to take it away.","Tippe, um es wegzunehmen.")else t("Tap to add an animal.","Tippe, um ein Tier dazuzulegen.")
                                    Surface(onClick={onToggle(slot)},enabled=ready&&!state.current.solved,
                                        modifier=Modifier.weight(1f).height(80.dp).testTag("quantity-change-slot-$slot").semantics{
                                            contentDescription=description;stateDescription=if(occupied)t("Occupied","Belegt")else t("Empty","Leer")
                                        },shape=RoundedCornerShape(12.dp),color=if(occupied)Color(0xFFE4F1DD)else Color(0xFFF5F9FC),border=BorderStroke(2.dp,Color(0xFF658F86))) {
                                        Box(Modifier.fillMaxSize().padding(5.dp),contentAlignment=Alignment.Center) {
                                            if(occupied)AnimalPicture(state.task.animal,Modifier.fillMaxSize())
                                            else Text("+",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.clearAndSetSemantics{})
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(state.count==0)Text(t("Empty group: zero animals","Leere Gruppe: null Tiere"),Modifier.testTag("quantity-change-zero"))
                    Text(t("Tap an empty space to add an animal. Tap an animal to take it away.","Tippe auf ein leeres Feld, um ein Tier dazuzulegen. Tippe auf ein Tier, um es wegzunehmen."))
                    if(state.current.solved) {
                        Text("${state.task.start} ${if(config.mode==ChangeMode.MORE)"+"else"−"} 1 = ${state.answer}",Modifier.testTag("quantity-change-equation"),style=MaterialTheme.typography.headlineLarge)
                        NativeSupportMessage(QuantityChange.feedback(state).display[language],Modifier.testTag("quantity-change-feedback"))
                        NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("quantity-change-next"),ready)
                    }else {
                        if(state.needsCorrection)NativeSupportMessage(QuantityChange.feedback(state).display[language],Modifier.testTag("quantity-change-feedback"))
                        NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,onCheck,Modifier.fillMaxWidth().testTag("quantity-change-check"),ready&&!state.needsCorrection&&state.current.attempts<10000)
                        NativeActionButton(t("Undo last move","Letzten Schritt rückgängig machen"),NativeActionRole.SECONDARY,onUndo,Modifier.fillMaxWidth().testTag("quantity-change-undo"),ready&&state.undoMasks.isNotEmpty())
                        NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,onHelp,Modifier.testTag("quantity-change-help"),ready)
                    }
                }
                if(saveFailed) {
                    NativeSupportMessage(t("Your saved group is kept. Please try again.","Deine gespeicherte Gruppe bleibt erhalten. Versuche es erneut."))
                    NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("quantity-change-retry"),!busy)
                }
                if(audioFailed)Text(t("Speech is unavailable. You can keep building. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Du kannst weiterbauen. Prüfe installierte Offline-Stimmen."))
                NativeActionButton(t("Choose an activity","Aufgabe auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("quantity-change-levels"))
                NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
            }
        }
    }
    BoxWithConstraints(modifier.fillMaxSize().padding(12.dp).testTag("quantity-change-game")) {
        val wide=maxWidth>=600.dp
        Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            NativeActivityTitle("${config.mode.title.display[language]} · ${if(de)"bis"else"up to"} ${config.range.limit}")
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
private fun StartingGroup(state:QuantityChange.State,language:ContentLanguage,modifier:Modifier) {
    val de=language==ContentLanguage.GERMAN
    BoxWithConstraints(modifier) {
        val rows=state.config.range.limit/5
        val font=LocalDensity.current.fontScale.coerceAtLeast(1f)
        val cell=if(maxHeight.value.isFinite())((maxHeight-32.dp*font-12.dp)/rows).coerceIn(48.dp,64.dp)else 56.dp
        Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("${if(de)"Am Anfang"else"At the start"}: ${state.task.start}",style=MaterialTheme.typography.bodyLarge)
            Column(Modifier.fillMaxWidth().testTag("quantity-change-starting-group").semantics(mergeDescendants=true){
                contentDescription=if(de)"Anfangsgruppe: ${state.task.start} Tiere. Bleibt als Beispiel sichtbar."else"Starting group: ${state.task.start} animals. Stays visible as an example."
            },verticalArrangement=Arrangement.spacedBy(6.dp)) {
                (0 until state.config.range.limit).toList().chunked(5).forEach { row->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                        row.forEach {slot->
                            Surface(Modifier.weight(1f).height(cell).testTag("quantity-change-given-$slot"),shape=RoundedCornerShape(10.dp),color=Color(0xFFFFF0CF),border=BorderStroke(1.dp,Color(0xFFA88955))) {
                                if(slot<state.task.start)AnimalPicture(state.task.animal,Modifier.fillMaxSize().padding(3.dp))
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
