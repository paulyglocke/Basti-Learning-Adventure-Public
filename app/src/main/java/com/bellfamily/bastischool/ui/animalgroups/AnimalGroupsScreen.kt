package com.bellfamily.bastischool.ui.animalgroups

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.animalgroups.*
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.sorting.*
import com.bellfamily.bastischool.ui.common.*

/** Decorative context cues, never an extra focus/action target. */
@Composable private fun HabitatCue(category: ContentId) {
    val ink = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(64.dp, 36.dp)) {
        if (category == AnimalGroups.water) {
            repeat(3) { row ->
                val y = size.height * (row + 1) / 4
                repeat(6) { column ->
                    val x = size.width * column / 6
                    drawLine(ink, Offset(x, y), Offset(x + size.width / 12, y - 4.dp.toPx()), 2.dp.toPx())
                    drawLine(ink, Offset(x + size.width / 12, y - 4.dp.toPx()), Offset(x + size.width / 6, y), 2.dp.toPx())
                }
            }
        } else {
            val y = size.height * .8f
            drawLine(ink, Offset(0f,y), Offset(size.width,y), 3.dp.toPx())
            listOf(.2f,.5f,.8f).forEach { x ->
                val base=Offset(size.width*x,y)
                drawLine(ink,base,Offset(base.x-5.dp.toPx(),y-14.dp.toPx()),2.dp.toPx())
                drawLine(ink,base,Offset(base.x+5.dp.toPx(),y-18.dp.toPx()),2.dp.toPx())
            }
        }
    }
}
@Composable fun AnimalGroupsScreen(state:SortState?,language:ContentLanguage,busy:Boolean,saveFailed:Boolean,audioFailed:Boolean,
    images:Map<ContentId,ImageBitmap>,
    onAction:(SortAction)->Unit,onAgain:()->Unit,onRetry:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    val ready=state!=null && !busy && !saveFailed && state.language==language
    if(state?.completed==true){
        NativeCompletionScreen(state.id.value,language,AnimalGroups.completion.display[language],ready,"groups-",
            {onAction(SortAction.Replay)},onAgain,onHome,onPop,modifier,saveFailed,onRetry,audioFailed)
        return
    }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(AnimalGroups.title.display[language],style=MaterialTheme.typography.headlineMedium)
        if(state==null)Text(t("Opening…","Wird geöffnet…")) else {
            Text(t("${state.placements.count {it.placed}} of 4 sorted","${state.placements.count {it.placed}} von 4 einsortiert"),Modifier.testTag("groups-progress"))
            Text(AnimalGroups.instruction.display[language],style=MaterialTheme.typography.titleMedium)
            Text(AnimalGroups.interaction.display[language])
            NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,{onAction(SortAction.Replay)},Modifier.fillMaxWidth().testTag("groups-replay"),ready)
            Text(if(state.selected==null)t("Choose an animal","Wähle ein Tier") else AnimalGroups.selectedInstruction.display[language],Modifier.testTag("groups-step"))
            if(state.selected!=null)NativeActionButton(t("Help","Hilfe"),NativeActionRole.SECONDARY,{onAction(SortAction.Hint)},Modifier.testTag("groups-help"),ready)
            state.selectedPlacement?.let {p ->
                if(p.attempts>0)NativeSupportMessage(AnimalGroups.wrong.display[language],Modifier.testTag("groups-guidance"))
                if(p.support.hint)NativeSupportMessage(AnimalGroups.hint(state).display[language],Modifier.testTag("groups-hint"))
            }
            state.order.filter {!state.placement(it).placed}.chunked(2).forEach {row ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    row.forEach {id ->
                        val item=AnimalGroups.item(id);val chosen=state.selected==id;val image=images[id]
                        Column(Modifier.weight(1f).heightIn(min=160.dp).border(if(chosen)4.dp else 1.dp,
                            if(chosen)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,RoundedCornerShape(16.dp))
                            .then(if(image!=null) Modifier.clickable(ready,role=Role.Button){onAction(SortAction.Select(id))} else Modifier)
                            .semantics {selected=chosen;stateDescription=if(chosen)t("Selected","Ausgewählt") else t("Available to sort","Zum Einsortieren")}
                            .testTag("groups-item-${id.value}").padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,
                            verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            if(image!=null) Image(image,contentDescription=null,contentScale=ContentScale.Fit,
                                modifier=Modifier.fillMaxWidth().height(120.dp).testTag("groups-art-${id.value}"))
                            else Text(t("Picture unavailable","Bild nicht verfügbar"))
                            Text(item.text.display[language])
                        }
                    }
                    if(row.size==1)Spacer(Modifier.weight(1f))
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                state.rule.categories.forEach {category ->
                    val placed=state.rule.items.filter {it.category==category && state.placement(it.id).placed}
                    val name=AnimalGroups.category(category).display[language]
                    Column(Modifier.weight(1f).heightIn(min=160.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant,RoundedCornerShape(20.dp))
                        .border(3.dp,MaterialTheme.colorScheme.outline,RoundedCornerShape(20.dp))
                        .clickable(ready && state.selected!=null && images.containsKey(state.selected),role=Role.Button) {
                            state.selected?.let {onAction(SortAction.Place(state.id,it,category,state.placement(it).attempts+1))}
                        }.semantics {contentDescription=t("$name group. ${placed.size} animals.","Gruppe $name. ${placed.size} Tiere.")}
                        .testTag("groups-category-${category.value}").padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        HabitatCue(category)
                        Text(name,style=MaterialTheme.typography.titleLarge,modifier=Modifier.clearAndSetSemantics {})
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {placed.forEach {
                            images[it.id]?.let { image -> Image(image,contentDescription=null,contentScale=ContentScale.Fit,
                                modifier=Modifier.size(48.dp).testTag("groups-placed-${it.id.value}")) }
                        }}
                    }
                }
            }
            if(state.selected==null && state.placements.any {it.placed})Text(AnimalGroups.correct.display[language],Modifier.testTag("groups-correct"))
        }
        if(saveFailed) {
            Text(t("Your saved work is kept. Please try again.","Deine gespeicherte Arbeit bleibt erhalten. Versuche es noch einmal."))
            NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("groups-save-retry"),!busy)
        }
        if(audioFailed)Text(t("Speech is unavailable. Check installed offline voices in Options.","Die Sprachausgabe ist nicht verfügbar. Prüfe die Offline-Stimmen in den Optionen."))
        NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome,Modifier.fillMaxWidth().testTag("groups-home"))
    }
}
