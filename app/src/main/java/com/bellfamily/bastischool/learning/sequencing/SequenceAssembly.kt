package com.bellfamily.bastischool.learning.sequencing

import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.*
import java.util.Collections

/** Build freely, evaluate on Check. Distinct from OrderedPlacement's correct-prefix policy. */
class SequenceState(val id: SessionId, val language: ContentLanguage, target: List<ContentId>,
    presentation: List<ContentId>, constructed: List<ContentId> = emptyList(),
    val revision: Int = 0, val attempts: Int = 0, lastChecked: List<ContentId> = emptyList(),
    val support: SupportUse = SupportUse(), val hintPosition: Int? = null, val acknowledged: Boolean = false) {
    val target = Collections.unmodifiableList(target.toList())
    val presentation = Collections.unmodifiableList(presentation.toList())
    val constructed = Collections.unmodifiableList(constructed.toList())
    val lastChecked = Collections.unmodifiableList(lastChecked.toList())
    val available get() = presentation.filter { it !in constructed }
    val completed get() = attempts > 0 && lastChecked == target
    val needsCorrection get() = attempts > 0 && constructed == lastChecked && !completed
    val firstMismatch get() = target.indices.firstOrNull { constructed.getOrNull(it) != target[it] }
    init {
        require(target.size in 2..10 && target.distinct().size == target.size)
        require(presentation.size == target.size && presentation.toSet() == target.toSet())
        require(constructed.distinct().size == constructed.size && constructed.all { it in target })
        require(revision in 0..100000 && attempts in 0..10000 && revision >= attempts)
        require(support.replays in 0..10000)
        require(if(attempts == 0) lastChecked.isEmpty() else lastChecked.size == target.size && lastChecked.toSet() == target.toSet())
        require(!completed || constructed == target)
        require(!acknowledged || completed)
        require(hintPosition == null || support.hint && hintPosition == firstMismatch)
    }
    fun changed(language: ContentLanguage = this.language, constructed: List<ContentId> = this.constructed,
        revision: Int = this.revision, attempts: Int = this.attempts, lastChecked: List<ContentId> = this.lastChecked,
        support: SupportUse = this.support, hintPosition: Int? = this.hintPosition, acknowledged: Boolean = this.acknowledged) =
        SequenceState(id,language,target,presentation,constructed,revision,attempts,lastChecked,support,hintPosition,acknowledged)
}
sealed interface SequenceOperation {
    data class Append(val item: ContentId): SequenceOperation
    data class Remove(val item: ContentId): SequenceOperation
    data object Check: SequenceOperation
    data object Hint: SequenceOperation
    data object Replay: SequenceOperation
    data class Language(val language: ContentLanguage): SequenceOperation
}
data class SequenceAction(val session: SessionId, val revision: Int, val operation: SequenceOperation)
object SequenceAssembly {
    fun start(id:SessionId, target:List<ContentId>, seed:Long, language:ContentLanguage) =
        SequenceState(id,language,target,OrderedPlacement.scramble(target,seed))
    fun reduce(s:SequenceState,a:SequenceAction):SequenceState {
        if(a.session!=s.id || a.revision!=s.revision || s.revision>=100000)return s
        val revision=s.revision+1
        if(a.operation is SequenceOperation.Language) return if(s.language==a.operation.language)s else s.changed(language=a.operation.language,revision=revision)
        if(s.completed)return s
        return when(val op=a.operation) {
            is SequenceOperation.Append -> if(op.item !in s.available)s else s.changed(constructed=s.constructed+op.item,revision=revision,hintPosition=null)
            is SequenceOperation.Remove -> if(op.item !in s.constructed)s else s.changed(constructed=s.constructed-op.item,revision=revision,hintPosition=null)
            SequenceOperation.Check -> if(s.constructed.size!=s.target.size || s.attempts>=10000)s else
                s.changed(revision=revision,attempts=s.attempts+1,lastChecked=s.constructed,hintPosition=null)
            SequenceOperation.Hint -> if(s.firstMismatch==null)s else s.changed(revision=revision,support=s.support.copy(hint=true),hintPosition=s.firstMismatch)
            SequenceOperation.Replay -> s.changed(revision=revision,support=s.support.copy(replays=(s.support.replays+1).coerceAtMost(10000)))
            is SequenceOperation.Language -> s
        }
    }
}
