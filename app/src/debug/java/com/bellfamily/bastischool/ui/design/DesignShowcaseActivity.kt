package com.bellfamily.bastischool.ui.design

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/** Debug-only, no app preferences/session/navigation dependencies. */
class DesignShowcaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val german = intent.getStringExtra("language") == "de"
        val page = intent.getStringExtra("page") ?: "all"
        setContent {
            BastiDesignTheme {
                Surface(color = BastiPalette.background) {
                    BastiDesignShowcase(german, Modifier.fillMaxSize().safeDrawingPadding(), page)
                }
            }
        }
    }
}
