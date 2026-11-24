package com.bellfamily.bastischool.ui.length

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.length.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*

@Composable
fun LengthComparisonScreen(vm: LengthComparisonViewModel, language: ContentLanguage, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}, onStopSound: () -> Unit = {}) {
    LengthComparisonContent(vm.state,language,vm.busy,vm.saveFailed,vm.audioFailed,vm::select,vm::move,vm::choose,
        vm::undo,vm::check,vm::next,{onStopSound();vm.replay()},vm::help,{onStopSound();vm.again()},vm::retrySave,onHome,modifier,onPop)
}

@Composable
internal fun LengthComparisonContent(state: LengthComparison.State?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean,
    audioFailed: Boolean, onSelect: (Strip) -> Unit, onMove: (Int) -> Unit, onChoose: (LengthRelation) -> Unit,
    onUndo: () -> Unit, onCheck: () -> Unit, onNext: () -> Unit, onReplay: () -> Unit, onHelp: () -> Unit,
    onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    val de = language == ContentLanguage.GERMAN
    fun t(en: String, german: String) = if(de)german else en
    val ready = state != null && state.language == language && !busy && !saveFailed
    if (state?.completed == true) {
        NativeCompletionScreen(state.id.value,language,LengthComparison.complete.display[language],ready,"length-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed)
        return
    }
    val work: @Composable (Modifier) -> Unit = { area ->
        key(state?.id?.value,state?.index) {
            Column(area.verticalScroll(rememberScrollState()).padding(vertical=4.dp).testTag("length-work"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                if(state == null) Text(t("Opening your strips…","Deine Streifen werden geöffnet…")) else {
                    NativeQuestionProgress(state.index+1,state.tasks.size,language)
                    Text(LengthComparison.question.display[language],Modifier.testTag("length-prompt"),style=MaterialTheme.typography.titleLarge)
                    NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("length-replay"),ready)
                    if(state.current.support.hint)NativeSupportMessage(LengthComparison.hint.display[language],Modifier.testTag("length-hint"))
                    if(state.current.solved) {
                        NativeSupportMessage(LengthComparison.feedback(state).display[language],Modifier.testTag("length-feedback"))
                        NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("length-next"),ready)
                    } else {
                        Text(state.selected?.let {
                            if(state.positions.offset(it)==0)t("Strip ${it.name} starts on the line.","Streifen ${it.name} beginnt an der Linie.")
                            else t("Strip ${it.name} selected. Move its start to the line.","Streifen ${it.name} ausgewählt. Schiebe seinen Anfang an die Linie.") }
                            ?:t("Tap strip A or B to select it. Then move its start to the line.","Tippe auf Streifen A oder B. Schiebe dann seinen Anfang an die Linie."),Modifier.testTag("length-move-instruction"))
                        val offset = state.selected?.let { state.positions.offset(it) }
                        NativeActionButton(t("To the starting line","Zur Startlinie"),NativeActionRole.SECONDARY,{onMove(0)},Modifier.fillMaxWidth().testTag("length-align"),ready && offset != null && offset > 0)
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            NativeActionButton(t("Move left","Nach links"),NativeActionRole.SECONDARY,{onMove((offset?:0)-1)},Modifier.weight(1f).testTag("length-left"),ready && offset != null && offset > 0)
                            NativeActionButton(t("Move right","Nach rechts"),NativeActionRole.SECONDARY,{onMove((offset?:0)+1)},Modifier.weight(1f).testTag("length-right"),ready && offset != null && offset < 3)
                        }
                        NativeActionButton(t("Undo last move","Letzten Schritt rückgängig machen"),NativeActionRole.SECONDARY,onUndo,Modifier.fillMaxWidth().testTag("length-undo"),ready && state.undo.isNotEmpty())
                        Text(if(state.positions.aligned)t("Both starts line up. Compare the other ends.","Beide Anfänge liegen an der Linie. Vergleiche die anderen Enden.")
                            else t("Line up both starts before checking.","Lege beide Anfänge an die Linie, bevor du prüfst."),Modifier.testTag("length-alignment"))
                        LengthRelation.entries.forEach { relation ->
                            NativeTextChoice((if(state.answer==relation)"✓ "else"")+relation.text.display[language],{onChoose(relation)},Modifier.fillMaxWidth().testTag("length-answer-${relation.name.lowercase()}").semantics { selected=state.answer==relation },ready)
                        }
                        Text(state.answer?.let { t("Your choice: ","Deine Antwort: ")+it.text.display[language] }?:t("Choose an answer, then check.","Wähle eine Antwort und prüfe."),Modifier.testTag("length-choice"))
                        if(state.needsCorrection)NativeSupportMessage(LengthComparison.feedback(state).display[language],Modifier.testTag("length-feedback"))
                        NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,onCheck,Modifier.fillMaxWidth().testTag("length-check"),ready && state.positions.aligned && state.answer!=null && !state.needsCorrection && state.current.attempts<10000)
                        NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,onHelp,Modifier.testTag("length-help"),ready)
                    }
                }
                if(saveFailed) {
                    NativeSupportMessage(t("Your saved strips are kept. Please try again.","Deine gespeicherten Streifen bleiben erhalten. Versuche es erneut."))
                    NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("length-retry"),!busy)
                }
                if(audioFailed)Text(t("Speech is unavailable. You can keep playing. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Du kannst weiterspielen. Prüfe installierte Offline-Stimmen."))
                NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome,Modifier.testTag("length-home"))
            }
        }
    }
    BoxWithConstraints(modifier.fillMaxSize().padding(12.dp).testTag("length-game")) {
        val wide = maxWidth >= 600.dp
        if(wide)Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(.4f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                NativeActivityTitle(LengthComparison.title.display[language])
                if(state!=null)StripBoard(state,language,ready,onSelect)
            }
            work(Modifier.weight(.6f).fillMaxHeight())
        } else Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            NativeActivityTitle(LengthComparison.title.display[language])
            if(state!=null)StripBoard(state,language,ready,onSelect)
            work(Modifier.weight(1f).fillMaxWidth())
        }
    }
}

