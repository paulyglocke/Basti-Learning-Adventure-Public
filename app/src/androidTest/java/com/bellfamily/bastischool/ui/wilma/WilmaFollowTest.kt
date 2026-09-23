package com.bellfamily.bastischool.ui.wilma

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bellfamily.bastischool.learning.models.ContentLanguage
import com.bellfamily.bastischool.learning.wilma.WilmaContent
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class WilmaFollowTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    private fun check(orientation:Int,width:Int) {
        compose.activity.requestedOrientation=orientation
        val expected=if(orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)Configuration.ORIENTATION_PORTRAIT else Configuration.ORIENTATION_LANDSCAPE
        compose.waitUntil(10_000){compose.activity.resources.configuration.orientation==expected}
        var count by mutableIntStateOf(3)
        var instance by mutableIntStateOf(0)
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f)) {MaterialTheme {
                Box(Modifier.width(width.dp).height(220.dp)) {key(instance) {
                    WilmaStrip(emptyMap(),ContentLanguage.GERMAN,WilmaContent.days.take(count),null,emptySet(),{},prefix="placed",showTail=count==7)
                }}
            }}
        }
        fun latestVisible() {
            val viewport=compose.onNodeWithTag("wilma-strip-placed").fetchSemanticsNode().boundsInRoot
            val last=compose.onNodeWithTag("placed-${WilmaContent.days[count-1].value}").fetchSemanticsNode()
            // Use the actual layout extent, not clipped semantics bounds that could hide truncation.
            val left=last.positionInRoot.x
            assertTrue("Latest segment must fit within viewport",left>=viewport.left-1 && left+last.size.width<=viewport.right+1)
            compose.onNodeWithTag("placed-${WilmaContent.days[count-1].value}").assertIsDisplayed()
        }
        latestVisible() // restored partial sequence, no placement action or manual scroll
        for(n in 4..6) {compose.runOnIdle {count=n};latestVisible()}
        val before=compose.onNodeWithTag("placed-day.saturday").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle {instance++} // new presentation of the same durable placement state
        latestVisible()
        assertEquals(before.left,compose.onNodeWithTag("placed-day.saturday").fetchSemanticsNode().boundsInRoot.left,1f)
        compose.runOnIdle {count=7};latestVisible()
        // A recomposition/wrong attempt that does not add a day leaves the position stable.
        val final=compose.onNodeWithTag("placed-day.sunday").fetchSemanticsNode().boundsInRoot
        compose.waitForIdle()
        assertEquals(final,compose.onNodeWithTag("placed-day.sunday").fetchSemanticsNode().boundsInRoot)
    }
    @Test fun portraitLargeTextFollowsNewSegmentsAndRestoresAtGrowingEnd()=check(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,320)
    @Test fun shortLandscapeFollowsNewSegments()=check(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,640)
    @Test fun reverseLandscapeFollowsNewSegments()=check(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,640)
}
