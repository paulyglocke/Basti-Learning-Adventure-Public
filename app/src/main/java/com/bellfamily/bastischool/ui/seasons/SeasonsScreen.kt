package com.bellfamily.bastischool.ui.seasons

import com.bellfamily.bastischool.ui.common.NativeActivityTitle
import com.bellfamily.bastischool.ui.common.NativeQuestionProgress

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.bellfamily.bastischool.ui.common.NativeCompletionScreen
import com.bellfamily.bastischool.ui.common.NativeTextChoice
import com.bellfamily.bastischool.ui.common.NativeActionButton
import com.bellfamily.bastischool.ui.common.NativeSupportMessage
import com.bellfamily.bastischool.ui.common.NativeActionRole
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.seasons.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.sorting.*

@Composable
fun DaysSeasonsHub(language: ContentLanguage, onSeasons: () -> Unit, onWilma: () -> Unit, onLegacy: () -> Unit, modifier: Modifier = Modifier) {
    val de = language == ContentLanguage.GERMAN
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(if(de) "Tage & Jahreszeiten" else "Days & Seasons", style = MaterialTheme.typography.headlineMedium)
        Button(onClick=onWilma, modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("open-wilma")) {
            Text(if(de) "Wilmas Woche" else "Wilma’s Week")
        }
        Button(onClick=onSeasons, modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("open-seasons")) {
            Text(if(de) "Jahreszeiten – Lernen und Üben" else "Seasons – Learn and practise")
        }
        OutlinedButton(onClick=onLegacy, modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("legacy-calendar")) {
            Text(if(de) "Tage & Jahreszeiten – bisherige Version" else "Days & Seasons – previous version")
        }
    }
}

