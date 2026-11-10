package com.bellfamily.bastischool.ui.memory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.memory.*
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.ui.common.*
import com.bellfamily.bastischool.ui.vocabulary.VocabularyArtwork

@Composable
fun MemoryPairsChooser(language: ContentLanguage, onStart:(MemoryConfig)->Unit, modifier: Modifier=Modifier) {
    val de=language==ContentLanguage.GERMAN
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("memory-chooser"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        NativeActivityTitle(MemoryPairs.title.display[language])
        Text(if(de)"Decke zwei Karten auf und finde die Paare. Wähle dein Spielfeld." else "Turn over two cards and find the pairs. Choose your board.",style=MaterialTheme.typography.bodyLarge)
        MemoryMode.entries.forEach { mode ->
            Surface(shape=RoundedCornerShape(20.dp),color=Color(0xFFF1F6EA)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    if(mode==MemoryMode.ANIMALS)Row(Modifier.height(100.dp)) {
                        listOf("dinosaur","crocodile").forEach { BundledArtwork("Animals/canonical/$it.png",Modifier.weight(1f).fillMaxHeight(),256) }
                    }else Row(Modifier.height(90.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("3",Modifier.weight(1f),style=MaterialTheme.typography.displayLarge)
                        DotGroup(3,Modifier.weight(1f).fillMaxHeight())
                    }
                    Text(mode.title.display[language],style=MaterialTheme.typography.headlineSmall)
                    Text(if(mode==MemoryMode.ANIMALS) {
                        if(de)"Finde zweimal dasselbe Tier." else "Find two pictures of the same animal."
                    }else if(de)"Finde zu jeder Zahl die passende Punktmenge." else "Match each number with its dot group.")
                    (2..4).forEach { pairs ->
                        NativeActionButton(if(de)"${pairs*2} Karten · $pairs Paare" else "${pairs*2} cards · $pairs pairs",NativeActionRole.PRIMARY,{onStart(MemoryConfig(mode,pairs))},
                            Modifier.fillMaxWidth().testTag("memory-start-${mode.key}-$pairs"))
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryPairsScreen(vm: MemoryPairsViewModel, config: MemoryConfig, language: ContentLanguage, onBoards:()->Unit,
    onHome:()->Unit, modifier: Modifier=Modifier, onPop:(String)->Unit={}, onStopSound:()->Unit={}) {
    MemoryPairsContent(vm.state,config,language,vm.busy,vm.saveFailed,vm.audioFailed,vm::reveal,vm::listen,vm::next,
        {onStopSound();vm.replay()},vm::help,{onStopSound();vm.again()},vm::retrySave,onBoards,onHome,modifier,onPop)
}

@Composable
internal fun MemoryPairsContent(state: MemoryPairs.State?,config: MemoryConfig,language: ContentLanguage,busy: Boolean,saveFailed: Boolean,audioFailed: Boolean,
    onReveal:(Int)->Unit,onListen:(Int)->Unit,onNext:()->Unit,onReplay:()->Unit,onHelp:()->Unit,onAgain:()->Unit,onRetry:()->Unit,
    onBoards:()->Unit,onHome:()->Unit,modifier: Modifier=Modifier,onPop:(String)->Unit={}) {
    val de=language==ContentLanguage.GERMAN
    fun t(en:String,deText:String)=if(de)deText else en
    val ready=state!=null&&state.config==config&&state.language==language&&!busy&&!saveFailed
    if(state?.completed==true&&state.config==config) {
        Column(modifier.fillMaxSize()) {
            NativeCompletionScreen(state.id.value,language,MemoryPairs.complete.display[language],ready,"memory-",onReplay,onAgain,onHome,onPop,
                Modifier.weight(1f),saveFailed,onRetry,audioFailed)
            NativeActionButton(t("Choose a board","Spielfeld wählen"),NativeActionRole.NAVIGATION,onBoards,Modifier.fillMaxWidth().padding(12.dp).testTag("memory-boards"))
        }
        return
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("memory-game"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        NativeActivityTitle(MemoryPairs.title.display[language])
        Text(config.mode.title.display[language],style=MaterialTheme.typography.titleLarge)
        Text(MemoryPairs.instruction.display[language])
        NativeActionButton(t("Listen again","Noch einmal hören"),NativeActionRole.SECONDARY,onReplay,Modifier.fillMaxWidth().testTag("memory-replay"),ready)
        if(state==null||state.config!=config)Text(t("Opening your board…","Dein Spielfeld wird geöffnet…"))else {
            Text(t("${state.solvedPairs} of ${config.pairs} pairs found","${state.solvedPairs} von ${config.pairs} Paaren gefunden"),Modifier.testTag("memory-progress"),style=MaterialTheme.typography.titleMedium)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val fontScale=LocalDensity.current.fontScale
                val columns=if(config.pairs==2)2 else (maxWidth.value/(110f*fontScale.coerceAtLeast(1f))).toInt().coerceIn(2,4)
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    state.deck.indices.toList().chunked(columns).forEach { row ->
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            row.forEach { slot ->
                                val card=state.deck[slot];val shown=state.visible(slot);val matched=state.matchedMask and (1 shl slot)!=0
                                val name=MemoryPairs.label(card).display[language]
                                Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
                                    Surface(onClick={onReveal(slot)},enabled=ready&&!shown&&state.phase==MemoryPairs.Phase.PLAYING,
                                        modifier=Modifier.fillMaxWidth().height(130.dp*fontScale.coerceAtLeast(1f)).testTag("memory-card-$slot").semantics {
                                            contentDescription=if(shown)t("Card ${slot+1}: $name","Karte ${slot+1}: $name")else t("Card ${slot+1}, face down","Karte ${slot+1}, verdeckt")
                                            stateDescription=if(matched)t("Pair found","Paar gefunden")else if(shown)t("Face up","Aufgedeckt")else t("Face down","Verdeckt")
                                        },shape=RoundedCornerShape(18.dp),color=if(!shown)Color(0xFF245A67)else if(matched)Color(0xFFE4F1DD)else Color(0xFFFFFBEE),
                                        border=BorderStroke(if(matched)3.dp else 2.dp,if(matched)Color(0xFF557A45)else Color(0xFF78968E))) {
                                        Column(Modifier.fillMaxSize().padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                                                if(!shown)CardBack(Modifier.fillMaxSize())else when(card.face) {
                                                    MemoryFace.PICTURE -> Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally) {
                                                        BundledArtwork(VocabularyArtwork.paths.getValue(card.pair),Modifier.fillMaxWidth().weight(1f),256)
                                                        Text(name,style=MaterialTheme.typography.titleMedium)
                                                    }
                                                    MemoryFace.NUMERAL -> Text(name,style=MaterialTheme.typography.displayLarge)
                                                    MemoryFace.QUANTITY -> DotGroup(MemoryPairs.amount(card.pair),Modifier.fillMaxSize())
                                                }
                                            }
                                            Text(if(matched)t("Pair found","Paar gefunden")else " ",style=MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                    // Reserve the same row space before and after a flip; cards never move.
                                    val listenModifier=if(shown)Modifier.testTag("memory-listen-$slot")else Modifier.alpha(0f).clearAndSetSemantics {}
                                    NativeActionButton(t("Listen","Anhören"),NativeActionRole.SECONDARY,{onListen(slot)},Modifier.fillMaxWidth().then(listenModifier),ready&&shown)
                                }
                            }
                            repeat(columns-row.size){Spacer(Modifier.weight(1f))}
                        }
                    }
                }
            }
            if(state.phase!=MemoryPairs.Phase.PLAYING) {
                NativeSupportMessage(MemoryPairsAudio.current(state).display[language],Modifier.testTag("memory-feedback"))
                NativeActionButton(if(state.phase==MemoryPairs.Phase.MATCH)t("Continue","Weiter")else t("Turn cards back over","Karten wieder umdrehen"),NativeActionRole.PRIMARY,onNext,Modifier.fillMaxWidth().testTag("memory-next"),ready)
            }else NativeActionButton(t("Show me a pair","Zeig mir ein Paar"),NativeActionRole.SECONDARY,onHelp,Modifier.fillMaxWidth().testTag("memory-help"),ready)
        }
        if(saveFailed) {
            NativeSupportMessage(t("Your saved board is kept. Please try again.","Dein gespeichertes Spielfeld bleibt erhalten. Versuche es erneut."))
            NativeActionButton(t("Try again","Erneut versuchen"),NativeActionRole.SECONDARY,onRetry,Modifier.testTag("memory-retry"),!busy)
        }
        if(audioFailed)Text(t("Speech is unavailable. You can keep playing. Check installed offline voices.","Die Sprachausgabe ist nicht verfügbar. Du kannst weiterspielen. Prüfe installierte Offline-Stimmen."))
        NativeActionButton(t("Choose a board","Spielfeld wählen"),NativeActionRole.NAVIGATION,onBoards,Modifier.testTag("memory-boards"))
        NativeActionButton(t("Back home","Zurück zum Start"),NativeActionRole.NAVIGATION,onHome)
    }
}

@Composable
private fun CardBack(modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val center=Offset(size.width/2,size.height/2);val r=minOf(size.width,size.height)*.30f
        drawCircle(Color(0xFFDBEDE3),r,center,style=Stroke(3.dp.toPx()))
        listOf(-.35f,.35f).forEach { x ->
            drawOval(Color(0xFFF4D79E),topLeft=Offset(center.x+x*r-r*.23f,center.y-r*.45f),size=androidx.compose.ui.geometry.Size(r*.46f,r*.8f))
        }
        drawLine(Color(0xFFDBEDE3),center+Offset(-r*.2f,r*.55f),center+Offset(r*.2f,r*.55f),3.dp.toPx())
    }
}
@Composable
private fun DotGroup(count: Int,modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics {}) {
        val side=minOf(size.width,size.height)*.2f
        val positions=when(count) {
            1->listOf(0f to 0f)
            2->listOf(-1f to -1f,1f to 1f)
            3->listOf(-1f to -1f,0f to 0f,1f to 1f)
            4->listOf(-1f to -1f,1f to -1f,-1f to 1f,1f to 1f)
            else->listOf(-1f to -1f,1f to -1f,0f to 0f,-1f to 1f,1f to 1f)
        }
        positions.forEach { (x,y)->drawCircle(Color(0xFF315D75),side*.38f,Offset(size.width/2+x*side,size.height/2+y*side)) }
    }
}
