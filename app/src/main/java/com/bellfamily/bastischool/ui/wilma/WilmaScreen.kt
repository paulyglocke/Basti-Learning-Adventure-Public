package com.bellfamily.bastischool.ui.wilma

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.bellfamily.bastischool.ui.common.NativeCompletionScreen
import androidx.compose.runtime.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.wilma.*

@Composable
fun WilmaScreen(selection:WilmaSelection?,quiz:SessionState?,ordering:WilmaOrderState?,language:ContentLanguage,
    images:Map<String,ImageBitmap>,busy:Boolean,saveFailed:Boolean,imageFailed:Boolean,audioFailed:Boolean,
    onPhase:(WilmaPhase)->Unit,onDay:(ContentId)->Unit,onReplay:()->Unit,onOption:(ContentId)->Unit,
    onAction:(SessionAction)->Unit,onOrder:(WilmaOrderAction)->Unit,onAgain:()->Unit,onRetry:()->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,german:String)=if(de)german else en
    val ready=!busy && !saveFailed && selection!=null
    val completedOrder = selection?.phase == WilmaPhase.ORDER && ordering?.completed == true
    val completedQuiz = selection?.phase in listOf(WilmaPhase.FIND, WilmaPhase.RELATIONS) && quiz?.phase == SessionPhase.COMPLETED
    if(completedOrder || completedQuiz) {
        val id = if(completedOrder) ordering!!.id.value else quiz!!.plan.id.value
        val sameLanguage = (if(completedOrder) ordering!!.language else quiz!!.language) == language
        NativeCompletionScreen(id, language, if(completedOrder) t("You put the whole week in order!","Du hast die ganze Woche geordnet!") else quiz!!.plan.completionText.display[language],
            ready && sameLanguage, "wilma-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed) {
            if(completedOrder) {
                Text(t("7 of 7 days placed","7 von 7 Tagen eingeordnet"),modifier=Modifier.testTag("wilma-placed-count"))
                WilmaStrip(images,language,ordering!!.placed,null,emptySet(),{},prefix="placed")
            }
            WilmaPhase.entries.forEach {phase ->
                OutlinedButton(onClick = {onPhase(phase)}, enabled = ready, modifier = Modifier.heightIn(min = 56.dp).testTag("wilma-phase-${phase.name}")) {Text(phase.title.display[language])}
            }
        }
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFF1F8E9)).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(t("Wilma’s Week","Wilmas Woche"),style=MaterialTheme.typography.headlineMedium)
        WilmaPhase.entries.chunked(2).forEach {row -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach {phase ->
            OutlinedButton(onClick={onPhase(phase)},enabled=ready,modifier=Modifier.weight(1f).heightIn(min=56.dp).testTag("wilma-phase-${phase.name}")) {
                Text(phase.title.display[language])
            }
        } } }
        if(saveFailed) Text(t("Your saved work is kept. Please try saving or opening it again.","Deine gespeicherte Arbeit bleibt erhalten. Versuche erneut, sie zu speichern oder zu öffnen."))
        if(imageFailed) Text(t("Wilma’s pictures could not be opened. Please try again.","Wilmas Bilder konnten nicht geöffnet werden. Bitte versuche es erneut."))
        if(saveFailed || imageFailed) Button(onClick=onRetry,enabled=!busy,modifier=Modifier.testTag("wilma-retry-save")){Text(t("Try again","Erneut versuchen"))}
        if(audioFailed) Text(t("Speech is unavailable. Check the installed offline English and German voices in device settings.","Die Sprachausgabe ist nicht verfügbar. Prüfe die installierten Offline-Stimmen für Englisch und Deutsch in den Geräte-Einstellungen."))
        if(selection==null) Text(t("Opening…","Wird geöffnet…")) else {
            Text(selection.phase.title.display[language],style=MaterialTheme.typography.titleLarge)
            Button(onClick=onReplay,enabled=ready,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("wilma-replay")) {Text(t("Listen again","Noch einmal hören"))}
            when(selection.phase) {
                WilmaPhase.EXPLORE -> {
                    Text(t("Tap a day to hear its name. Swipe along Wilma to see the whole week.","Tippe auf einen Tag, um seinen Namen zu hören. Wische an Wilma entlang, um die ganze Woche zu sehen."))
                    // Whole-character overview preserves orientation on narrow displays; not another answer target.
                    images[WilmaContent.REFERENCE]?.let {Image(it,null,Modifier.fillMaxWidth().heightIn(max=160.dp),contentScale=ContentScale.Fit)}
                    WilmaStrip(images,language,WilmaContent.days,selection.selected,if(ready)WilmaContent.days.toSet() else emptySet(),onDay)
                    Text(WilmaContent.day(selection.selected).text.display[language],style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("wilma-selected"))
                }
                WilmaPhase.FIND,WilmaPhase.RELATIONS -> if(quiz!=null) {
                    val canAct=ready && quiz.language==language
                    if(quiz.phase==SessionPhase.ACTIVE) {
                        Text(t("Question ${quiz.index+1} of ${quiz.plan.tasks.size}","Frage ${quiz.index+1} von ${quiz.plan.tasks.size}"))
                        Text(quiz.task.question.instruction.display[language],style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("wilma-prompt"))
                        Text(t("Swipe along Wilma to find all seven days.","Wische an Wilma entlang, um alle sieben Tage zu finden."))
                        WilmaStrip(images,language,quiz.task.question.choices,quiz.current.lastChoice,
                            if(canAct && !imageFailed && quiz.current.answer==AnswerState.UNANSWERED && quiz.nextAttempt!=null)WilmaContent.days.toSet() else emptySet(),
                            {id -> quiz.nextAttempt?.let {onAction(SessionAction.Answer(it,id))}},onOption=onOption,listenEnabled=canAct)
                        if(quiz.current.answer==AnswerState.CORRECT)Text(quiz.task.question.correctFeedback.display[language])
                        if(quiz.current.answer==AnswerState.RETRY_AVAILABLE) {
                            Text(quiz.task.question.wrongFeedback.display[language])
                            Button(onClick={onAction(SessionAction.Retry(AttemptId(quiz.task.id,quiz.current.attempts)))},enabled=canAct,modifier=Modifier.testTag("wilma-retry")){Text(t("Try again","Nochmal versuchen"))}
                        }
                        if(quiz.current.support.hint)Text(quiz.task.question.hint!!.display[language])
                        if(!quiz.current.locked)OutlinedButton(onClick={onAction(SessionAction.Hint(quiz.task.id))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("wilma-help")){Text(t("Help","Hilfe"))}
                        if(quiz.current.locked)Button(onClick={onAction(SessionAction.Next(quiz.task.id))},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("wilma-next")){Text(t("Next","Weiter"))}
                    }
                }
                WilmaPhase.ORDER -> if(ordering!=null) {
                    val canAct=ready && ordering.language==language
                    Text(WilmaOrder.prompt(ordering).display[language],style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("wilma-prompt"))
                    Text(t("${ordering.index} of 7 days placed","${ordering.index} von 7 Tagen eingeordnet"),modifier=Modifier.testTag("wilma-placed-count"))
                    WilmaStrip(images,language,ordering.placed,null,emptySet(),{},prefix="placed",showTail=ordering.completed)
                    if(!ordering.completed) {
                        Text(t("Tap the next day below. The days already placed stay on Wilma.","Tippe unten auf den nächsten Tag. Die eingeordneten Tage bleiben an Wilma."))
                        ordering.choices.filter {it !in ordering.placed}.chunked(2).forEach {row -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach {id ->
                            Column(Modifier.weight(1f)) {
                                OutlinedButton(onClick={ordering.nextAttempt?.let {onOrder(WilmaOrderAction.Place(it,id))}},
                                    colors=wilmaDayButtonColours(id),
                                    enabled=canAct && !imageFailed && ordering.nextAttempt!=null,modifier=Modifier.fillMaxWidth().heightIn(min=64.dp).testTag("order-${id.value}")) {
                                    Text(WilmaContent.day(id).text.display[language])
                                }
                                OutlinedButton(onClick={onOption(id)},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("order-speaker-${id.value}")) {
                                    Text(t("Hear ${WilmaContent.day(id).text.display[language]}","${WilmaContent.day(id).text.display[language]} anhören"))
                                }
                            }
                        } } }
                        if(ordering.current.answer==AnswerState.RETRY_AVAILABLE) {
                            Text(t("Try again. Your placed days stay here.","Versuche es noch einmal. Deine eingeordneten Tage bleiben hier."))
                            Button(onClick={onOrder(WilmaOrderAction.Retry(AttemptId(ordering.task,ordering.current.attempts)))},enabled=canAct,modifier=Modifier.testTag("wilma-retry")){Text(t("Try again","Nochmal versuchen"))}
                        }
                        if(ordering.current.support.hint)Text(WilmaOrder.help(ordering).display[language])
                        OutlinedButton(onClick={onOrder(WilmaOrderAction.Help(ordering.task))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("wilma-help")){Text(t("Help","Hilfe"))}
                    }
                }
            }
        }
        Button(onClick=onHome,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("wilma-home")){Text(t("Back home","Zurück zum Start"))}
    }
}

