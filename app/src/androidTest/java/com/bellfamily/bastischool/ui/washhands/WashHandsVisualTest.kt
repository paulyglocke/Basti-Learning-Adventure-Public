package com.bellfamily.bastischool.ui.washhands

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import com.bellfamily.bastischool.captureSweepScreen
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.session.SessionId
import com.bellfamily.bastischool.learning.washhands.WashHands
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WashHandsVisualTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @Test fun normalPhoneShowsPicturesInChoicesAndConstructedOrder() {
        compose.activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT}
        var state by mutableStateOf(WashHands.start(SessionId("wash-pictures"),4,ContentLanguage.ENGLISH))
        compose.setContent {MaterialTheme {
            WashHandsScreen(state,ContentLanguage.ENGLISH,false,false,false,
                {state=WashHands.reduce(state,it).state},{},{},{},{})
        }}
        WashHandsArtwork.paths.forEach { (id,path) ->
            compose.onNodeWithTag("wash-item-${id.value}").performScrollTo()
            compose.waitUntil(10_000) {compose.onAllNodesWithTag("bundled-art-$path",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("bundled-art-$path",useUnmergedTree=true).assertIsDisplayed()
        }
        compose.onNodeWithTag("wash-replay").performScrollTo()
        captureSweepScreen("wash-choices",compose)
        WashHands.order.take(2).forEach {id->compose.onNodeWithTag("wash-item-${id.value}").performScrollTo().performClick()}
        compose.onNodeWithTag("wash-position-1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("bundled-art-${WashHandsArtwork.paths.getValue(WashHands.order[0])}",useUnmergedTree=true).assertIsDisplayed()
        assertEquals(WashHands.order.take(2),state.constructed)
        captureSweepScreen("wash-constructed",compose)
    }
}
