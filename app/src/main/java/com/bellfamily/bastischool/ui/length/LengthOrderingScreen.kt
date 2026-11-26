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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.length.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun LengthOrderingScreen(vm: LengthOrderingViewModel, language: ContentLanguage, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}, onStopSound: () -> Unit = {}) {
    LengthOrderingContent(vm.state,language,vm.busy,vm.saveFailed,vm.audioFailed,vm::place,vm::remove,vm::undo,
        vm::check,vm::next,{onStopSound();vm.replay()},vm::help,{onStopSound();vm.again()},vm::retrySave,onHome,modifier,onPop)
}

@Composable
internal fun LengthOrderingContent(state: LengthOrdering.State?, language: ContentLanguage, busy: Boolean, saveFailed: Boolean,
    audioFailed: Boolean, onPlace: (ContentId) -> Unit, onRemove: (ContentId) -> Unit, onUndo: () -> Unit,
    onCheck: () -> Unit, onNext: () -> Unit, onReplay: () -> Unit, onHelp: () -> Unit,
    onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en: String, german: String)=if(de)german else en
    val ready=state!=null && state.language==language && !busy && !saveFailed
    if(state?.completed==true) {
        NativeCompletionScreen(state.id.value,language,LengthOrdering.complete.display[language],ready,"length-order-",onReplay,onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed)
        return
    }
    BoxWithConstraints(modifier.fillMaxSize().padding(12.dp).testTag("length-order-game")) {
        val wide=maxWidth>=600.dp
        val work: @Composable (Modifier) -> Unit = { area ->
            key(state?.id?.value,state?.index) {
                Column(area.verticalScroll(rememberScrollState()).testTag("length-order-work"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    if(wide)NativeActivityTitle(LengthOrdering.title.display[language])
                    if(state==null)Text(t("Opening your strips…","Deine Streifen werden geöffnet…")) else {
                        NativeQuestionProgress(state.index+1,state.tasks.size,language)
                        Text(LengthOrdering.question.display[language],Modifier.testTag("length-order-prompt"),style=MaterialTheme.typography.titleLarge)
                        NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("length-order-replay"),ready)
                        Text(t("Tap a strip on the left to add it on the right. Tap a placed strip to put it back.","Tippe links auf einen Streifen, um ihn rechts einzuordnen. Tippe auf einen eingeordneten Streifen, um ihn zurückzulegen."))
                        if(state.current.support.hint)NativeSupportMessage(LengthOrdering.hint.display[language],Modifier.testTag("length-order-hint"))
                        if(state.solved || state.needsCorrection)NativeSupportMessage(LengthOrdering.feedback(state).display[language],Modifier.testTag("length-order-feedback"))
                        if(state.solved)NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("length-order-next"),ready)
                        else {
                            NativeActionButton(t("Undo last move","Letzten Schritt rückgängig machen"),NativeActionRole.SECONDARY,onUndo,Modifier.fillMaxWidth().testTag("length-order-undo"),ready&&state.undo.isNotEmpty())
                            NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,onCheck,Modifier.fillMaxWidth().testTag("length-order-check"),ready&&state.canCheck)
                            NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,onHelp,Modifier.testTag("length-order-help"),ready)
                        }
                    }
                    if(saveFailed) {
                        NativeSupportMessage(t("Your saved order is kept. Please try again.","Deine gespeicherte Reihenfolge bleibt erhalten. Versuche es erneut."))
                        NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("length-order-retry"),!busy)
                    }
                    if(audioFailed)Text(t("Speech is unavailable. You can keep playing. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Du kannst weiterspielen. Prüfe installierte Offline-Stimmen."))
                    NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome,Modifier.testTag("length-order-home"))
                }
            }
        }
        if(wide)Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
            if(state!=null)OrderBoard(state,language,ready,onPlace,onRemove,Modifier.weight(1f))
            work(Modifier.weight(1f).fillMaxHeight())
        } else Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            NativeActivityTitle(LengthOrdering.title.display[language])
            if(state!=null)OrderBoard(state,language,ready,onPlace,onRemove,Modifier.fillMaxWidth())
            work(Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun OrderBoard(s: LengthOrdering.State, language: ContentLanguage, ready: Boolean,
    place: (ContentId) -> Unit, remove: (ContentId) -> Unit, modifier: Modifier) {
    val de=language==ContentLanguage.GERMAN
    fun t(en: String,german: String)=if(de)german else en
    Column(modifier.testTag("length-order-board"),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(t("Strips","Streifen"),Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
            Text(t("Your order","Deine Reihe"),Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
        }
        repeat(3) { index ->
            val source=s.task.presentation[index];val placed=s.current.order.getOrNull(index)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                val available=source !in s.current.order
                val label=LengthOrdering.letter(source)
                val description=t("Strip $label. Length ${s.task.length(source)} comparison units. ","Streifen $label. Länge ${s.task.length(source)} Vergleichseinheiten. ")+
                    if(available)t("Tap to add to the next place.","Tippen, um ihn an den nächsten Platz zu legen.")else t("Already placed on the right.","Bereits rechts eingeordnet.")
                StripCell(if(available)source else null,label,s,description,ready&&!s.solved&&available,
                    {place(source)},"source-${source.value.takeLast(1)}",Modifier.weight(1f))
                val destination=t("Place ${index+1}. ","Platz ${index+1}. ")+if(placed==null) t("Empty. ${if(index==0)"Shortest at the top."else if(index==2)"Longest at the bottom."else"Middle length."}","Leer. ${if(index==0)"Der kürzeste nach oben."else if(index==2)"Der längste nach unten."else"Mittlere Länge."}")
                    else t("Strip ${LengthOrdering.letter(placed)}. Length ${s.task.length(placed)} comparison units. Tap to put it back.","Streifen ${LengthOrdering.letter(placed)}. Länge ${s.task.length(placed)} Vergleichseinheiten. Tippen, um ihn zurückzulegen.")
                StripCell(placed,placed?.let(LengthOrdering::letter) ?: "${index+1}",s,destination,ready&&!s.solved&&placed!=null,
                    {placed?.let(remove)},"place-${index+1}",Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StripCell(strip: ContentId?, label: String, state: LengthOrdering.State, description: String, enabled: Boolean,
    onClick: () -> Unit, tag: String, modifier: Modifier) {
    Surface(onClick=onClick,enabled=enabled,modifier=modifier.height(60.dp).testTag("length-order-$tag").semantics { contentDescription=description },
        shape=RoundedCornerShape(12.dp),color=Color(0xFFFFF8EA),border=BorderStroke(1.dp,Color(0xFF425E60))) {
        Row(Modifier.fillMaxSize().padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
            Text(label,Modifier.width(26.dp).clearAndSetSemantics {},style=MaterialTheme.typography.titleMedium)
            Canvas(Modifier.weight(1f).fillMaxHeight().testTag("length-order-track-$tag").clearAndSetSemantics {}) {
                drawLengthStrip(strip?.let(state.task::length),colour=state.task.palette+(strip?.let { LengthOrdering.strips.indexOf(it) } ?: 0))
            }
        }
    }
}
