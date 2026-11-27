package com.bellfamily.bastischool.ui.conservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.conservation.*
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.vocabulary.VocabularyArtwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ConservationChooser(language:ContentLanguage,onStart:(ConservationRange)->Unit,modifier:Modifier=Modifier) {
    val de=language==ContentLanguage.GERMAN
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("conservation-chooser"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        NativeActivityTitle(Conservation.title.display[language])
        Text(if(de)"Nah beieinander oder weit verteilt: Bleibt die Anzahl gleich?"else"Close together or spread out: is it still the same amount?",style=MaterialTheme.typography.titleLarge)
        Surface(shape=RoundedCornerShape(20.dp),color=Color(0xFFF0F5E8)) {
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(if(de)"Verschiebe Bilder. Vergleiche Vorher und Jetzt."else"Move pictures. Compare Before and Now.")
                Row(Modifier.fillMaxWidth().height(90.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    repeat(3){Picture(QuantityObject.DINOSAUR,Modifier.weight(1f).fillMaxHeight())}
                }
                Text(if(de)"Meist ändern sich nur die Plätze. Manchmal kommt eins dazu oder eins wird weggenommen."else"Usually only the places change. Sometimes one is added or taken away.")
                ConservationRange.entries.forEach { range->
                    NativeActionButton(if(de)"Bis ${range.limit}"else"Up to ${range.limit}",NativeActionRole.PRIMARY,{onStart(range)},Modifier.fillMaxWidth().testTag("conservation-start-${range.key}"))
                }
            }
        }
    }
}

@Composable
fun ConservationScreen(vm:ConservationViewModel,config:ConservationRange,language:ContentLanguage,onLevels:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={},onStopSound:()->Unit={}) {
    ConservationContent(vm.state,config,language,vm.busy,vm.saveFailed,vm.audioFailed,vm::tap,vm::choose,vm::undo,vm::check,vm::next,
        {onStopSound();vm.replay()},vm::help,{onStopSound();vm.again()},vm::retrySave,onLevels,onHome,modifier,onPop)
}

