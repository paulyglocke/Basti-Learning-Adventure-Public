package com.bellfamily.bastischool.ui.prepositions

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.prepositions.*
import com.bellfamily.bastischool.learning.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class PrepositionsArtworkScreenTest(private val language:ContentLanguage,private val orientation:Int) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}")
        fun cases()=listOf(
            arrayOf<Any>(ContentLanguage.ENGLISH,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
            arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
            arrayOf<Any>(ContentLanguage.GERMAN,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE))
    }
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @Test fun artworkDescriptionsChoicesActionsAndFailureStayUsableAtLargeFont() {
        val landscape=orientation!=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.activity.requestedOrientation=orientation
        compose.waitUntil(10_000) {compose.activity.resources.configuration.orientation==if(landscape)Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT}
        val rotation=when(orientation) {ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE->Surface.ROTATION_90;ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE->Surface.ROTATION_270;else->Surface.ROTATION_0}
        compose.waitUntil(10_000) {compose.activity.window.decorView.display?.rotation==rotation}
        val keys=listOf("snake.on","bird.above","fish.inside","dragon.near","crocodile.far_from")
        val scenes=keys.map {key->PrepositionsContent.scenes.single {it.id.value=="scene.prepositions.$key"}}
        val id=SessionId("artwork-ui")
        val tasks=scenes.mapIndexed {i,s->
            val others=PositionRelation.entries.filter {it!=s.relation}.take(3)
            ChoiceTask(TaskInstanceId(id,i+1),PrepositionsContent.question(s,(listOf(s.relation)+others).map {it.id}))
        }
        val plan=SessionPlan(id,PrepositionsContent.activity,PrepositionsContent.REVISION,PrepositionsContent.version,SessionPolicy(RoundLength.FIVE),tasks,PrepositionsContent.completion)
        val state=mutableStateOf(SessionReducer.start(plan,language,PrepositionsContent.repository).state)
        // Decode outside composition, just as the production worker does.
        val loader=PrepositionsArtworkLoader {compose.activity.assets.open(it)}
        val images=scenes.associate {it.id to loader.load(it.id)!!}
        val failed=mutableStateOf(false)
        var listens=0;var home=0
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {
                MaterialTheme {
                    PrepositionsScreen(state.value,language,false,false,false,
                        {state.value=SessionReducer.reduce(state.value,it).state},{listens++},{},{},{},{home++},{},
                        Modifier.size(if(landscape)700.dp else 320.dp,if(landscape)240.dp else 600.dp).testTag("art-review"),
                        artwork=if(failed.value)null else images[PrepositionsContent.scene(state.value.task).id])
                }
            }
        }
        for(index in 0..2) {
            val scene=scenes[index]
            compose.onNodeWithTag("position-scene").performScrollTo().assertIsDisplayed().assertContentDescriptionEquals(scene.description[language])
            compose.onNodeWithTag("position-image-unavailable").assertDoesNotExist()
            // A scroll viewport may expose only part of the frame; verify the complete layout.
            val bounds=compose.onNodeWithTag("position-scene").getUnclippedBoundsInRoot()
            assertEquals(4f/3f,(bounds.right-bounds.left).value/(bounds.bottom-bounds.top).value,0.02f)
            if(index==0) {
                val snapshot=SessionCheckpoint.encode(state.value)
                compose.runOnIdle {failed.value=true}
                compose.onNodeWithTag("position-image-unavailable").performScrollTo().assertIsDisplayed()
                compose.runOnIdle {assertArrayEquals(snapshot,SessionCheckpoint.encode(state.value));failed.value=false}
            }
            val q=state.value.task.question
            q.choices.forEach {choice->compose.onNodeWithTag("answer-${choice.value}").performScrollTo().assertIsDisplayed().assertIsEnabled().assertHeightIsAtLeast(64.dp)}
            compose.onNodeWithTag("speaker-${q.correct.value}").performScrollTo().performClick()
            compose.onNodeWithTag("replay").performScrollTo().performClick()
            compose.onNodeWithTag("hint").performScrollTo().performClick()
            compose.onNodeWithTag("hint-text").performScrollTo().assertIsDisplayed()
            compose.runOnIdle {assertEquals(0,state.value.current.attempts);assertTrue(state.value.current.support.hint)}
            val wrong=q.choices.first {it!=q.correct}
            compose.onNodeWithTag("answer-${wrong.value}").performScrollTo().performClick()
            compose.onNodeWithTag("retry").performScrollTo().performClick()
            compose.onNodeWithTag("answer-${q.correct.value}").performScrollTo().performClick().assertIsNotEnabled()
            compose.onNodeWithTag("speaker-${q.correct.value}").assertIsEnabled()
            compose.onNodeWithTag("next").performScrollTo().assertIsDisplayed().performClick()
        }
        compose.onNodeWithTag("position-scene").performScrollTo()
        val bitmap=compose.onNodeWithTag("art-review").captureToImage().asAndroidBitmap()
        File(compose.activity.cacheDir,"prepositions-art-$language-$orientation.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        compose.onNodeWithTag("home").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle {assertEquals(3,listens);assertEquals(3,state.value.score);assertEquals(1,home)}
    }
}
