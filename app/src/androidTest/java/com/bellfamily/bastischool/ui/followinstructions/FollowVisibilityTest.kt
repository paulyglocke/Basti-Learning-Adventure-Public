package com.bellfamily.bastischool.ui.followinstructions

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.follow.FollowContent
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FollowVisibilityTest(private val language: ContentLanguage, private val orientation: Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}") fun cases() = listOf(
            arrayOf<Any>(ContentLanguage.ENGLISH, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.GERMAN, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.GERMAN, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>(ContentLanguage.GERMAN, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE))
    }
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun visiblePixelsSingleLabeledTargetLockRetryAndPassiveFailure() {
        compose.activity.requestedOrientation = orientation
        val landscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation ==
            if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT }
        val plan = (FollowContent.generate(SessionId("visible"), RoundLength.FIVE, 17) as GenerationResult.Generated).plan
        var state by mutableStateOf(SessionReducer.start(plan, language, FollowContent.repository).state)
        // Distinctive opaque image makes an overlaid opaque hit target objectively detectable.
        val bitmap = Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.MAGENTA) }.asImageBitmap()
        var images by mutableStateOf(FollowContent.objects.associate { it.id to bitmap })
        var replays = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme { FollowScreen(state,language,images,false,false,false,
                    { state = SessionReducer.reduce(state,it).state },{replays++},{},{},{}) }
            }
        }
        fun target(id: com.bellfamily.bastischool.learning.models.ContentId) = compose.onNodeWithTag("follow-tap-${id.value}")
        state.task.question.choices.forEach { id ->
            val node = target(id).performScrollTo().assertIsDisplayed().assertIsEnabled()
            node.assertHeightIsEqualTo(150.dp).assertWidthIsAtLeast(56.dp)
            compose.onAllNodesWithContentDescription(FollowContent.objectFor(id).text.display[language]).assertCountEquals(1)
            val pixels = node.captureToImage().toPixelMap()
            val center = pixels[pixels.width/2,pixels.height/2]
            assertEquals(1f,center.red,0.02f); assertEquals(0f,center.green,0.02f); assertEquals(1f,center.blue,0.02f)
        }
        val before = SessionCheckpoint.encode(state)
        compose.onNodeWithTag("follow-replay").performScrollTo().performClick()
        assertEquals(1,replays); assertArrayEquals(before,SessionCheckpoint.encode(state))
        val correct = state.task.question.correct
        val wrong = state.task.question.choices.first { it != correct }
        target(wrong).performScrollTo().performClick()
        target(correct).assertIsNotEnabled()
        compose.onNodeWithTag("follow-retry").performScrollTo().performClick()
        target(correct).performScrollTo().assertIsEnabled().performClick()
        target(correct).assertIsNotEnabled()
        compose.onNodeWithTag("follow-next").performScrollTo().performClick()
        val missing = state.task.question.choices.first()
        val unchanged = SessionCheckpoint.encode(state)
        compose.runOnIdle { images = images - missing }
        target(missing).performScrollTo().assertIsNotEnabled().performClick()
        assertArrayEquals(unchanged,SessionCheckpoint.encode(state))
        compose.onNodeWithTag("follow-unavailable-${missing.value}", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("follow-home").performScrollTo().assertIsDisplayed()
    }
}