@Composable
internal fun ConservationContent(state:Conservation.State?,config:ConservationRange,language:ContentLanguage,busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,
    onTap:(Int)->Unit,onChoose:(QuantityRelation)->Unit,onUndo:()->Unit,onCheck:()->Unit,onNext:()->Unit,onReplay:()->Unit,onHelp:()->Unit,onAgain:()->Unit,onRetry:()->Unit,
    onLevels:()->Unit,onHome:()->Unit,modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,deText:String)=if(de)deText else en
    val ready=state!=null&&state.config==config&&state.language==language&&!busy&&!saveFailed
    if(state?.completed==true&&state.config==config) {
        NativeCompletionScreen(state.id.value,language,Conservation.complete.display[language],ready,"conservation-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed) {
            NativeActionButton(t("Choose a level","Stufe auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("conservation-levels"))
        }
        return
    }
    // Decode once per object, then render the very same bitmap in every occupied cell.
    // Do not expose an answerable board containing temporarily blank pictures.
    val assets=LocalContext.current.applicationContext.assets
    val path=state?.takeIf{it.config==config}?.task?.item?.id?.let{VocabularyArtwork.paths.getValue(it)}
    var picture by remember(assets,path){mutableStateOf<ImageBitmap?>(null)}
    var artFailed by remember(assets,path){mutableStateOf(false)}
    var artRequest by remember{mutableIntStateOf(0)}
    LaunchedEffect(assets,path,artRequest) {
        withContext(Dispatchers.Main.immediate) {picture=null;artFailed=false}
        if(path!=null) {
            loadArtworkForUi({BundledArtworkCache.load(assets,path,256)}) {
                picture=it;artFailed=it==null
            }
        }
    }
    if(state!=null&&state.config==config&&picture==null) {
        Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("conservation-art-loading"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            NativeActivityTitle(Conservation.title.display[language])
            Text(if(artFailed)t("The pictures could not open. Your group is saved. Try again.","Die Bilder konnten nicht geöffnet werden. Deine Gruppe ist gespeichert. Versuche es erneut.")else t("Opening your pictures…","Deine Bilder werden geöffnet…"))
            if(artFailed)NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,{artRequest++},Modifier.testTag("conservation-art-retry"))
            NativeActionButton(t("Choose a level","Stufe auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("conservation-levels"))
            NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
        }
        return
    }
    val work:@Composable (Modifier)->Unit={area->
        key(state?.id?.value,state?.index,config) {
            Column(area.verticalScroll(rememberScrollState()).padding(vertical=4.dp).testTag("conservation-work"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                if(state==null||state.config!=config)Text(t("Opening your group…","Deine Gruppe wird geöffnet…"))else {
                    NativeQuestionProgress(state.index+1,state.tasks.size,language)
                    Text(Conservation.change(state.task).display[language],Modifier.testTag("conservation-change"),style=MaterialTheme.typography.titleLarge)
                    Text(t("Now: fewer, the same amount, or more than before?","Jetzt: weniger, gleich viele oder mehr als vorher?"),Modifier.testTag("conservation-prompt"),style=MaterialTheme.typography.titleMedium)
                    NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("conservation-replay"),ready)
                    if(state.current.support.hint)NativeSupportMessage(Conservation.hint.display[language],Modifier.testTag("conservation-hint"))
                    Text(t("Now — move the pictures","Jetzt — verschiebe die Bilder"),style=MaterialTheme.typography.titleMedium)
                    Text(if(state.selected==null)t("Tap a picture, then an empty place. Nothing is added or removed when you move it.","Tippe auf ein Bild und dann auf einen freien Platz. Beim Verschieben kommt nichts dazu und nichts geht weg.")
                        else t("Picture in place ${state.selected+1} selected. Tap an empty place to move it, or tap it again to cancel.","Bild auf Platz ${state.selected+1} ausgewählt. Tippe auf einen freien Platz oder zum Abbrechen noch einmal auf das Bild."),Modifier.testTag("conservation-move-instruction"))
                    Column(Modifier.widthIn(max=680.dp).fillMaxWidth().testTag("conservation-board"),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        (0 until config.slots).toList().chunked(5).forEach { row->
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                row.forEach {slot->
                                    val occupied=state.mask and(1 shl slot)!=0
                                    val selected=state.selected==slot
                                    val description=t("Place ${slot+1}: ","Platz ${slot+1}: ")+
                                        if(occupied)state.task.item.noun(1,language)+". "+t("Select to move.","Zum Verschieben auswählen.")else t("Empty. Move the selected picture here.","Leer. Ausgewähltes Bild hierhin verschieben.")
                                    Surface(onClick={onTap(slot)},enabled=ready&&!state.current.solved&&(occupied||state.selected!=null),
                                        modifier=Modifier.weight(1f).height(72.dp).testTag("conservation-slot-$slot").semantics {
                                            contentDescription=description
                                            stateDescription=if(selected)t("Selected","Ausgewählt")else if(occupied)t("Occupied","Belegt")else t("Empty","Leer")
                                            this.selected=selected
                                        },shape=RoundedCornerShape(12.dp),color=if(selected)Color(0xFFDDEFE9)else Color(0xFFFFF7E8),border=BorderStroke(if(selected)3.dp else 1.dp,Color(0xFF658F86))) {
                                        Box(Modifier.fillMaxSize().padding(5.dp),contentAlignment=Alignment.Center) {
                                            if(occupied)Picture(state.task.item,Modifier.fillMaxSize(),picture)
                                            else Text("·",style=MaterialTheme.typography.headlineLarge,modifier=Modifier.clearAndSetSemantics{})
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(state.count==0)Text(t("Empty group: zero","Leere Gruppe: null"),Modifier.testTag("conservation-zero"))
                    if(state.current.solved) {
                        NativeSupportMessage(Conservation.feedback(state).display[language],Modifier.testTag("conservation-feedback"))
                        NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("conservation-next"),ready)
                    }else {
                        QuantityRelation.entries.forEach { relation->
                            NativeTextChoice((if(state.answer==relation)"✓ "else"")+relation.text.display[language],{onChoose(relation)},Modifier.fillMaxWidth().testTag("conservation-answer-${relation.name.lowercase()}").semantics{selected=state.answer==relation},ready)
                        }
                        Text(state.answer?.let{t("Your choice: ","Deine Antwort: ")+it.text.display[language]}?:t("Choose an answer, then check.","Wähle eine Antwort und prüfe."),Modifier.testTag("conservation-choice"))
                        if(state.needsCorrection)NativeSupportMessage(Conservation.feedback(state).display[language],Modifier.testTag("conservation-feedback"))
                        NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,onCheck,Modifier.fillMaxWidth().testTag("conservation-check"),ready&&state.answer!=null&&!state.needsCorrection&&state.current.attempts<10000)
                        NativeActionButton(t("Undo last move","Letzten Schritt rückgängig machen"),NativeActionRole.SECONDARY,onUndo,Modifier.fillMaxWidth().testTag("conservation-undo"),ready&&state.undoMasks.isNotEmpty())
                        NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,onHelp,Modifier.testTag("conservation-help"),ready)
                    }
                }
                if(saveFailed) {
                    NativeSupportMessage(t("Your saved group is kept. Please try again.","Deine gespeicherte Gruppe bleibt erhalten. Versuche es erneut."))
                    NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("conservation-retry"),!busy)
                }
                if(audioFailed)Text(t("Speech is unavailable. You can keep playing. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Du kannst weiterspielen. Prüfe installierte Offline-Stimmen."))
                NativeActionButton(t("Choose a level","Stufe auswählen"),NativeActionRole.NAVIGATION,onLevels,Modifier.testTag("conservation-levels"))
                NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
            }
        }
    }
    BoxWithConstraints(modifier.fillMaxSize().padding(12.dp).testTag("conservation-game")) {
        val wide=maxWidth>=600.dp
        Column(Modifier.fillMaxSize().testTag(if(picture!=null)"conservation-art-ready"else"conservation-art-pending"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("${Conservation.title.display[language]} · ${if(de)"bis"else"up to"} ${config.limit}",style=MaterialTheme.typography.titleLarge)
            if(wide&&state!=null&&state.config==config)Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                BeforeGroup(state,language,Modifier.weight(.38f).fillMaxHeight(),picture)
                work(Modifier.weight(.62f).fillMaxHeight())
            }else {
                if(state!=null&&state.config==config)BeforeGroup(state,language,Modifier.fillMaxWidth(),picture)
                work(Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

@Composable
private fun BeforeGroup(state:Conservation.State,language:ContentLanguage,modifier:Modifier,picture:ImageBitmap?) {
    val de=language==ContentLanguage.GERMAN
    BoxWithConstraints(modifier) {
        val font=LocalDensity.current.fontScale.coerceAtLeast(1f)
        val cell=if(maxHeight.value.isFinite())((maxHeight-32.dp*font-12.dp)/(state.config.limit/5)).coerceIn(40.dp,64.dp)else 48.dp
        Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("${if(de)"Vorher"else"Before"}: ${state.task.before}",Modifier.testTag("conservation-before-label"),style=MaterialTheme.typography.bodyLarge)
            Column(Modifier.fillMaxWidth().testTag("conservation-before").semantics(mergeDescendants=true){
                contentDescription=if(de)"Vorher: ${state.task.before} ${state.task.item.noun(state.task.before,language)}. Bleibt sichtbar."else"Before: ${state.task.before} ${state.task.item.noun(state.task.before,language)}. Stays visible."
            },verticalArrangement=Arrangement.spacedBy(6.dp)) {
                (0 until state.config.limit).toList().chunked(5).forEach { row->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                        row.forEach {slot->
                            Surface(Modifier.weight(1f).height(cell).testTag("conservation-given-$slot"),shape=RoundedCornerShape(10.dp),color=Color(0xFFE9F2DA),border=BorderStroke(1.dp,Color(0xFF819566))) {
                                if(slot<state.task.before)Picture(state.task.item,Modifier.fillMaxSize().padding(3.dp),picture)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Picture(item:QuantityObject,modifier:Modifier=Modifier,picture:ImageBitmap?=null) {
    if(picture!=null)Image(picture,null,modifier,contentScale=ContentScale.Fit)
    else BundledArtwork(VocabularyArtwork.paths.getValue(item.id),modifier,256)
}
