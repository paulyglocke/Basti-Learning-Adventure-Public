package com.bellfamily.bastischool

import org.junit.Assert.*
import org.junit.Test

class ShellNavigationTest {
    @Test fun homeOptionsReturnsHomeAndBackExitsOnlyAtHome() {
        val home = ShellNavigation()
        val options = home.openOptions()
        assertEquals(BackAction.EXIT, home.backAction)
        assertEquals(BackAction.CLOSE_OPTIONS, options.backAction)
        assertFalse(options.ownsWebSession)
        assertEquals(home, options.closeOptions())
    }

    @Test fun everyLearningRouteKeepsOneSessionBehindOptions() {
        for (mode in ShellNavigation.modes) {
            val activity = ShellNavigation().openActivity(mode)
            val options = activity.openOptions()
            assertTrue(activity.ownsWebSession)
            assertTrue(options.ownsWebSession)
            assertEquals(BackAction.LEGACY, activity.backAction)
            assertEquals(activity, options.closeOptions())
            assertFalse(activity.home().ownsWebSession)
        }
    }

    @Test fun repeatedOptionsDoesNotReplaceOriginOrCreateALoop() {
        var route = ShellNavigation().openActivity("verbExplorer")
        repeat(10) {
            val original = route
            route = route.openOptions().openOptions().openOptions()
            assertEquals(ShellScreen.WEB, route.optionsOrigin)
            route = route.closeOptions()
            assertEquals(original, route)
        }
    }

    @Test fun savedRouteRestoresOptionsOriginAndExactCheckpoint() {
        for (mode in ShellNavigation.modes) {
            for (screen in listOf(ShellScreen.WEB, ShellScreen.OPTIONS)) {
                val snapshot = "{\"language\":\"de\",\"question\":4,\"score\":3}"
                val saved = LegacyCheckpoint(snapshot)
                val restored = ShellNavigation.restore(screen.name, "WEB", mode, saved.read())
                assertEquals(screen, restored.screen)
                assertTrue(restored.ownsWebSession)
                assertEquals(mode, restored.mode)
                assertEquals(snapshot, saved.read())
                if (screen == ShellScreen.OPTIONS) assertEquals(ShellScreen.WEB, restored.closeOptions().screen)
            }
        }
    }

    @Test fun homeOptionsRestoresWithoutAWebSession() {
        val restored = ShellNavigation.restore("OPTIONS", "HOME", "verbs", null)
        assertEquals(ShellScreen.OPTIONS, restored.screen)
        assertEquals(ShellScreen.HOME, restored.closeOptions().screen)
        assertFalse(restored.recoveryFailed)
    }

    @Test fun missingOversizedOrUnknownRecoveryReturnsHomeWithNotice() {
        for (snapshot in listOf(null, "", "x".repeat(LegacyCheckpoint.MAX_CHARS + 1))) {
            val restored = ShellNavigation.restore("OPTIONS", "WEB", "count", snapshot)
            assertEquals(ShellScreen.HOME, restored.screen)
            assertTrue(restored.recoveryFailed)
        }
        assertTrue(ShellNavigation.restore("WEB", "HOME", "unknown", "{}").recoveryFailed)
        assertTrue(ShellNavigation.restore("unknown", "HOME", "count", "{}").recoveryFailed)
    }

    @Test fun checkpointPublicationDoesNotDependOnUiThreadAndOldOwnersCannotReplaceNew() {
        val old = LegacyCheckpoint("first")
        val current = LegacyCheckpoint("second")
        val thread = Thread { old.publish("late callback") }
        thread.start(); thread.join()
        assertEquals("second", current.read())
        current.publish("x".repeat(LegacyCheckpoint.MAX_CHARS + 1))
        assertNull(current.read())
    }

    @Test fun backCoalescesAndOptionsOrDisposalInvalidatesLateCallbacks() {
        val gate = LegacyBackGate()
        val first = gate.begin()!!
        assertTrue(gate.isPending)
        assertNull(gate.begin())
        gate.invalidate()
        assertFalse(gate.isPending)
        assertFalse(gate.finish(first))
        val next = gate.begin()!!
        assertTrue(gate.finish(next))
        assertFalse(gate.finish(next))
        assertNotNull(gate.begin())
    }

    @Test fun monthsBuildYearHasItsOwnRouteAndReturnsToMonths() {
        val levels = ShellNavigation().openMonthsBuildYear()
        assertEquals(ShellScreen.MONTHS_BUILD_YEAR_LEVELS, levels.screen)
        assertEquals(BackAction.MONTHS_AREA, levels.backAction)
        assertEquals(levels, levels.openOptions().closeOptions())
        val three = levels.openMonthsBuildYearThree()
        val four = levels.openMonthsBuildYearFour()
        assertEquals(ShellScreen.MONTHS_BUILD_YEAR, three.screen)
        assertEquals(ShellScreen.MONTHS_BUILD_YEAR_FOUR, four.screen)
        assertEquals(BackAction.MONTHS_AREA, three.backAction)
        assertEquals(BackAction.MONTHS_AREA, four.backAction)
        assertEquals(ShellScreen.MONTHS, levels.openMonths().screen)
    }

    @Test fun monthsBetweenAndItsYearWheelReturnToTheSameQuestionRoute() {
        val question = ShellNavigation().openMonthsBetween()
        assertEquals(ShellScreen.MONTHS_BETWEEN, question.screen)
        assertEquals(BackAction.MONTHS_AREA, question.backAction)
        val wheel = question.openMonthsBetweenWheel()
        assertEquals(ShellScreen.MONTHS_BETWEEN_WHEEL, wheel.screen)
        assertEquals(BackAction.MONTHS_AREA, wheel.backAction)
        assertEquals(question.screen, ShellNavigation().openMonthsBetween().openMonthsBetweenWheel()
            .openMonthsBetween().screen)
        assertEquals(question, question.openOptions().closeOptions())
    }
}
