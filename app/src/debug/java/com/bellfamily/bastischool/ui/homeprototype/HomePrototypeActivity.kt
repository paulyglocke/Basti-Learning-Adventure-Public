package com.bellfamily.bastischool.ui.homeprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/** Explicit debug launch only. Never creates production ViewModels, WebViews, speech or preferences. */
class HomePrototypeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomePrototype(initialGerman = intent.getStringExtra("language") == "de") }
    }
}
