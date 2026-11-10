package com.bellfamily.bastischool.learning.memory

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import com.bellfamily.bastischool.learning.progress.*
import com.bellfamily.bastischool.learning.vocabulary.VocabularyContent
import com.bellfamily.bastischool.learning.bonds.NumberBonds
import java.util.Collections
import java.util.Random

enum class MemoryMode(val key: String, val title: ContentText) {
    ANIMALS("animals", ContentText.plain("Animal pictures", "Tierbilder")),
    NUMBERS("numbers", ContentText.plain("Numbers and groups", "Zahlen und Mengen"))
}
data class MemoryConfig(val mode: MemoryMode, val pairs: Int) {
    init { require(pairs in 2..4) }
    val key get() = "${mode.key}-$pairs"
}
enum class MemoryFace { PICTURE, NUMERAL, QUANTITY }
/** Relationship identity is independent of the displayed language, artwork and card position. */
data class MemoryCard(val pair: ContentId, val face: MemoryFace, val copy: Int) {
    init { require(copy in 0..1) }
    val key get() = "${pair.value}-$copy"
}
object MemoryPairs {
    const val REVISION = 1
    val version = ContentVersion(1, 1)
    val title = ContentText.plain("Memory Pairs", "Paare merken")
    val instruction = ContentText.plain("Turn over two cards. Find the pairs. Take your time.", "Decke zwei Karten auf. Finde die Paare. Lass dir Zeit.")
    val complete = ContentText.plain("You found every pair!", "Du hast alle Paare gefunden!")
    val matched = ContentText.plain("You found a pair!", "Du hast ein Paar gefunden!")
    val different = ContentText.plain("These belong to different pairs. Look at both, then turn them back over.", "Diese gehören zu verschiedenen Paaren. Schau sie dir an und drehe sie dann wieder um.")
    val model = ContentText.plain("These two belong together. Look at them, then turn them back over and try finding them.", "Diese beiden gehören zusammen. Schau sie dir an. Drehe sie dann wieder um und versuche, sie zu finden.")
    fun pool(mode: MemoryMode): List<ContentId> = when(mode) {
        MemoryMode.ANIMALS -> VocabularyContent.items.map { it.id }
        MemoryMode.NUMBERS -> (1..5).map(NumberBonds::countId)
    }
    fun amount(id: ContentId): Int = (1..5).single { NumberBonds.countId(it) == id }
    fun label(card: MemoryCard): ContentText = when(card.face) {
        MemoryFace.PICTURE -> VocabularyContent.item(card.pair).text
        MemoryFace.NUMERAL -> amount(card.pair).let { ContentText.plain("$it", "$it") }
        MemoryFace.QUANTITY -> amount(card.pair).let { ContentText.plain(if(it==1) "One dot" else "$it dots", if(it==1) "Ein Punkt" else "$it Punkte") }
    }
    fun activity(config: MemoryConfig) = ActivityId("activity.memory.${config.mode.key}")
    fun skill(config: MemoryConfig) = SkillId(if(config.mode==MemoryMode.ANIMALS) "skill.memory.picture_pairs" else "skill.math.number_quantity_pairs")
    fun context(config: MemoryConfig) = LearningContextId("context.memory.${config.mode.key}.pairs_${config.pairs}")
    enum class Phase { PLAYING, MATCH, DIFFERENT, MODEL, COMPLETED }
    data class Result(val attempts: Int = 0, val wrong: Int = 0, val solved: Boolean = false, val support: SupportUse = SupportUse()) {
        init { require(attempts in 0..10000 && wrong in 0..attempts && attempts == wrong + if(solved) 1 else 0) }
    }
    data class State(val id: SessionId, val config: MemoryConfig, val deck: List<MemoryCard>, val language: ContentLanguage,
        val revealed: List<Int> = emptyList(), val matchedMask: Int = 0, val phase: Phase = Phase.PLAYING,
        val results: List<Result> = List(config.pairs) { Result() }, val exposures: List<Int> = List(config.pairs*2) { 0 },
        val acknowledged: Boolean = false) {
        val pairIds get() = deck.map { it.pair }.distinct().sortedBy { it.value }
        val solvedPairs get() = results.count { it.solved }
        val completed get() = phase == Phase.COMPLETED
        fun visible(slot: Int) = slot in revealed || matchedMask and (1 shl slot) != 0
        fun ordinal(slot: Int) = pairIds.indexOf(deck[slot].pair)
        init {
            require(deck.size == config.pairs*2 && deck.map { it.key }.distinct().size == deck.size)
            require(pairIds.size == config.pairs && pairIds.all { it in pool(config.mode) })
            pairIds.forEach { id ->
                val cards = deck.filter { it.pair == id }.sortedBy { it.copy }
                require(cards.map { it.copy } == listOf(0,1))
                require(cards.map { it.face } == if(config.mode==MemoryMode.ANIMALS) listOf(MemoryFace.PICTURE,MemoryFace.PICTURE) else listOf(MemoryFace.NUMERAL,MemoryFace.QUANTITY))
            }
            require(revealed.size <= 2 && revealed.distinct().size == revealed.size && revealed.all { it in deck.indices })
            require(matchedMask in 0 until (1 shl deck.size) && results.size == config.pairs)
            require(exposures.size == deck.size && exposures.all { it in 0..100000 })
            deck.indices.forEach { require((matchedMask and (1 shl it) != 0) == results[ordinal(it)].solved) }
            require(!acknowledged || completed)
            when(phase) {
                Phase.PLAYING -> require(revealed.size <= 1 && revealed.none { results[ordinal(it)].solved } && solvedPairs < config.pairs)
                Phase.MATCH -> require(revealed.size==2 && deck[revealed[0]].pair==deck[revealed[1]].pair && results[ordinal(revealed[0])].solved)
                Phase.DIFFERENT -> require(revealed.size==2 && deck[revealed[0]].pair!=deck[revealed[1]].pair && revealed.none { results[ordinal(it)].solved } && results[ordinal(revealed[0])].wrong>0)
                Phase.MODEL -> require(revealed.size==2 && deck[revealed[0]].pair==deck[revealed[1]].pair && !results[ordinal(revealed[0])].solved && results[ordinal(revealed[0])].support.hint)
                Phase.COMPLETED -> require(revealed.isEmpty() && solvedPairs==config.pairs)
            }
        }
    }
    fun start(id: SessionId, config: MemoryConfig, seed: Long, language: ContentLanguage, previous: State? = null): State {
        val random=Random(seed)
        val candidates=pool(config.mode).toMutableList().also { Collections.shuffle(it, random) }
        if(previous?.config==config && candidates.take(config.pairs).toSet()==previous.pairIds.toSet()) {
            val replacement=candidates.indexOfFirst { it !in previous.pairIds }
            if(replacement>=config.pairs) Collections.swap(candidates,config.pairs-1,replacement)
        }
        val deck=candidates.take(config.pairs).flatMap { id ->
            if(config.mode==MemoryMode.ANIMALS) listOf(MemoryCard(id,MemoryFace.PICTURE,0),MemoryCard(id,MemoryFace.PICTURE,1))
            else listOf(MemoryCard(id,MemoryFace.NUMERAL,0),MemoryCard(id,MemoryFace.QUANTITY,1))
        }.toMutableList().also { Collections.shuffle(it,random) }
        if(deck==previous?.deck) Collections.swap(deck,0,1)
        return State(id,config,deck.toList(),language)
    }
    sealed interface Action {
        data class Reveal(val slot: Int): Action
        data object Continue: Action
        data object Replay: Action
        data object Help: Action
        data class Listen(val slot: Int): Action
        data class Language(val language: ContentLanguage): Action
    }
    fun reduce(s: State, action: Action): State {
        if(action is Action.Language) return s.copy(language=action.language)
        if(s.completed) return s
        fun support(indices: Set<Int>, hint: Boolean = false) = s.results.mapIndexed { i,r ->
            if(i !in indices || r.solved) r else r.copy(support=if(hint) r.support.copy(hint=true) else r.support.copy(replays=(r.support.replays+1).coerceAtMost(10000)))
        }
        return when(action) {
            is Action.Reveal -> {
                val slot=action.slot
                if(s.phase!=Phase.PLAYING || slot !in s.deck.indices || s.visible(slot)) s else {
                    val reveal=s.revealed+slot
                    val exposures=s.exposures.mapIndexed { i,n -> if(i==slot) (n+1).coerceAtMost(100000) else n }
                    if(reveal.size==1) s.copy(revealed=reveal,exposures=exposures) else {
                        val index=s.ordinal(reveal.first());val r=s.results[index]
                        if(r.attempts>=10000) s else {
                            val matches=s.deck[reveal.first()].pair==s.deck[slot].pair
                            val results=s.results.mapIndexed { i,old -> if(i!=index)old else r.copy(attempts=r.attempts+1,wrong=r.wrong+if(matches)0 else 1,solved=matches) }
                            val mask=if(matches) s.matchedMask or (1 shl reveal.first()) or (1 shl slot) else s.matchedMask
                            s.copy(revealed=reveal,exposures=exposures,results=results,matchedMask=mask,phase=if(matches)Phase.MATCH else Phase.DIFFERENT)
                        }
                    }
                }
            }
            Action.Continue -> if(s.phase==Phase.PLAYING) s else s.copy(revealed=emptyList(),phase=if(s.solvedPairs==s.config.pairs)Phase.COMPLETED else Phase.PLAYING)
            Action.Replay -> s.copy(results=support(s.results.indices.toSet()))
            is Action.Listen -> if(action.slot !in s.deck.indices || !s.visible(action.slot))s else s.copy(results=support(setOf(s.ordinal(action.slot))))
            Action.Help -> if(s.phase!=Phase.PLAYING)s else {
                val first=s.revealed.firstOrNull() ?: s.deck.indices.first { !s.visible(it) }
                val second=s.deck.indices.single { it!=first && s.deck[it].pair==s.deck[first].pair }
                s.copy(revealed=listOf(first,second),phase=Phase.MODEL,results=support(setOf(s.ordinal(first)),hint=true),
                    exposures=s.exposures.mapIndexed { i,n -> if((i==first||i==second)&&i !in s.revealed)(n+1).coerceAtMost(100000)else n })
            }
            is Action.Language -> s
        }
    }
    private fun origin(s: State)=ProgressOrigin(s.id,activity(s.config),REVISION,version)
    private fun evidence(s: State,i: Int)=TaskEvidence(TaskInstanceId(s.id,i+1),TaskDefinitionId("task.memory.${s.config.mode.key}.${s.pairIds[i].value}"),skill(s.config),context(s.config),s.config.pairs-1)
    fun attempt(s: State): AttemptEvent {
        require(s.phase in setOf(Phase.MATCH,Phase.DIFFERENT))
        val index=s.ordinal(s.revealed.first());val r=s.results[index]
        return AttemptEvent(origin(s),evidence(s,index),AttemptId(TaskInstanceId(s.id,index+1),r.attempts),s.language,s.deck[s.revealed.last()].pair,
            if(s.phase==Phase.MATCH)AttemptOutcome.CORRECT else AttemptOutcome.INCORRECT,r.support)
    }
    fun completion(s: State): CompletionEvent {
        require(s.completed)
        return CompletionEvent(origin(s),s.results.mapIndexed { i,r -> CompletedTask(evidence(s,i),s.pairIds[i],AttemptOutcome.CORRECT,r.attempts,r.wrong,r.support) })
    }
}
