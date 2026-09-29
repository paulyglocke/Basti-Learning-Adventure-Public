package com.bellfamily.bastischool.ui.animalgroups

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.bellfamily.bastischool.learning.animalgroups.AnimalGroups
import com.bellfamily.bastischool.learning.models.*
import com.bellfamily.bastischool.learning.session.SessionId
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class AnimalGroupsArtworkTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @Test fun fourCanonicalAssetsDecodeFitSampleBoundAndCache() {
        val app=ApplicationProvider.getApplicationContext<Application>();var opens=0
        val loader=AnimalGroupsArtworkLoader {opens++;app.assets.open(it)}
        val first=loader.load();assertEquals(4,first.size);assertEquals(8,opens)
        first.values.forEach {assertTrue(it.width in 1..384 && it.height in 1..384)}
        assertSame(first,loader.load());assertEquals(8,opens)
    }
    @Test fun missingAndCorruptArtArePassiveAndOtherAnimalsRemainAvailable() {
        val app=ApplicationProvider.getApplicationContext<Application>()
        val images=AnimalGroupsArtworkLoader {path -> when {
            path.endsWith("whale.png")->throw IOException("missing")
            path.endsWith("dolphin.png")->ByteArrayInputStream(byteArrayOf(0,1,2))
            else->app.assets.open(path)
        }}.load()
        assertEquals(2,images.size)
        val state=AnimalGroups.start(SessionId("missing"),1,ContentLanguage.ENGLISH);var actions=0
        compose.setContent {MaterialTheme {AnimalGroupsScreen(state,ContentLanguage.ENGLISH,false,false,false,images,{actions++},{},{},{})}}
        compose.onNodeWithTag("groups-item-animal.whale").performScrollTo().assertHasNoClickAction()
        compose.onNodeWithTag("groups-item-animal.dolphin").performScrollTo().assertHasNoClickAction()
        compose.onNodeWithTag("groups-item-animal.horse").performScrollTo().assertHasClickAction()
        assertEquals(0,actions);assertTrue(state.placements.all {it.attempts==0})
    }
}
