package com.bellfamily.bastischool.ui.bonds

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.bonds.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*

@Composable
fun NumberBondsScreen(vm:NumberBondsViewModel,level:BondLevel,language:ContentLanguage,onHome:()->Unit,modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    NumberBondsContent(vm.state,level,language,vm.busy,vm.saveFailed,vm.audioFailed,
        vm::toggle,vm::check,vm::next,vm::replay,vm::help,vm::again,vm::retrySave,onHome,modifier,onPop)
}

@Composable
internal fun NumberBondsContent(state:NumberBonds.State?,level:BondLevel,language:ContentLanguage,
    busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,onToggle:(Int)->Unit,onCheck:()->Unit,onNext:()->Unit,
    onReplay:()->Unit,onHelp:()->Unit,onAgain:()->Unit,onRetry:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    val ready=state!=null&&state.language==language&&!busy&&!saveFailed
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    if(state?.completed==true){NativeCompletionScreen(state.id.value,language,NumberBonds.complete.display[language],ready,"number-bonds-${level.key}-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed);return}
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("number-bonds-${level.key}"),
        verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        NativeActivityTitle(NumberBonds.title(level).display[language])
        if(state==null)Text(t("Opening…","Wird geöffnet…"))else {
            NativeQuestionProgress(state.index+1,state.targets.size,language)
            Text(NumberBonds.prompt(state.shown,level).display[language],Modifier.testTag("number-bonds-prompt"),style=MaterialTheme.typography.headlineSmall)
            NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("number-bonds-replay"),ready)
            Text(t("Already here: ${state.shown}. You add the rest.","Schon da: ${state.shown}. Du legst den Rest dazu."))
            Column(Modifier.widthIn(max=680.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                (0 until level.total).toList().chunked(5).forEach {row ->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        row.forEach {slot ->
                            val fixed=slot<state.shown;val added=state.placedMask and (1 shl slot)!=0;val occupied=fixed||added
                            val description=when {fixed->t("Starting egg ${slot+1}. Stays in place.","Vorhandenes Ei ${slot+1}. Bleibt liegen.")
                                added->t("Added egg in nest ${slot+1}. Tap to take it away.","Dazugelegtes Ei im Nest ${slot+1}. Tippe, um es wegzunehmen.")
                                else->t("Empty nest ${slot+1}. Tap to add an egg.","Leeres Nest ${slot+1}. Tippe, um ein Ei dazuzulegen.")}
                            Surface(onClick={onToggle(slot)},enabled=ready&&!fixed&&!state.current.solved,
                                modifier=Modifier.weight(1f).heightIn(min=64.dp).aspectRatio(.85f).testTag("number-bonds-slot-$slot").semantics{contentDescription=description},
                                shape=RoundedCornerShape(12.dp),color=if(fixed)Color(0xFFFFEDC5)else Color(0xFFEAF4F0),border=BorderStroke(2.dp,if(fixed)Color(0xFFAD8046)else Color(0xFF568C82))) {
                                Canvas(Modifier.fillMaxSize().padding(5.dp)) {
                                    val w=size.width;val h=size.height
                                    drawOval(Color(0xFFBCA181),Offset(w*.05f,h*.72f),Size(w*.9f,h*.2f))
                                    drawOval(Color(0xFF80684C),Offset(w*.05f,h*.72f),Size(w*.9f,h*.2f),style=Stroke(2.dp.toPx()))
                                    if(occupied){
                                        drawOval(if(fixed)Color(0xFFF4D69A)else Color(0xFFB8DDD0),Offset(w*.18f,h*.08f),Size(w*.64f,h*.72f))
                                        drawOval(if(fixed)Color(0xFFAD8046)else Color(0xFF48786E),Offset(w*.18f,h*.08f),Size(w*.64f,h*.72f),style=Stroke(2.dp.toPx()))
                                        drawCircle(if(fixed)Color(0xFFC29456)else Color(0xFF669E8B),w*.055f,Offset(w*.42f,h*.30f))
                                        drawCircle(if(fixed)Color(0xFFC29456)else Color(0xFF669E8B),w*.07f,Offset(w*.59f,h*.53f))
                                    }else drawCircle(Color(0xFF568C82),w*.08f,Offset(w*.5f,h*.45f),style=Stroke(2.dp.toPx()))
                                }
                            }
                        }
                    }
                }
            }
            Text(NumberBonds.help.display[language],style=MaterialTheme.typography.bodyLarge)
            if(state.current.support.hint)NativeSupportMessage(t("Fill each empty nest. Count all the eggs together.","Fülle jedes leere Nest. Zähle alle Eier zusammen."))
            if(state.current.solved){
                Text("${state.shown} + ${state.addedCount} = ${level.total}",Modifier.testTag("number-bonds-equation"),style=MaterialTheme.typography.headlineLarge)
                NativeSupportMessage(NumberBonds.feedback(state).display[language])
                NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("number-bonds-next"),ready)
            }else {
                if(state.current.attempts>0)NativeSupportMessage(NumberBonds.feedback(state).display[language])
                NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,onCheck,Modifier.fillMaxWidth().testTag("number-bonds-check"),ready)
                NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,onHelp,Modifier.testTag("number-bonds-help"),ready)
            }
        }
        if(saveFailed){NativeSupportMessage(t("Your saved work is kept. Please try again.","Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."));NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,enabled=!busy)}
        if(audioFailed)Text(t("Speech is unavailable. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
    }
}
