package com.bellfamily.bastischool.ui.tellme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.scenedescription.SceneCategoryId
import com.bellfamily.bastischool.learning.scenedescription.SceneId
import com.bellfamily.bastischool.learning.tellme.*
import com.bellfamily.bastischool.ui.common.*

/** Authored display content only. This screen never listens, grades, speaks or writes progress. */
@Composable
fun TellMeScreen(
    flow: TellMeFlow,
    state: TellMeState,
    language: ContentLanguage,
    artwork: ImageBitmap?,
    loading: Boolean,
    onCategory: (SceneCategoryId) -> Unit,
    onAdvance: (SceneId, TellMeStage) -> Unit,
    onHelp: () -> Unit,
    onGrownUps: () -> Unit,
    onAgain: () -> Unit,
    onChooseAnother: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val de = language == ContentLanguage.GERMAN
    fun text(en: String, german: String) = if (de) german else en
    BoxWithConstraints(modifier.fillMaxSize()) {
        // A complete image can fit inside even a short viewport after scrolling to it.
        val imageHeightLimit = (maxHeight - 32.dp).coerceIn(96.dp, 420.dp)
        // New pictures/phases start at the top; revealing support preserves the current viewport.
        key(state.category, state.index, state.stage) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.category == null) {
                    Text(text("Tell Me!", "Erzähl mal!"), style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag("tellme-landing"))
                    Text(text("Choose an adventure and tell me what you can see.",
                        "Wähle ein Abenteuer und erzähle, was du sehen kannst."))
                    flow.categories.forEach { category ->
                        NativeActionButton(category.display[language], NativeActionRole.NAVIGATION,
                            { onCategory(category.id) }, Modifier.fillMaxWidth().testTag("tellme-category-${category.id.value}"))
                    }
                } else if (state.stage == TellMeStage.COMPLETE) {
                    Text(text("Great talking!", "Toll erzählt!"), style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag("tellme-completion"))
                    Text(text("You explored all 9 pictures in this adventure.",
                        "Du hast alle 9 Bilder in diesem Abenteuer entdeckt."))
                    NativeActionButton(text("Again", "Nochmal"), NativeActionRole.PRIMARY, onAgain,
                        Modifier.fillMaxWidth().testTag("tellme-again"))
                    NativeActionButton(text("Choose another adventure", "Anderes Abenteuer wählen"), NativeActionRole.NAVIGATION,
                        onChooseAnother, Modifier.fillMaxWidth().testTag("tellme-categories"))
                } else {
                    val scene = requireNotNull(flow.scene(state))
                    val support = flow.support(scene, language)
                    Text(text("${state.index + 1} of 9", "${state.index + 1} von 9"), Modifier.testTag("tellme-progress"))
                    scene.title[language]?.let { Text(it, Modifier.testTag("tellme-title"), style = MaterialTheme.typography.titleLarge) }
                    // Fit preserves the entire composition. Adjacent title is the minimal authored image label.
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        Box(Modifier.fillMaxWidth().height((maxWidth * 0.75f).coerceAtMost(imageHeightLimit)), contentAlignment = Alignment.Center) {
                            if (artwork != null) Image(artwork, scene.title[language], Modifier.fillMaxSize().testTag("tellme-artwork"),
                                contentScale = ContentScale.Fit)
                            else Text(if (loading) text("Loading picture…", "Bild wird geladen…") else
                                text("The picture is unavailable right now.", "Das Bild ist gerade nicht verfügbar."),
                                Modifier.padding(16.dp).testTag(if (loading) "tellme-image-loading" else "tellme-image-unavailable"))
                        }
                    }
                    if (state.stage == TellMeStage.TALK) {
                        Text(text("Tell me about the picture.", "Erzähl mir etwas über das Bild."), style = MaterialTheme.typography.titleMedium)
                        support.prompt?.let { Text(it, Modifier.testTag("tellme-prompt")) }
                        if (support.hasHelp) NativeActionButton(text("Help", "Hilfe"), NativeActionRole.SECONDARY, onHelp,
                            Modifier.fillMaxWidth().testTag("tellme-help"))
                    }
                    if (state.help) {
                        support.starter?.let {
                            Text(text("You could start with…", "Du könntest anfangen mit…"))
                            NativeSupportMessage(it, Modifier.fillMaxWidth().testTag("tellme-starter"))
                        }
                        if (support.words.isNotEmpty()) {
                            Text(text("Words that might help", "Wörter, die helfen können"))
                            NativeSupportMessage(support.words.joinToString("\n"), Modifier.fillMaxWidth().testTag("tellme-words"))
                        }
                    }
                    if (state.stage == TellMeStage.MODEL) support.model?.let {
                        Text(text("Another way to say more", "So kannst du noch mehr sagen"), style = MaterialTheme.typography.titleMedium)
                        Text(it, Modifier.testTag("tellme-model"))
                    }
                    NativeActionButton(if (state.stage == TellMeStage.TALK) text("Continue", "Weiter") else text("Next picture", "Nächstes Bild"),
                        NativeActionRole.PRIMARY, { onAdvance(scene.id, state.stage) },
                        Modifier.fillMaxWidth().testTag("tellme-continue"))
                    if (support.hasGrownUps) {
                        NativeActionButton(text("For grown-ups", "Für Erwachsene"), NativeActionRole.SECONDARY, onGrownUps,
                            Modifier.fillMaxWidth().testTag("tellme-grownups").semantics {
                                stateDescription = if (state.grownUps) text("Expanded", "Ausgeklappt") else text("Collapsed", "Eingeklappt")
                            })
                        if (state.grownUps) Column(Modifier.testTag("tellme-adult"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            support.principle?.let { Text(it) }
                            support.focus?.let { Text(it) }
                            support.expansion?.let { (child, adult) ->
                                Text(text("If your child says: $child", "Wenn dein Kind sagt: $child"))
                                Text(text("You could expand it to: $adult", "Du könntest es erweitern zu: $adult"))
                            }
                        }
                    }
                }
                NativeActionButton(text("Home", "Startseite"), NativeActionRole.NAVIGATION, onHome,
                    Modifier.fillMaxWidth().testTag("tellme-home"))
            }
        }
    }
}