@Composable
fun SeasonsScreen(selection: SeasonsSelection?, state: SessionState?, language: ContentLanguage, artwork: ImageBitmap?,
                  busy: Boolean, saveFailed: Boolean, imageFailed: Boolean, audioFailed: Boolean,
                  onSelect: (ContentId) -> Unit, onPhase: (SeasonsPhase) -> Unit, onReplay: () -> Unit,
                  onAction: (SessionAction) -> Unit, onOption: (ContentId) -> Unit, onAgain: () -> Unit,
                  onRetry: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier, onPop: (String) -> Unit = {},
                  ordering: SeasonsOrderState? = null, orderArtwork: Map<ContentId,ImageBitmap> = emptyMap(),
                  onOrder: (SeasonsOrderAction) -> Unit = {}, matching: SortState? = null,
                  onMatch: (SortAction) -> Unit = {}, combined: SeasonsCombined.State? = null,
                  onCombined: (SeasonsCombined.Action) -> Unit = {}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,german:String)=if(de)german else en
    val ready=!busy && !saveFailed && selection!=null
    val completedOrder = selection?.phase == SeasonsPhase.ORDER && ordering?.completed == true
    val completedMatch = selection?.phase == SeasonsPhase.MATCH && matching?.completed == true
    val completedCombined = selection?.phase == SeasonsPhase.COMBINED && combined?.completed == true
    if(completedOrder || completedMatch || completedCombined || selection?.phase?.isQuiz == true && state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(if(completedOrder) ordering!!.id.value else if(completedMatch) matching!!.id.value else if(completedCombined) combined!!.id else state!!.plan.id.value, language,
            if(completedOrder) SeasonsOrder.completion.display[language] else if(completedMatch) SeasonsMatch.completion.display[language] else if(completedCombined) if(de) "Du hast beide Nachbarn gefunden!" else "You found both neighbours!" else state!!.plan.completionText.display[language],
            ready && (if(completedOrder) ordering!!.language else if(completedMatch) matching!!.language else if(completedCombined) combined!!.language else state!!.language) == language,
            "seasons-", onReplay, onAgain, onHome, onPop, modifier, saveFailed, onRetry, audioFailed) {
            if(completedOrder) PlacedSeasons(ordering!!,orderArtwork,language)
            SeasonModes(selection!!.phase,language,ready,onPhase)
        }
        return
    }
    Column(modifier.fillMaxSize().background(Color(0xFFEAF7FC)).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(t("Seasons", "Jahreszeiten"))
        SeasonModes(selection?.phase,language,ready,onPhase)
        if(saveFailed) Text(t("Progress could not be saved or restored. Please try again. Saved records are kept.",
            "Der Fortschritt konnte nicht gespeichert oder wiederhergestellt werden. Bitte versuche es erneut. Gespeicherte Einträge bleiben erhalten."))
        if(imageFailed) Text(t("The picture could not be opened. Please try again.","Das Bild konnte nicht geöffnet werden. Bitte versuche es erneut."))
        if(saveFailed || imageFailed) NativeActionButton(t("Try again","Erneut versuchen"), NativeActionRole.SECONDARY, onClick=onRetry,enabled=!busy,modifier=Modifier.testTag("retry-load"))
        if(audioFailed) Text(t("Speech is unavailable. Please check installed offline English and German voices in device settings.",
            "Die Sprachausgabe ist nicht verfügbar. Bitte prüfe installierte Offline-Stimmen für Englisch und Deutsch in den Geräte-Einstellungen."))
        if(selection==null) Text(t("Opening…","Wird geöffnet…"))
        else if(selection.phase==SeasonsPhase.EXPLORE) {
            SeasonIds.canonicalOrder.chunked(2).forEach { row ->
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { row.forEach { id ->
                    OutlinedButton(onClick={onSelect(id)},enabled=ready,
                        modifier=Modifier.weight(1f).heightIn(min=56.dp).testTag("season-${id.value}")) {
                        Text(SeasonsContent.season(id).text.display[language])
                    }
                } }
            }
            Text(selection.season.text.display[language],style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("season-name"))
            NativeActionButton(t("Listen again","Noch einmal hören"), NativeActionRole.SECONDARY, onClick=onReplay,enabled=ready,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-replay"))
            BoxWithConstraints {
                if(maxWidth>=700.dp) Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    SeasonPicture(artwork,selection.season.text.display[language],Modifier.weight(1.4f))
                    Text(selection.season.spokenDescription[language],modifier=Modifier.weight(1f).testTag("season-description"))
                } else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    SeasonPicture(artwork,selection.season.text.display[language],Modifier.fillMaxWidth())
                    Text(selection.season.spokenDescription[language],modifier=Modifier.testTag("season-description"))
                }
            }
        } else if(selection.phase == SeasonsPhase.COMBINED && combined != null) {
            SeasonsCombinedView(combined, language, ready, onCombined, onOption)
        } else if(selection.phase == SeasonsPhase.MATCH && matching != null) {
            SeasonsMatchView(matching, language, ready, onMatch, onOption, modifier = Modifier.fillMaxWidth())
        } else if(selection.phase == SeasonsPhase.ORDER && ordering != null) {
            val canAct=ready && ordering.language==language
            NativeActionButton(t("Listen again","Noch einmal hören"), NativeActionRole.SECONDARY, onClick=onReplay,enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-replay"))
            Text(SeasonsOrder.prompt(ordering).display[language],style=MaterialTheme.typography.titleLarge,modifier=Modifier.testTag("seasons-order-prompt"))
            PlacedSeasons(ordering,orderArtwork,language)
            ordering.choices.filter {it !in ordering.placed}.chunked(2).forEach {row ->
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach {id ->
                    Column(Modifier.weight(1f)) {
                        OutlinedButton(onClick={ordering.nextAttempt?.let {onOrder(SeasonsOrderAction.Place(it,id))}},
                            enabled=canAct && !imageFailed && ordering.nextAttempt!=null,
                            modifier=Modifier.fillMaxWidth().heightIn(min=64.dp).testTag("season-order-${id.value}")) {
                            Column {
                                orderArtwork[id]?.let {Image(it,null,Modifier.fillMaxWidth().aspectRatio(4f/3f),contentScale=ContentScale.Fit)}
                                Text(SeasonsContent.season(id).text.display[language])
                            }
                        }
                        OutlinedButton(onClick={onOption(id)},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("order-speaker-${id.value}").semantics {contentDescription=t("Listen: ","Anhören: ")+SeasonsContent.season(id).text.display[language]}) {
                            Text(t("Listen","Hören"))
                        }
                    }
                }}
            }
            if(ordering.current.answer==AnswerState.RETRY_AVAILABLE) {
                NativeSupportMessage(t("Try again. The seasons already placed stay here.","Versuche es noch einmal. Die eingeordneten Jahreszeiten bleiben hier."))
                NativeActionButton(t("Try again","Nochmal versuchen"), NativeActionRole.SECONDARY, onClick={onOrder(SeasonsOrderAction.Retry(AttemptId(ordering.task,ordering.current.attempts)))},enabled=canAct,modifier=Modifier.testTag("seasons-retry"))
            }
            if(ordering.current.support.hint) NativeSupportMessage(SeasonsOrder.help(ordering).display[language])
            NativeActionButton(t("Help","Hilfe"), NativeActionRole.SECONDARY, onClick={onOrder(SeasonsOrderAction.Help(ordering.task))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("seasons-hint"))
        } else if(state!=null) {
            val task=state.task
            val canAct=ready && state.language==language
            if(state.phase==SessionPhase.COMPLETED) Text(t("Adventure complete!","Abenteuer geschafft!"),
                style=MaterialTheme.typography.headlineSmall,modifier=Modifier.testTag("seasons-progress"))
            else NativeQuestionProgress(state.index+1,state.plan.tasks.size,language,Modifier.testTag("seasons-progress"))
            NativeActionButton(t("Listen again","Noch einmal hören"), NativeActionRole.SECONDARY, onClick=onReplay,enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-replay"))
            if(state.phase==SessionPhase.ACTIVE) {
                Text(task.question.instruction.display[language],style=MaterialTheme.typography.titleLarge,modifier=Modifier.testTag("seasons-prompt"))
                if(selection.phase==SeasonsPhase.MISSING) {
                    MissingSeasonSequence(task.question.correct,language)
                    SeasonAnswers(state,language,canAct,onAction,onOption,imageFailed,Modifier.fillMaxWidth())
                } else if(selection.phase==SeasonsPhase.CLUES) {
                    SeasonAnswers(state,language,canAct,onAction,onOption,imageFailed,Modifier.fillMaxWidth())
                } else {
                    val pictured=if(selection.phase==SeasonsPhase.PRACTICE) task.question.correct else SeasonsCycle.anchor(selection.phase,task.question)
                    if(selection.phase!=SeasonsPhase.PRACTICE) Text(SeasonsContent.season(pictured).text.display[language],modifier=Modifier.testTag("season-anchor"))
                    BoxWithConstraints {
                        if(maxWidth>=700.dp) Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                            SeasonPicture(artwork,SeasonsContent.season(pictured).spokenDescription[language],Modifier.weight(1.4f))
                            SeasonAnswers(state,language,canAct,onAction,onOption,imageFailed,Modifier.weight(1f))
                        } else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            SeasonPicture(artwork,SeasonsContent.season(pictured).spokenDescription[language],Modifier.fillMaxWidth())
                            SeasonAnswers(state,language,canAct,onAction,onOption,imageFailed,Modifier.fillMaxWidth())
                        }
                    }
                }
                if(state.current.answer==AnswerState.CORRECT) Text(task.question.correctFeedback.display[language])
                if(state.current.answer==AnswerState.RETRY_AVAILABLE) {
                    NativeSupportMessage(task.question.wrongFeedback.display[language])
                    NativeActionButton(t("Try again","Nochmal versuchen"), NativeActionRole.SECONDARY, onClick={onAction(SessionAction.Retry(AttemptId(task.id,state.current.attempts)))},enabled=canAct,modifier=Modifier.testTag("seasons-retry"))
                }
                if(state.current.support.hint) NativeSupportMessage(task.question.hint!!.display[language])
                if(!state.current.locked) NativeActionButton(t("Help","Hilfe"), NativeActionRole.SECONDARY, onClick={onAction(SessionAction.Hint(task.id))},enabled=canAct,modifier=Modifier.heightIn(min=56.dp).testTag("seasons-hint"))
                if(state.current.locked) NativeActionButton(t("Next","Weiter"), NativeActionRole.PRIMARY, onClick={onAction(SessionAction.Next(task.id))},enabled=canAct,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-next"))
            }
        }
        NativeActionButton(t("Back home","Zurück zum Start"), NativeActionRole.NAVIGATION, onClick=onHome,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-home"))
    }
}

