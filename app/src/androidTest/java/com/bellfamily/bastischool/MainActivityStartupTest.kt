package com.bellfamily.bastischool

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.junit.Rule
import org.junit.Test

/** Launch the real shell: isolated Compose screens cannot detect owner/content initialization crashes. */
class MainActivityStartupTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun homeSurvivesLaunchRecreationAndBackgroundReturn() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            compose.onNodeWithTag("home-settings").assertIsDisplayed()
            scenario.recreate()
            compose.onNodeWithTag("home-settings").assertIsDisplayed()
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.onNodeWithTag("home-settings").assertIsDisplayed()
        }
    }
}
