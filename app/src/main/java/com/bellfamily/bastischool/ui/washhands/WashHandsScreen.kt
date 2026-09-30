package com.bellfamily.bastischool.ui.washhands

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sequencing.*
import com.bellfamily.bastischool.learning.washhands.*
import com.bellfamily.bastischool.ui.common.*

@Composable fun WashHandsScreen(state:SequenceState?,language:ContentLanguage,busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,
    onAction:(SequenceAction)->Unit,onListen:(ContentId)->Unit,onAgain:()->Unit,onRetry:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    val ready=state!=null && !busy && !saveFailed && state.language==language
    fun act(op:SequenceOperation){state?.let {onAction(SequenceAction(it.id,it.revision,op))}}
    if(state?.completed==true){
        NativeCompletionScreen(state.id.value,language,WashHands.completion.display[language],ready,"wash-",
            {act(SequenceOperation.Replay)},onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed)
        return
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(WashHands.title.display[language],style=MaterialTheme.typography.headlineMedium)
        Text(WashHands.instruction.display[language])
        NativeActionButton(t("Listen","Anhören"),NativeActionRole.SECONDARY,{act(SequenceOperation.Replay)},Modifier.fillMaxWidth().testTag("wash-replay"),ready)
        if(state!=null){
            if(state.available.isNotEmpty())Text(t("Choose the next step","Wähle den nächsten Schritt"),style=MaterialTheme.typography.titleLarge)
            state.available.forEach {id ->
                val label=WashHands.step(id).text.display[language]
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        NativeActionButton(label,NativeActionRole.SECONDARY,{act(SequenceOperation.Append(id))},
                            Modifier.fillMaxWidth().testTag("wash-item-${id.value}").semantics {stateDescription=t("Available step. Add to your order.","Verfügbarer Schritt. Zur Reihenfolge hinzufügen.")},ready)
                        NativeActionButton(t("Listen: $label","Anhören: $label"),NativeActionRole.SECONDARY,{onListen(id)},
                            Modifier.fillMaxWidth().testTag("wash-listen-${id.value}"),ready)
                    }
                }
            }
            Text(t("Your order","Deine Reihenfolge"),style=MaterialTheme.typography.titleLarge)
            state.target.indices.forEach {i ->
                val id=state.constructed.getOrNull(i)
                OutlinedCard(Modifier.fillMaxWidth().testTag("wash-position-${i+1}")) {
                    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(t("Step ${i+1}","Schritt ${i+1}"),style=MaterialTheme.typography.titleMedium)
                        if(id==null)Text(t("Place a step here","Hier kommt ein Schritt hin")) else {
                            val label=WashHands.step(id).text.display[language]
                            Text(label)
                            NativeActionButton(t("Listen: $label","Anhören: $label"),NativeActionRole.SECONDARY,{onListen(id)},
                                Modifier.fillMaxWidth().testTag("wash-listen-${id.value}"),ready)
                            NativeActionButton(t("Remove step ${i+1}","Schritt ${i+1} zurücklegen"),NativeActionRole.SECONDARY,{act(SequenceOperation.Remove(id))},
                                Modifier.fillMaxWidth().testTag("wash-remove-${id.value}"),ready)
                        }
                    }
                }
            }
            if(state.needsCorrection)NativeSupportMessage(WashHands.wrong.display[language],Modifier.testTag("wash-guidance"))
            if(state.hintPosition!=null)NativeSupportMessage(WashHands.hint(state).display[language],Modifier.testTag("wash-hint"))
            NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,{act(SequenceOperation.Hint)},Modifier.testTag("wash-help"),ready)
            NativeActionButton(t("Check","Prüfen"),NativeActionRole.PRIMARY,{act(SequenceOperation.Check)},Modifier.fillMaxWidth().testTag("wash-check"),ready && state.available.isEmpty())
        } else Text(t("Opening…","Wird geöffnet…"))
        if(saveFailed){Text(t("Your saved work is kept. Please try again.","Deine gespeicherte Arbeit bleibt erhalten. Versuche es noch einmal."))
            NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("wash-save-retry"),!busy)}
        if(audioFailed)Text(t("Speech is unavailable. Check installed offline voices in Options.","Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome,Modifier.fillMaxWidth().testTag("wash-home"))
    }
}
