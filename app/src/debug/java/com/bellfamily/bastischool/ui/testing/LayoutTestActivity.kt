package com.bellfamily.bastischool.ui.testing

import androidx.activity.ComponentActivity

/**
 * Empty host for isolated Compose layout fixtures. Like MainActivity, its manifest
 * handles orientation/screen-size changes without replacing the Activity. Content
 * installed by ComposeTestRule is not an onCreate callback and would otherwise be
 * lost if Android completes a pending rotation after setContent.
 *
 * Production lifecycle/restoration tests still launch MainActivity directly.
 */
class LayoutTestActivity : ComponentActivity()
