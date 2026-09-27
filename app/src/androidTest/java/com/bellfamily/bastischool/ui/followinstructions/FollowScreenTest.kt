package com.bellfamily.bastischool.ui.followinstructions

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import com.bellfamily.bastischool.learning.follow.FollowContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.models.ContentText
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FollowScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun objectsAreFourSemanticTargetsAndWrongRetryCorrectNextPreserveFlow() {
        val plan = (FollowContent.generate(SessionId("ui"), RoundLength.FIVE, 4) as GenerationResult.Generated).plan
        var state by mutableStateOf(SessionReducer.start(plan, ContentLanguage.ENGLISH, FollowContent.repository).state)
        val images = FollowContent.objects.associate { it.id to ImageBitmap(32, 32) }
        compose.setContent {
            MaterialTheme {
                FollowScreen(state, ContentLanguage.ENGLISH, images, false, false, false,
                    { state = SessionReducer.reduce(state, it).state }, {}, {}, {}, {}, Modifier)
            }
        }
        FollowContent.objects.forEach { objectDef ->
            compose.onNodeWithTag("follow-object-${objectDef.id.value}").assertIsDisplayed()
            compose.onNodeWithTag("follow-tap-${objectDef.id.value}").assertIsDisplayed()
        }
        val target = state.task.question.correct
        val wrong = state.task.question.choices.first { it != target }
        compose.onNodeWithTag("follow-tap-${wrong.value}").performClick()
        compose.onNodeWithTag("follow-retry").assertIsDisplayed().performClick()
        assertEquals(AnswerState.UNANSWERED, state.current.answer)
        compose.onNodeWithTag("follow-tap-${target.value}").performClick()
        compose.onNodeWithTag("follow-next").assertIsDisplayed().performClick()
        assertEquals(1, state.index)
    }

    @Test fun germanLabelsAndReplayAreSeparateFromObjectTargets() {
        val plan = (FollowContent.generate(SessionId("ui-de"), RoundLength.FIVE, 8) as GenerationResult.Generated).plan
        var state by mutableStateOf(SessionReducer.start(plan, ContentLanguage.GERMAN, FollowContent.repository).state)
        val images = FollowContent.objects.associate { it.id to ImageBitmap(32, 32) }
        var replay = 0
        compose.setContent {
            MaterialTheme { FollowScreen(state, ContentLanguage.GERMAN, images, false, false, false,
                { state = SessionReducer.reduce(state, it).state }, { replay++ }, {}, {}, {}, Modifier) }
        }
        compose.onNodeWithText("Tippe auf", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("follow-replay").performClick()
        assertEquals(1, replay)
        FollowContent.objects.forEach { objectDef ->
            compose.onNodeWithTag("follow-object-${objectDef.id.value}").assertIsDisplayed()
        }
    }
}
