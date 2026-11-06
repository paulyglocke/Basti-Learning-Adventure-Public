package com.bellfamily.bastischool.ui.verbmotion

import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

/** Tests that need motion restore the exact prior device preference, including absent. */
class MotionScaleRule:ExternalResource() {
    private var original:String?=null
    override fun before() {
        original=Settings.Global.getString(InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE)
        set("1")
    }
    override fun after() {set(original)}
    private fun set(value:String?) {
        require(value==null || value.toFloatOrNull()!=null)
        val command=if(value==null)"settings delete global animator_duration_scale" else "settings put global animator_duration_scale $value"
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
    }
}