/** One neutral head, semantic weekday cells and optional neutral tail. Decorations have no click action. */
@Composable
internal fun WilmaStrip(images:Map<String,ImageBitmap>,language:ContentLanguage,days:List<ContentId>,selected:ContentId?,
    enabled:Set<ContentId>,onDay:(ContentId)->Unit,prefix:String="day",showTail:Boolean=true,
    onOption:((ContentId)->Unit)?=null,listenEnabled:Boolean=false) {
    val follow = prefix == "placed"
    // First layout/restoration starts at the growing end without an animation.
    val scroll = rememberScrollState(if(follow) Int.MAX_VALUE else 0)
    var viewportWidth by remember {mutableIntStateOf(0)}
    var previousCount by remember {mutableIntStateOf(days.size)}
    LaunchedEffect(follow, days.size, viewportWidth) {
        if(follow && viewportWidth > 0) {
            // Wait for the new segment's measure pass, not a wall-clock timer.
            withFrameNanos { }
            val grew = days.size > previousCount
            previousCount = days.size
            if(grew) scroll.animateScrollTo(scroll.maxValue, tween(400))
            else scroll.scrollTo(scroll.maxValue)
        }
    }
    Row(Modifier.fillMaxWidth().onSizeChanged {viewportWidth = it.width}.horizontalScroll(scroll).testTag("wilma-strip-$prefix"),verticalAlignment=Alignment.Bottom) {
        Column(Modifier.width(120.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            images[WilmaContent.HEAD]?.let {Image(it,null,Modifier.size(120.dp).testTag("wilma-head-$prefix"))}
            Spacer(Modifier.height(if(onOption==null)64.dp else 120.dp))
        }
        days.forEach {id ->
            val label=WilmaContent.day(id).text.display[language]
            Column(Modifier.width(128.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Column(Modifier.fillMaxWidth().selectable(selected==id,enabled=id in enabled,role=Role.Button,onClick={onDay(id)})
                    .border(if(selected==id)3.dp else 1.dp,if(selected==id)Color(0xFF275C2F) else Color.Transparent,RoundedCornerShape(12.dp))
                    .testTag("$prefix-${id.value}"),horizontalAlignment=Alignment.CenterHorizontally) {
                    images[WilmaContent.image(id)]?.let {Image(it,null,Modifier.size(128.dp).graphicsLayer(scaleX=1.3f,scaleY=1.3f))}
                    val cue = WilmaDayColours.background(id, id in enabled, MaterialTheme.colorScheme.surface)
                    Text(label,modifier=Modifier.fillMaxWidth().heightIn(min=64.dp)
                        .then(if(prefix=="day") Modifier.background(cue,RoundedCornerShape(12.dp)) else Modifier)
                        .padding(4.dp),textAlign=TextAlign.Center,
                        color=if(prefix=="day") WilmaDayColours.foreground(cue) else Color.Unspecified)
                }
                if(onOption!=null)OutlinedButton(onClick={onOption(id)},enabled=listenEnabled,
                    modifier=Modifier.heightIn(min=56.dp).testTag("wilma-speaker-${id.value}").semantics {contentDescription=if(language==ContentLanguage.GERMAN)"Anhören: $label" else "Listen: $label"}) {
                    Text(if(language==ContentLanguage.GERMAN)"Hören" else "Listen")
                }
            }
        }
        if(showTail)Column(Modifier.width(72.dp)) {
            images[WilmaContent.TAIL]?.let {Image(it,null,Modifier.size(72.dp))}
            Spacer(Modifier.height(if(onOption==null)64.dp else 120.dp))
        }
    }
}

@Composable
private fun wilmaDayButtonColours(id: ContentId): ButtonColors {
    val active = WilmaDayColours.day(id)
    val disabled = WilmaDayColours.background(id, false, MaterialTheme.colorScheme.surface)
    return ButtonDefaults.outlinedButtonColors(containerColor = active,
        contentColor = WilmaDayColours.foreground(active), disabledContainerColor = disabled,
        disabledContentColor = WilmaDayColours.foreground(disabled))
}