/** Both rows use the same scale. Endpoints, never colour or width scaling per row, define length. */
@Composable
private fun StripBoard(state: LengthComparison.State, language: ContentLanguage, ready: Boolean, onSelect: (Strip) -> Unit) {
    val de=language==ContentLanguage.GERMAN
    val colours=listOf(Color(0xFF367C87),Color(0xFFB66531),Color(0xFF7260A8),Color(0xFF4B8051))
    Column(Modifier.fillMaxWidth().testTag("length-board"),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(if(de)"Startlinie"else"Starting line",style=MaterialTheme.typography.bodyMedium)
        Strip.entries.forEach { strip ->
            val selected=state.selected==strip
            val length=if(strip==Strip.A)state.task.a else state.task.b
            val position=state.positions.offset(strip)
            val description=if(de)"Streifen ${strip.name}. Länge: $length Vergleichseinheiten. "+(if(position==0)"Anfang an der Startlinie."else"Anfang $position ${if(position==1)"Schritt"else"Schritte"} rechts von der Startlinie.")
                else "Strip ${strip.name}. Length: $length comparison units. "+(if(position==0)"Start on the line."else"Start $position ${if(position==1)"step"else"steps"} to the right of the line.")
            Surface(onClick={onSelect(strip)},enabled=ready&&!state.current.solved,
                modifier=Modifier.fillMaxWidth().height(60.dp).testTag("length-strip-${strip.name.lowercase()}").semantics {
                    contentDescription=description;this.selected=selected
                    stateDescription=if(selected) {if(de)"Ausgewählt"else"Selected"} else {if(de)"Nicht ausgewählt"else"Not selected"}
                },shape=RoundedCornerShape(12.dp),color=if(selected)Color(0xFFE2F0E8)else Color(0xFFFFF8EA),
                border=BorderStroke(if(selected)3.dp else 1.dp,Color(0xFF425E60))) {
                Row(Modifier.fillMaxSize().padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(strip.name,Modifier.width(26.dp).clearAndSetSemantics{},style=MaterialTheme.typography.titleMedium)
                    Canvas(Modifier.weight(1f).fillMaxHeight().testTag("length-track-${strip.name.lowercase()}").clearAndSetSemantics{}) {
                        val margin=6.dp.toPx();val unit=(size.width-2*margin)/9f
                        val x=margin+position*unit
                        drawLine(Color(0xFF244951),Offset(margin,4.dp.toPx()),Offset(margin,size.height-4.dp.toPx()),3.dp.toPx())
                        val top=(size.height-26.dp.toPx())/2;val rect=Size(length*unit,26.dp.toPx())
                        drawRoundRect(colours[(state.task.palette+strip.ordinal)%colours.size],Offset(x,top),rect,CornerRadius(3.dp.toPx()))
                        drawRoundRect(Color(0xFF244951),Offset(x,top),rect,CornerRadius(3.dp.toPx()),style=Stroke(1.5.dp.toPx()))
                    }
                }
            }
        }
    }
}
