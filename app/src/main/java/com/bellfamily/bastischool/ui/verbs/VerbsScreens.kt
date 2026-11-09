package com.bellfamily.bastischool.ui.verbs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.verbs.*
import com.bellfamily.bastischool.ui.common.*

@Composable
fun VerbLibraryScreen(language:ContentLanguage,onLesson:(String)->Unit,modifier:Modifier=Modifier) {
    LazyColumn(modifier.fillMaxSize().testTag("native-verb-library"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {NativeActivityTitle(if(language==ContentLanguage.GERMAN)"Verben entdecken" else "Verb Explorer")}
        item {Text(if(language==ContentLanguage.GERMAN)"Wähle eine Aktion. Schau sie dir an, hör zu und mach mit." else "Choose an action. Watch, listen and try it together.")}
        items(VerbContent.lessons,key={it.id.value}) {lesson ->
            OutlinedCard(onClick={onLesson(lesson.id.value)},modifier=Modifier.fillMaxWidth().testTag("native-verb-card-${lesson.id.value}")) {
                Row(Modifier.padding(12.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    BundledArtwork(VerbContent.animals.getValue(lesson.animal).artwork,Modifier.size(76.dp),192)
                    Column(Modifier.weight(1f)) {Text(lesson.label.display[language],style=MaterialTheme.typography.titleLarge)
                        Text(lesson.label.display[if(language==ContentLanguage.GERMAN)ContentLanguage.ENGLISH else ContentLanguage.GERMAN])}
                }
            }
        }
    }
}

@Composable
fun VerbLessonScreen(lesson:VerbLesson,language:ContentLanguage,fromQuiz:Boolean,audioFailed:Boolean,
    onListen:(Boolean)->Unit,onInterrupt:()->Unit,onReturn:()->Unit,modifier:Modifier=Modifier,onEnter:()->Unit={}) {
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("native-verb-lesson"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(lesson.label.display[language])
        Text(lesson.label.display[if(language==ContentLanguage.GERMAN)ContentLanguage.ENGLISH else ContentLanguage.GERMAN])
        VerbPlayer(lesson,language,onInterrupt)
        LaunchedEffect(lesson.id,language){onEnter()}
        Text(t("What it means","Was bedeutet das?"),style=MaterialTheme.typography.titleLarge)
        Text(lesson.explanation.display[language])
        Text(t("Example","Beispiel"),style=MaterialTheme.typography.titleLarge)
        Text(lesson.example.display[language])
        Text(t("Try it!","Mach mit!"),style=MaterialTheme.typography.titleLarge)
        Text(t("Can you act it out or make the sound?","Kannst du die Bewegung vormachen oder das Geräusch nachmachen?"))
        if(lesson.sound) {
            var guide by rememberSaveable(lesson.id.value){mutableStateOf(lesson.motion.mode=="copy")}
            NativeActionButton(t("Make the sound together","Das Geräusch gemeinsam vormachen"),NativeActionRole.SECONDARY,{guide=!guide})
            if(guide){Text(t("Grown-up, model the animal's sound. Listen, then try it together.","Mach als erwachsene Begleitperson das Tiergeräusch vor. Hört zu und versucht es gemeinsam."))
                NativeActionButton(t("Hear the instructions","Anleitung anhören"),NativeActionRole.SECONDARY,{onListen(true)})}
        }
        NativeActionButton(t("Listen","Anhören"),NativeActionRole.SECONDARY,{onListen(false)},Modifier.fillMaxWidth().testTag("native-verb-listen"))
        if(audioFailed)Text(t("Speech is unavailable. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(if(fromQuiz)t("Back to question","Zurück zur Frage") else t("All verbs","Alle Verben"),NativeActionRole.NAVIGATION,onReturn,
            Modifier.fillMaxWidth().testTag("native-verb-return"))
    }
}

@Composable
fun VerbQuizScreen(vm:VerbsQuizViewModel,language:ContentLanguage,onLesson:(String)->Unit,onHome:()->Unit,
    modifier:Modifier=Modifier,onPop:(String)->Unit={}) {
    val state=vm.state;val ready=state!=null&&state.language==language&&!vm.busy&&!vm.saveFailed
    fun t(en:String,de:String)=if(language==ContentLanguage.GERMAN)de else en
    if(state?.phase==SessionPhase.COMPLETED){NativeCompletionScreen(state.plan.id.value,language,state.plan.completionText.display[language],ready,"native-verbs-",vm::replay,vm::again,onHome,onPop,modifier,vm.saveFailed,vm::retrySave,vm.audioFailed);return}
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(t("Animal Actions","Tieraktionen"))
        var instructions by rememberSaveable {mutableStateOf(false)}
        TextButton({instructions=!instructions}){Text(t("How to play","So geht es"))}
        if(instructions){Text(VerbContent.quizIntroduction.display[language]);NativeActionButton(t("Hear the instructions","Anleitung anhören"),NativeActionRole.SECONDARY,vm::instructions,enabled=ready)}
        if(state==null)Text(t("Opening…","Wird geöffnet…"))else {
            val lesson=VerbContent.lesson(state.task.question)
            NativeQuestionProgress(state.index+1,state.plan.tasks.size,language)
            Text(state.task.question.instruction.display[language],Modifier.testTag("native-verbs-prompt"),style=MaterialTheme.typography.headlineSmall)
            NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,vm::replay,Modifier.testTag("native-verbs-replay"),ready)
            NativeActionButton(t("Learn this verb","Dieses Verb lernen"),NativeActionRole.SECONDARY,{
                if(!state.current.locked)vm.action(SessionAction.Hint(state.task.id))
                onLesson(lesson.id.value)
            },Modifier.testTag("native-verbs-learn"),ready)
            NativeChoiceRows(state.task.question.choices) {id ->
                val animal=VerbContent.animals.values.single{it.id==id}
                Column(Modifier.weight(1f)) {
                    OutlinedCard(onClick={state.nextAttempt?.let{vm.action(SessionAction.Answer(it,id))}},enabled=ready&&state.current.answer==AnswerState.UNANSWERED,
                        modifier=Modifier.fillMaxWidth().testTag("native-verbs-answer-${id.value}")) {
                        Column(Modifier.padding(12.dp)) {BundledArtwork(animal.artwork,Modifier.fillMaxWidth().height(110.dp),256);Text(animal.label.display[language])}
                    }
                    NativeActionButton(t("Hear ${animal.label.display.en}","${animal.label.display.de} anhören"),NativeActionRole.SECONDARY,{vm.option(id)},Modifier.fillMaxWidth().testTag("native-verbs-speaker-${id.value}"),ready)
                }
            }
            when(state.current.answer) {
                AnswerState.RETRY_AVAILABLE->{NativeSupportMessage(state.task.question.wrongFeedback.display[language]);NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,{vm.action(SessionAction.Retry(AttemptId(state.task.id,state.current.attempts)))},Modifier.testTag("native-verbs-retry"),ready)}
                AnswerState.CORRECT,AnswerState.INCORRECT->{NativeSupportMessage(if(state.current.answer==AnswerState.CORRECT)state.task.question.correctFeedback.display[language] else state.task.question.wrongFeedback.display[language]);NativeActionButton(t("Next","Weiter"),NativeActionRole.PRIMARY,{vm.action(SessionAction.Next(state.task.id))},Modifier.fillMaxWidth().testTag("native-verbs-next"),ready)}
                else->Unit
            }
        }
        if(vm.saveFailed){NativeSupportMessage(t("Your saved work is kept. Please try again.","Deine gespeicherte Arbeit bleibt erhalten. Versuche es erneut."));NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,vm::retrySave,enabled=!vm.busy)}
        if(vm.audioFailed)Text(t("Speech is unavailable. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
    }
}