@Composable private fun SeasonsMatchView(state: SortState, language: ContentLanguage, ready: Boolean,
    onAction: (SortAction) -> Unit, onOption: (ContentId) -> Unit, modifier: Modifier) {
    val de = language == ContentLanguage.GERMAN
    NativeActionButton(if(de) "Noch einmal hören" else "Listen again", NativeActionRole.SECONDARY,
        { onAction(SortAction.Replay) }, modifier.fillMaxWidth().heightIn(min=56.dp).testTag("seasons-replay"), ready)
    Text(if(de) "Tippe eine Jahreszeit und dann den passenden Hinweis an." else "Tap a season, then its matching clue.",
        style=MaterialTheme.typography.titleLarge, modifier=Modifier.testTag("seasons-match-prompt"))
    val selected = state.selected
    state.order.forEach { season ->
        val placement = state.placement(season)
        NativeTextChoice(SeasonsContent.season(season).text.display[language],
            onClick={onAction(SortAction.Select(season))}, enabled=ready && !placement.placed,
            modifier=Modifier.fillMaxWidth().heightIn(min=64.dp).testTag("match-season-${season.value}"))
        if (selected == season && !placement.placed) NativeSupportMessage(
            if(de) "Wähle jetzt den passenden Hinweis." else "Now choose the matching clue.")
    }
    state.rule.categories.forEach { clueId ->
        val clue = SeasonsMatch.clue(clueId)
        val matched = state.placements.any { it.placed && it.lastCategory == clueId }
        OutlinedButton(onClick={selected?.let { onAction(SortAction.Place(state.id, it, clueId, state.placement(it).attempts+1)) }},
            enabled=ready && selected != null && !matched,
            modifier=Modifier.fillMaxWidth().heightIn(min=72.dp).testTag("match-clue-${clueId.value}")) {
            Text(clue.instruction.display[language].substringBefore(if(de) " Welche Jahreszeit" else " Which season"))
        }
        OutlinedButton(onClick={onOption(clueId)}, enabled=ready,
            modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("match-speaker-${clueId.value}")) {
            Text(if(de) "Hören" else "Listen")
        }
    }
    if(selected != null && state.selectedPlacement?.support?.hint == true)
        NativeSupportMessage(SeasonsMatch.hint(state).display[language])
    NativeActionButton(if(de) "Hilfe" else "Help", NativeActionRole.SECONDARY,
        { onAction(SortAction.Hint) }, Modifier.heightIn(min=56.dp).testTag("seasons-match-hint"), ready && selected != null)
}

