package com.bellfamily.bastischool.ui.months

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.months.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun MonthsSeasonsMatchScreen(state: SessionState?, artwork: Map<ContentId, ImageBitmap>,
    language: ContentLanguage, busy: Boolean, saveFailed: Boolean, artworkFailed: Boolean,
    audioFailed: Boolean, onAction: (SessionAction) -> Unit, onReplay: () -> Unit,
    onAgain: () -> Unit, onRetry: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier, onPop: (String) -> Unit = {}) {
    val de = language == ContentLanguage.GERMAN
    fun t(en: String, german: String) = if (de) german else en
    val ready = state != null && state.language == language && !busy && !saveFailed && !artworkFailed
    var guideOpen by rememberSaveable(state?.plan?.id?.value) { mutableStateOf(false) }

    if (state?.phase == SessionPhase.COMPLETED) {
        NativeCompletionScreen(state.plan.id.value, language, state.plan.completionText.display[language],
            ready, "months-seasons-", onReplay, onAgain, onHome, onPop, modifier,
            saveFailed || artworkFailed, onRetry, audioFailed)
        return
    }

    Column(modifier.fillMaxSize().background(Color(0xFFF8F5FC)).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(MonthsMode.SEASONS_MATCH.title.display[language])
        if (saveFailed || artworkFailed) {
            NativeSupportMessage(t("This activity could not be opened or saved. Your saved work is kept.",
                "Diese Aktivität konnte nicht geöffnet oder gespeichert werden. Gespeicherte Arbeit bleibt erhalten."))
            NativeActionButton(t("Try again", "Erneut versuchen"), NativeActionRole.SECONDARY, onRetry,
                Modifier.testTag("months-seasons-retry-load"), !busy)
        }
        if (audioFailed) NativeSupportMessage(t("Speech is unavailable. Check the installed offline voices.",
            "Die Sprachausgabe ist nicht verfügbar. Prüfe die installierten Offline-Stimmen."))

        if (state == null) {
            if (!saveFailed && !artworkFailed) Text(t("Opening…", "Wird geöffnet…"))
        } else {
            NativeQuestionProgress(state.index + 1, state.plan.tasks.size, language,
                Modifier.testTag("months-seasons-progress"))
            NativeActionButton(t("Listen again", "Noch einmal hören"), NativeActionRole.SECONDARY,
                onReplay, Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("months-seasons-replay"), ready)
            if (state.phase == SessionPhase.ACTIVE) {
                val monthId = MonthsSeasonsMatch.month(state.task.question)
                val month = MonthContent.month(monthId)
                NativeSupportMessage(t("Place this month in its season group.", "Ordne diesen Monat seiner Jahreszeitengruppe zu."),
                    Modifier.testTag("months-seasons-prompt"))
                Text(month.text.display[language], style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp)
                        .semantics { contentDescription = t("Month to sort: ${month.text.display[language]}", "Monat zum Einordnen: ${month.text.display[language]}") }
                        .testTag("months-seasons-month-tile"))
                Text(t("Choose a season group", "Wähle eine Jahreszeitengruppe"), style = MaterialTheme.typography.titleMedium)

                val assigned = state.plan.tasks.take(state.index).mapIndexedNotNull { index, task ->
                    if (state.progress[index].answer == AnswerState.CORRECT)
                        MonthsSeasonsMatch.month(task.question) to task.question.correct else null
                } + if (state.current.answer == AnswerState.CORRECT)
                    listOf(MonthsSeasonsMatch.month(state.task.question) to state.task.question.correct) else emptyList()
                val columns = when {
                    androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.7f -> 1
                    else -> 2
                }
                SeasonIds.canonicalOrder.chunked(columns).forEach { rowSeasons ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top) {
                        rowSeasons.forEach { seasonId ->
                            val season = MonthContent.season(seasonId)
                            val names = assigned.filter { it.second == seasonId }
                                .map { MonthContent.month(it.first).text.display[language] }
                            val description = buildString {
                                append(t("Place in ${season.text.display[language]}", "In ${season.text.display[language]} einordnen"))
                                if (names.isNotEmpty()) append(t(". Already grouped: ", ". Schon zugeordnet: ") + names.joinToString(", "))
                                else append(t(". No months grouped yet", ". Noch keine Monate zugeordnet"))
                            }
                            OutlinedButton(onClick = {
                                state.nextAttempt?.let { onAction(SessionAction.Answer(it, seasonId)) }
                            }, enabled = ready && state.current.answer == AnswerState.UNANSWERED,
                                modifier = Modifier.weight(1f).heightIn(min = 160.dp).testTag("months-season-target-${seasonId.value}")
                                    .semantics { contentDescription = description }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    artwork[seasonId]?.let { Image(it, null, Modifier.fillMaxWidth().height(76.dp), contentScale = ContentScale.Crop) }
                                    Text(season.text.display[language], style = MaterialTheme.typography.titleMedium)
                                    Text(if (names.isEmpty()) t("Place month here", "Monat hier einordnen") else names.joinToString(" · "),
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        if (rowSeasons.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                if (state.current.answer == AnswerState.CORRECT) {
                    NativeSupportMessage(state.task.question.correctFeedback.display[language], Modifier.testTag("months-seasons-correct"))
                    NativeActionButton(t("Continue when ready", "Weiter, wenn du bereit bist"), NativeActionRole.PRIMARY,
                        { onAction(SessionAction.Next(state.task.id)) }, Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            .testTag("months-seasons-next"), ready)
                } else if (state.current.answer == AnswerState.RETRY_AVAILABLE) {
                    NativeSupportMessage(state.task.question.wrongFeedback.display[language], Modifier.testTag("months-seasons-retry-message"))
                    NativeActionButton(t("Try another season", "Versuche eine andere Jahreszeit"), NativeActionRole.SECONDARY,
                        { onAction(SessionAction.Retry(AttemptId(state.task.id, state.current.attempts))) },
                        Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("months-seasons-retry-answer"), ready)
                }
                if (state.current.support.hint) NativeSupportMessage(state.task.question.hint!!.display[language], Modifier.testTag("months-seasons-hint"))
                if (!state.current.locked) {
                    NativeActionButton(t("Show the season groups", "Jahreszeitengruppen ansehen"), NativeActionRole.SECONDARY,
                        { guideOpen = !guideOpen; if (guideOpen) onAction(SessionAction.Hint(state.task.id)) },
                        Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("months-seasons-guide"), ready)
                }
                if (guideOpen) SeasonMonthGuide(language, Modifier.testTag("months-seasons-guide-content"))
            }
        }
        NativeActionButton(t("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
            Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("months-seasons-home"))
    }
}

@Composable
private fun SeasonMonthGuide(language: ContentLanguage, modifier: Modifier = Modifier) {
    val de = language == ContentLanguage.GERMAN
    Column(modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)) {
        SeasonIds.canonicalOrder.forEach { seasonId ->
            val season = MonthContent.season(seasonId)
            val months = MonthContent.definitions.filter { it.season == seasonId }
                .sortedBy { MonthIds.canonicalOrder.indexOf(it.id) }
                .let { if (seasonId == SeasonIds.WINTER) listOf(it.last()) + it.dropLast(1) else it }
                .joinToString(", ") { it.text.display[language] }
            Text("${season.text.display[language]}: $months", style = MaterialTheme.typography.bodyLarge)
        }
        Text(if (de) "Meteorologische Jahreszeiten in Deutschland" else "Meteorological seasons in Germany",
            style = MaterialTheme.typography.bodySmall)
    }
}
