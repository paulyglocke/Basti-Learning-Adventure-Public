package com.bellfamily.bastischool

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** Genuine renderer/capture artifacts on this disposable emulator, retained through test APK uninstall. */
internal fun saveNativeReviewBitmap(name:String,bitmap:Bitmap) {
    require(name.matches(Regex("[a-z0-9-]+")))
    val instrumentation=InstrumentationRegistry.getInstrumentation()
    val file=File(instrumentation.targetContext.getExternalFilesDir(null),"$name.png")
    file.outputStream().use{check(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}
    val directory="/sdcard/Download/basti-native-verbs-bonds-20261107"
    instrumentation.uiAutomation.executeShellCommand("mkdir -p $directory").use{android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()}
    instrumentation.uiAutomation.executeShellCommand("cp ${file.absolutePath} $directory/$name.png").use{android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()}
}
internal fun captureNativeReview(name:String,compose:ComposeTestRule) {
    compose.waitForIdle();val bitmap=compose.onRoot().captureToImage().asAndroidBitmap()
    try{saveNativeReviewBitmap(name,bitmap)}finally{bitmap.recycle()}
}
