package com.bellfamily.bastischool.learning.session

import com.bellfamily.bastischool.learning.content.CoreContent
import com.bellfamily.bastischool.learning.models.*

internal object SessionFixtures {
    val content = CoreContent.repository()
    val id = SessionId("test-session")
    val activity = ActivityId("activity.test_choice")
    val summary = ContentText.plain("Finished!", "Geschafft!")
    fun candidates(): List<ChoiceQuestion> = content.weekdays().take(3).map { day ->
        ChoiceQuestion(TaskDefinitionId("task.find_${day.id.value.substringAfter('.') }"),
            SkillId("skill.test.naming"), LearningContextId("context.test.calendar"), 1,
            ContentText(LocalizedText("${day.text.display.en} 🔊", "${day.text.display.de} 🔊"),
                LocalizedText("Touch ${day.text.speech.en}.", "Tippe auf ${day.text.speech.de}.")),
            content.weekdays().map { it.id }, day.id,
            ContentText.plain("Yes!", "Ja!"), ContentText.plain("Try again.", "Versuche es noch einmal."),
            ContentText.plain("Listen to the day.", "Hör dir den Tag an."))
    }
    fun request(round: RoundLength = RoundLength.FIVE, policy: WrongAnswerPolicy = WrongAnswerPolicy.RETRY,
                seed: Long = 42) = GenerationRequest(id, activity, 1, SessionPolicy(round, policy), seed, summary)
    fun plan(round: RoundLength = RoundLength.FIVE, policy: WrongAnswerPolicy = WrongAnswerPolicy.RETRY,
             seed: Long = 42) = (CandidateTaskGenerator(candidates()).generate(request(round, policy, seed), content)
        as GenerationResult.Generated).plan
    fun start(round: RoundLength = RoundLength.FIVE, policy: WrongAnswerPolicy = WrongAnswerPolicy.RETRY,
              language: ContentLanguage = ContentLanguage.ENGLISH) = SessionReducer.start(plan(round, policy), language, content).state
    fun answer(state: SessionState, correct: Boolean = true): SessionTransition = SessionReducer.reduce(state,
        SessionAction.Answer(state.nextAttempt!!, if (correct) state.task.question.correct
        else state.task.question.choices.first { it != state.task.question.correct }))
    fun next(state: SessionState) = SessionReducer.reduce(state, SessionAction.Next(state.task.id))
    fun retry(state: SessionState) = SessionReducer.reduce(state, SessionAction.Retry(AttemptId(state.task.id, state.current.attempts)))
    fun completed(round: RoundLength = RoundLength.FIVE): SessionTransition {
        var transition = SessionTransition(start(round))
        repeat(round.count) { transition = next(answer(transition.state).state) }
        return transition
    }
    fun restore(state: SessionState): SessionState = (SessionCheckpoint.restore(SessionCheckpoint.encode(state),
        activity, 1, content) as SessionRestoreResult.Restored).state
}