@Composable private fun SeasonsCombinedView(state: SeasonsCombined.State, language: ContentLanguage, ready: Boolean,
    action: (SeasonsCombined.Action) -> Unit, option: (ContentId) -> Unit) {
    val de = language == ContentLanguage.GERMAN
    NativeQuestionProgress(state.index + 1, state.tasks.size, language, Modifier.testTag("seasons-progress"))
    Text(SeasonsCombined.question(state.task, language), style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.testTag("seasons-combined-prompt"))
    Text(SeasonsContent.season(state.task.anchor).text.display[language], style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.testTag("seasons-combined-anchor").semantics { contentDescription = if (de) "Jahreszeit: ${SeasonsContent.season(state.task.anchor).text.display[language]}" else "Anchor season: ${SeasonsContent.season(state.task.anchor).text.display[language]}" })
    @Composable fun slot(label: String, selected: ContentId?, before: Boolean) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag(if (before) "seasons-before-label" else "seasons-after-label").semantics { contentDescription = label })
        Text(selected?.let { SeasonsContent.season(it).text.display[language] } ?: "?", modifier = Modifier.testTag(if (before) "seasons-before-slot" else "seasons-after-slot").semantics { contentDescription = "$label: ${selected?.let { SeasonsContent.season(it).text.display[language] } ?: if(de) "Noch nicht ausgewählt" else "Not selected"}" })
        state.task.choices.forEach { id ->
            NativeTextChoice(SeasonsContent.season(id).text.display[language], onClick = { action(if (before) SeasonsCombined.Action.Before(id) else SeasonsCombined.Action.After(id)) }, enabled = ready,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("combined-${if (before) "before" else "after"}-${id.value}"))
        }
    }
    slot(if (de) "Davor" else "Before", state.before, true)
    slot(if (de) "Danach" else "After", state.after, false)
    NativeActionButton(if (de) "Prüfen" else "Check", NativeActionRole.PRIMARY, { action(SeasonsCombined.Action.Check) }, enabled = ready && state.before != null && state.after != null, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("seasons-combined-check"))
    if (state.attempts > 0 && !state.solved) NativeSupportMessage(if (de) "Ein Platz stimmt noch nicht. Versuche es noch einmal." else "One place is not right yet. Try again.", Modifier.testTag("seasons-combined-feedback"))
    if (state.help) NativeSupportMessage(SeasonsCombined.help.display[language], Modifier.testTag("seasons-combined-help"))
    if (!state.solved) NativeActionButton(if (de) "Hilfe" else "Help", NativeActionRole.SECONDARY, { action(SeasonsCombined.Action.Help) }, enabled = ready, modifier = Modifier.heightIn(min = 56.dp).testTag("seasons-combined-help-button"))
    if (state.solved) NativeActionButton(if (de) "Weiter" else "Next", NativeActionRole.PRIMARY, { action(SeasonsCombined.Action.Next) }, enabled = ready, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("seasons-combined-next"))
}

