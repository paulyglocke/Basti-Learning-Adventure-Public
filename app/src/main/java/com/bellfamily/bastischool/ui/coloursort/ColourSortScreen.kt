package com.bellfamily.bastischool.ui.coloursort

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.coloursort.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.ui.common.*

private fun colour(id:ContentId)=if(id==ColourSort.red)Color(0xFFD32F2F) else Color(0xFF1565C0)
@Composable private fun SortToken(item:ColourSortObject,modifier:Modifier=Modifier) {
    Canvas(modifier) {
        val paint=colour(item.category)
        if(item.shape==ColourSortShape.BALL){drawCircle(paint);drawCircle(Color(0xFF263238),style=Stroke(2.dp.toPx()))}
        else {drawRect(paint);drawRect(Color(0xFF263238),style=Stroke(2.dp.toPx()))}
    }
}
@Composable fun ColourSortScreen(state:SortState?,language:ContentLanguage,busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,
    onAction:(SortAction)->Unit,onAgain:()->Unit,onRetry:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    val ready=state!=null && !busy && !saveFailed && state.language==language
    if(state?.completed==true){
        NativeCompletionScreen(state.id.value,language,ColourSort.completion.display[language],ready,"sort-",
            {onAction(SortAction.Replay)},onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(ColourSort.title.display[language],style=MaterialTheme.typography.headlineMedium)
        if(state==null)Text(t("Opening…","Wird geöffnet…")) else {
            Text(t("${state.placements.count {it.placed}} of 4 sorted","${state.placements.count {it.placed}} von 4 einsortiert"),Modifier.testTag("sort-progress"))
            Text(ColourSort.instruction.display[language],style=MaterialTheme.typography.titleMedium)
            NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,{onAction(SortAction.Replay)},Modifier.fillMaxWidth().testTag("sort-replay"),ready)
            Text(if(state.selected==null)t("Choose an object","Wähle ein Ding") else ColourSort.selectedInstruction.display[language],Modifier.testTag("sort-step"))
            state.order.filter {!state.placement(it).placed}.chunked(2).forEach {row ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    row.forEach {id ->
                        val item=ColourSort.item(id);val chosen=state.selected==id
                        Column(Modifier.weight(1f).heightIn(min=112.dp).border(if(chosen)4.dp else 1.dp,
                            if(chosen)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,RoundedCornerShape(16.dp))
                            .clickable(ready,role=Role.Button){onAction(SortAction.Select(id))}
                            .semantics {selected=chosen;stateDescription=if(chosen)t("Selected","Ausgewählt") else t("Available to sort","Zum Einsortieren")}
                            .testTag("sort-item-${id.value}").padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,
                            verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            SortToken(item,Modifier.size(64.dp))
                            Text(item.text.display[language])
                        }
                    }
                    if(row.size==1)Spacer(Modifier.weight(1f))
                }
            }
            state.selectedPlacement?.let {p ->
                if(p.attempts>0)NativeSupportMessage(ColourSort.wrong.display[language],Modifier.testTag("sort-guidance"))
                if(p.support.hint)NativeSupportMessage(ColourSort.hint(state).display[language],Modifier.testTag("sort-hint"))
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                state.rule.categories.forEach {category ->
                    val placed=state.rule.items.filter {it.category==category && state.placement(it.id).placed}
                    val name=ColourSort.category(category).display[language]
                    Column(Modifier.weight(1f).heightIn(min=160.dp)
                        .background(colour(category).copy(alpha=0.08f),RoundedCornerShape(20.dp))
                        .border(4.dp,colour(category),RoundedCornerShape(20.dp))
                        .clickable(ready && state.selected!=null,role=Role.Button) {
                            state.selected?.let {onAction(SortAction.Place(state.id,it,category,state.placement(it).attempts+1))}
                        }.semantics {contentDescription=t("$name group. ${placed.size} objects.","Farbgruppe $name. ${placed.size} Dinge.")}
                        .testTag("sort-category-${category.value}").padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Text(name,style=MaterialTheme.typography.titleLarge,modifier=Modifier.clearAndSetSemantics {})
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {placed.forEach {
                            SortToken(ColourSort.item(it.id),Modifier.size(40.dp).testTag("sort-placed-${it.id.value}"))
                        }}
                    }
                }
            }
            if(state.selected==null && state.placements.any {it.placed})Text(ColourSort.correct.display[language],Modifier.testTag("sort-correct"))
            if(state.selected!=null)NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,{onAction(SortAction.Hint)},Modifier.testTag("sort-help"),ready)
        }
        if(saveFailed) {
            Text(t("Your saved work is kept. Please try again.","Deine gespeicherte Arbeit bleibt erhalten. Versuche es noch einmal."))
            NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("sort-save-retry"),!busy)
        }
        if(audioFailed)Text(t("Speech is unavailable. Check installed offline voices in Options.","Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome,Modifier.fillMaxWidth().testTag("sort-home"))
    }
}
