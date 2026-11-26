package com.bellfamily.bastischool

import com.bellfamily.bastischool.ui.home.*
import org.junit.Assert.*
import org.junit.Test

class LengthOrderingNavigationTest {
    @Test fun homeOptionsAndRecreationKeepAnIndependentNativeDestination() {
        val route=homeActivity("lengthOrder").homeDestination()
        assertEquals(ShellScreen.LENGTH_ORDER,route.screen)
        assertEquals(BackAction.NATIVE_HOME,route.backAction)
        assertFalse(route.ownsWebSession)
        for(r in listOf(route,route.openOptions()))assertEquals(r,ShellNavigation.restore(r.screen.name,r.optionsOrigin.name,r.mode,null))
        assertEquals(route,route.openOptions().closeOptions())
        assertEquals(HomeCategory.MATHS,homeCategory("lengthOrder"))
    }
}
