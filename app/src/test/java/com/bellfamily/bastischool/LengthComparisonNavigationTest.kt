package com.bellfamily.bastischool

import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class LengthComparisonNavigationTest {
    @Test fun homeOptionsAndRecreationKeepAnIndependentNativeDestination() {
        val route=homeActivity("lengthComparison").homeDestination()
        assertEquals(ShellScreen.LENGTH_COMPARE,route.screen)
        assertEquals(BackAction.NATIVE_HOME,route.backAction)
        assertFalse(route.ownsWebSession)
        for(r in listOf(route,route.openOptions()))assertEquals(r,ShellNavigation.restore(r.screen.name,r.optionsOrigin.name,r.mode,null))
        assertEquals(route,route.openOptions().closeOptions())
        assertEquals(HomeCategory.MATHS,homeCategory("lengthComparison"))
    }
}
