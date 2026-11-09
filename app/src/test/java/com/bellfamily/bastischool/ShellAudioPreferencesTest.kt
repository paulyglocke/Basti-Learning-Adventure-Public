package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class ShellAudioPreferencesTest {
    @Test fun legacySoundOffSurvivesUpgradeAndExplicitCurrentPolicyTakesPrecedence() {
        assertEquals("off",shellAudioMode(null,false))
        assertEquals("all",shellAudioMode(null,true))
        assertEquals("questions",shellAudioMode("questions",false))
        assertEquals("off",shellAudioMode("off",true))
        assertEquals("all",shellAudioMode("all",false))
    }
}