@Composable
private fun SeasonAnswers(state:SessionState,language:ContentLanguage,ready:Boolean,action:(SessionAction)->Unit,
                          option:(ContentId)->Unit,imageFailed:Boolean,modifier:Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        state.task.question.choices.forEach { id ->
            val label=SeasonsContent.season(id).text.display[language]
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                NativeTextChoice(label=label,onClick={state.nextAttempt?.let {action(SessionAction.Answer(it,id))}},
                    enabled=ready && !imageFailed && state.current.answer==AnswerState.UNANSWERED,
                    modifier=Modifier.weight(1f).heightIn(min=64.dp).testTag("answer-${id.value}"))
                OutlinedButton(onClick={option(id)},enabled=ready,
                    modifier=Modifier.heightIn(min=64.dp).testTag("speaker-${id.value}").semantics {
                        contentDescription=if(language==ContentLanguage.GERMAN)"Anhören: $label" else "Listen: $label"
                    }) {Text(if(language==ContentLanguage.GERMAN)"Hören" else "Listen")}
            }
        }
    }
}

@Composable
private fun SeasonPicture(bitmap:ImageBitmap?,description:String,modifier:Modifier) {
    if(bitmap!=null) Image(bitmap,description,modifier.aspectRatio(bitmap.width.toFloat()/bitmap.height).testTag("season-artwork"),contentScale=ContentScale.Fit)
    else Box(modifier.aspectRatio(4f/3f))
}


@Composable
private fun SeasonModes(selected: SeasonsPhase?, language: ContentLanguage, ready: Boolean, onPhase: (SeasonsPhase)->Unit) {
    SeasonsPhase.entries.chunked(2).forEach {row ->
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach {phase ->
            FilterChip(selected=selected==phase,onClick={onPhase(phase)},enabled=ready,
                label={Text(phase.title.display[language])},modifier=Modifier.weight(1f).heightIn(min=48.dp).testTag(phase.tag))
        }}
    }
}

@Composable
private fun PlacedSeasons(state: SeasonsOrderState, images: Map<ContentId,ImageBitmap>, language: ContentLanguage) {
    Text(if(language==ContentLanguage.GERMAN) "${state.index} von 4 Jahreszeiten eingeordnet" else "${state.index} of 4 seasons placed",
        modifier=Modifier.testTag("seasons-placed-count"))
    state.placed.chunked(2).forEach {row ->
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {row.forEach {id ->
            Column(Modifier.weight(1f).testTag("placed-${id.value}")) {
                images[id]?.let {Image(it,null,Modifier.fillMaxWidth().aspectRatio(4f/3f),contentScale=ContentScale.Fit)}
                Text("${state.placed.indexOf(id)+1}. ${SeasonsContent.season(id).text.display[language]}")
            }
        }}
    }
}

/** Text supplies season identity; the gap is explicit without a colour cue or answer artwork. */
@Composable
private fun MissingSeasonSequence(missing: ContentId, language: ContentLanguage) {
    Column(Modifier.fillMaxWidth().testTag("seasons-missing-sequence"), verticalArrangement=Arrangement.spacedBy(8.dp)) {
        SeasonIds.canonicalOrder.forEachIndexed { index, id ->
            val gap = id == missing
            val name = SeasonsContent.season(id).text.display[language]
            Text(if(gap) "${index+1}. ?" else "${index+1}. $name",
                style=MaterialTheme.typography.titleLarge,
                modifier=Modifier.fillMaxWidth().testTag("seasons-missing-slot-${index+1}").clearAndSetSemantics {
                    contentDescription = if(language==ContentLanguage.GERMAN)
                        "Position ${index+1}: ${if(gap) "Fehlende Jahreszeit" else name}"
                    else "Position ${index+1}: ${if(gap) "Missing season" else name}"
                })
        }
    }
}
