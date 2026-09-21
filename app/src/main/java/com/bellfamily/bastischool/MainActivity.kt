package com.bellfamily.bastischool

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.speech.tts.UtteranceProgressListener
import org.json.JSONObject
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat

private val Sky = Color(0xFF79CEF7)
private val Grass = Color(0xFFA8E66C)
private val Ink = Color(0xFF18324A)
private val Gold = Color(0xFFFFD76A)
private val PaleBlue = Color(0xFFEAF7FC)
private val PaleYellow = Color(0xFFFFF5C8)

private enum class ShellScreen { HOME, OPTIONS, WEB }

private data class HomeCard(
    val emoji: String,
    val titleEn: String,
    val titleDe: String,
    val descriptionEn: String,
    val descriptionDe: String,
    val mode: String? = null,
    val verbExplorer: Boolean = false,
    val play: Boolean = false
)

private val homeCards = listOf(
    HomeCard("📚", "Vocabulary Booster", "Wortschatz", "Learn useful words for school.", "Nützliche Wörter für die Schule lernen."),
    HomeCard("🎬", "Learn Verbs", "Verben lernen", "Watch, hear and learn each action.", "Aktionen anschauen, anhören und lernen.", verbExplorer = true),
    HomeCard("🔤", "Letters", "Buchstaben", "Find words with the same first letter.", "Finde passende Anfangsbuchstaben.", "letters"),
    HomeCard("🔢", "Numbers", "Zahlen", "Count and order numbers up to your level.", "Zahlen zählen und ordnen.", "count"),
    HomeCard("📅", "Days & Seasons", "Tage & Jahreszeiten", "Days of the week and four seasons.", "Wochentage und Jahreszeiten.", "time"),
    HomeCard("🦘", "Animal Actions", "Tier-Aktionen", "Jump, swim, fly and make animal sounds.", "Springen, schwimmen, fliegen und Tiergeräusche.", "verbs"),
    HomeCard("➕", "Easy Maths", "Einfache Mathe", "Add and take away within 10.", "Plus und Minus bis 10.", "math"),
    HomeCard("📦", "Prepositions", "Präpositionen", "In, on, under, behind, next to and between.", "In, auf, unter, hinter, neben und zwischen.", "positions"),
    HomeCard("🌟", "Mixed Adventure", "Gemischtes Abenteuer", "A little bit of everything.", "Von allem ein bisschen.", "mixed"),
    HomeCard("🐉", "Dragon Treasure Hunt", "Drachenschatz", "A future adventure game.", "Ein zukünftiges Abenteuerspiel.", play = true),
    HomeCard("🦖", "Dinosaur Rescue", "Dinosaurier-Rettung", "A future adventure game.", "Ein zukünftiges Abenteuerspiel.", play = true),
    HomeCard("🧠", "Memory Pairs", "Paare merken", "A future memory game.", "Ein zukünftiges Merkspiel.", play = true),
    HomeCard("🐊", "Crocodile Snap", "Krokodil-Schnapp", "A future reaction game.", "Ein zukünftiges Reaktionsspiel.", play = true),
    HomeCard("👂", "Follow the Instructions", "Anweisungen folgen", "A future listening game.", "Ein zukünftiges Hörspiel.", play = true)
)

class MainActivity : ComponentActivity() {
    private lateinit var prefs: android.content.SharedPreferences
    private var webView: WebView? = null
    private var tts: TextToSpeech? = null
    private var audioStatus by mutableStateOf("initialising")
    private var foreground = false
    private var utteranceSerial = 0L
    private val speech = LegacySpeech<Voice>(
        stopEngine = { tts?.stop(); Unit },
        play = { voice, text, id ->
            val engine = tts
            engine != null && engine.setVoice(voice) == TextToSpeech.SUCCESS &&
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.SUCCESS
        },
        statusChanged = { audioStatus = it }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        prefs = getSharedPreferences("basti_shell", Context.MODE_PRIVATE)
        tts = TextToSpeech(this) { status ->
            // Post so the constructor assignment is complete even for an immediate callback.
            window.decorView.post {
                val engine = tts ?: return@post
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String) = Unit
                    override fun onDone(id: String) = runOnUiThread { speech.completed(id, true) }
                    @Deprecated("Platform callback")
                    override fun onError(id: String) = runOnUiThread { speech.completed(id, false) }
                    override fun onError(id: String, code: Int) = runOnUiThread { speech.completed(id, false) }
                    override fun onStop(id: String, interrupted: Boolean) = runOnUiThread { speech.completed(id, false) }
                })
                engine.setSpeechRate(0.88f)
                engine.setPitch(1.0f)
                val installed = try { engine.voices.orEmpty().map {
                    LegacySpeech.OfflineVoice(it, it.name, it.locale.language, it.locale.country, it.isNetworkConnectionRequired,
                        !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))
                } } catch (_: RuntimeException) { emptyList() }
                speech.initialise(status == TextToSpeech.SUCCESS, installed)
            }
        }
        setContent { BastiApp() }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun BastiApp() {
        var screen by remember { mutableStateOf(ShellScreen.HOME) }
        var language by remember { mutableStateOf(prefs.getString("lang", "en") ?: "en") }
        var sound by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
        var audioMode by remember { mutableStateOf(prefs.getString("audioMode", if (sound) "all" else "off") ?: "all") }
        var round by remember { mutableStateOf(prefs.getInt("round", 5)) }
        var numberMax by remember { mutableStateOf(prefs.getInt("numberMax", 10)) }
        var webMode by remember { mutableStateOf("verbs") }

        MaterialTheme(colorScheme = BastiColors) {
            fun back() {
                cancelAudio()
                if (screen == ShellScreen.WEB) webView?.evaluateJavascript("navigateBack()", null)
                else screen = ShellScreen.HOME
            }
            BackHandler(enabled = screen != ShellScreen.HOME) { back() }
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                when (screen) {
                                    ShellScreen.HOME -> if (language == "de") "Bastis Lernabenteuer" else "Basti's Learning Adventure"
                                    ShellScreen.OPTIONS -> if (language == "de") "Optionen" else "Options"
                                    ShellScreen.WEB -> if (language == "de") "Lernen" else "Learning"
                                },
                                fontWeight = FontWeight.Black
                            )
                        },
                        navigationIcon = { if (screen != ShellScreen.HOME) OutlinedButton(onClick = { back() }, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("←") } else Unit },
                        actions = {
                            OutlinedButton(onClick = { cancelAudio(); screen = ShellScreen.OPTIONS }) { Text(if (language == "de") "⚙ Optionen" else "⚙ Options") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Gold),
                        windowInsets = WindowInsets.statusBars
                    )
                },
            ) { padding ->
                when (screen) {
                    ShellScreen.HOME -> NativeHome(language, padding) { card ->
                        if (card.play) return@NativeHome
                        webMode = card.mode ?: "verbs"
                        if (card.verbExplorer) webMode = "verbExplorer"
                        screen = ShellScreen.WEB
                    }
                    ShellScreen.OPTIONS -> NativeOptions(language, audioMode, round, numberMax, padding, audioStatus,
                        onLanguage = { language = it; saveAndSync(it, audioMode, round, numberMax) },
                        onAudioMode = { audioMode = it; saveAndSync(language, it, round, numberMax) },
                        onRound = { round = it; saveAndSync(language, audioMode, it, numberMax) },
                        onNumberMax = { numberMax = it; saveAndSync(language, audioMode, round, it) },
                        onResetTutorials = {
                            val epoch = prefs.getLong("tutorialResetEpoch", 0) + 1
                            prefs.edit().putLong("tutorialResetEpoch", epoch).apply()
                            webView?.evaluateJavascript("syncTutorialReset($epoch)", null)
                        })
                    // Recovery guidance stays in parent Options; learning UI remains usable.
                    ShellScreen.WEB -> ExistingLearningSurface(webMode, padding) { screen = ShellScreen.HOME }
                }
            }
        }
    }

    private fun saveAndSync(language: String, audioMode: String, round: Int, numberMax: Int) {
        cancelAudio()
        val sound = audioMode != "off"
        prefs.edit().putString("lang", language).putString("audioMode", audioMode).putBoolean("sound", sound).putInt("round", round).putInt("numberMax", numberMax).apply()
        webView?.evaluateJavascript("setLang('$language');setAudioMode('$audioMode');setRound($round);setNumberMax($numberMax)", null)
    }

    @Composable
    private fun ExistingLearningSurface(mode: String, padding: PaddingValues, onHome: () -> Unit) {
        DisposableEffect(mode) {
            onDispose {
                cancelAudio()
                webView?.let { it.removeJavascriptInterface("Android"); it.stopLoading(); it.destroy() }
                webView = null
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.White),
            factory = { context -> createWebView(context, mode, onHome).also { webView = it } }
        )
    }

    private fun createWebView(context: Context, mode: String, onHome: () -> Unit): WebView {
        return WebView(context).apply {
            setBackgroundColor(Color.White.value.toInt())
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.blockNetworkLoads = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.mediaPlaybackRequiresUserGesture = false
            settings.textZoom = 100
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
                override fun onPageFinished(view: WebView, url: String) {
                    view.evaluateJavascript(syncScript(mode), null)
                }
            }
            addJavascriptInterface(AppBridge(context, this, onHome), "Android")
            loadUrl("file:///android_asset/index.html")
        }
    }

    private fun syncScript(mode: String): String {
        val language = prefs.getString("lang", "en") ?: "en"
        val sound = prefs.getBoolean("sound", true)
        val round = prefs.getInt("round", 5)
        val numberMax = prefs.getInt("numberMax", 10)
        val audioMode = prefs.getString("audioMode", if (sound) "all" else "off") ?: "all"
        return "syncTutorialReset(${prefs.getLong("tutorialResetEpoch", 0)});setLang('$language');setAudioMode('$audioMode');setRound($round);setNumberMax($numberMax);if(typeof startShellMode==='function')startShellMode('$mode')"
    }

    private inner class AppBridge(private val context: Context, private val owner: WebView, private val onHome: () -> Unit) {
        @JavascriptInterface fun onWebHome() = runOnUiThread { if (webView === owner) { cancelAudio(); onHome() } }
        @JavascriptInterface fun speak(text: String, language: String, requestId: String) = runOnUiThread {
            if (webView !== owner || !foreground || prefs.getString("audioMode", "all") == "off") return@runOnUiThread
            val id = (++utteranceSerial).toString()
            speech.request(text, language, id) { success ->
                if (webView === owner) owner.evaluateJavascript("speechResult(${JSONObject.quote(requestId)},$success)", null)
            }
        }
        @JavascriptInterface fun stopSpeaking() = runOnUiThread { if (webView === owner) speech.stop() }
        @JavascriptInterface fun vibrate() {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") vibrator.vibrate(45)
            }
        }
    }

    private fun cancelAudio() {
        speech.stop()
        webView?.evaluateJavascript("suspendAudio()", null)
    }
    override fun onPause() { foreground = false; cancelAudio(); webView?.onPause(); super.onPause() }
    override fun onResume() { super.onResume(); foreground = true; webView?.onResume() }
    override fun onDestroy() { speech.stop(); tts?.shutdown(); tts = null; webView?.destroy(); webView = null; super.onDestroy() }
}

@Composable
private fun NativeHome(language: String, padding: PaddingValues, onCard: (HomeCard) -> Unit) {
    val learn = homeCards.take(5); val practice = homeCards.slice(5..8); val play = homeCards.drop(9)
    Column(Modifier.fillMaxSize().padding(padding).background(Grass).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Text(if (language == "de") "Bereit für ein Abenteuer?" else "Ready for an adventure?", modifier = Modifier.fillMaxWidth().padding(top = 20.dp), textAlign = TextAlign.Center, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Ink)
        Text(if (language == "de") "Wähle ein Spiel. Jede Runde ist kurz und einfach." else "Choose a game. Each round is short and simple.", modifier = Modifier.fillMaxWidth().padding(4.dp), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(12.dp))
        HomeGroup(if (language == "de") "LERNEN" else "LEARN", learn, language, onCard)
        HomeGroup(if (language == "de") "ÜBEN" else "PRACTICE", practice, language, onCard)
        HomeGroup(if (language == "de") "SPIELEN" else "PLAY", play, language, onCard)
    }
}

@Composable
private fun HomeGroup(title: String, cards: List<HomeCard>, language: String, onCard: (HomeCard) -> Unit) {
    Text(title, modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 5.dp), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Ink)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        cards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { card ->
                    Card(onClick = { onCard(card) }, colors = CardDefaults.cardColors(containerColor = if (card.play) PaleYellow else Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.weight(1f).height(142.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(card.emoji, fontSize = 34.sp)
                            Text(if (language == "de") card.titleDe else card.titleEn, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Ink)
                            Text(if (language == "de") card.descriptionDe else card.descriptionEn, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF607788))
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NativeOptions(language: String, audioMode: String, round: Int, numberMax: Int, padding: PaddingValues, audioStatus: String, onLanguage: (String) -> Unit, onAudioMode: (String) -> Unit, onRound: (Int) -> Unit, onNumberMax: (Int) -> Unit, onResetTutorials: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).background(Color(0xFFF9FCFE)).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(if (language == "de") "Optionen" else "Options", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Ink)
        SettingCard(if (language == "de") "Sprache" else "Language") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = language == "en", onClick = { onLanguage("en") }, label = { Text("🇬🇧 English") }); FilterChip(selected = language == "de", onClick = { onLanguage("de") }, label = { Text("🇩🇪 Deutsch") }) }
        }
        SettingCard(if (language == "de") "Audio-Hilfe" else "Audio guidance") {
            if (audioStatus != "ready") Text(if (language == "de")
                "Sprachausgabe: Bitte installierte Offline-Stimmen für Englisch und Deutsch in den Geräte-Einstellungen prüfen. Nach Änderungen die App neu starten."
                else "Speech: Check installed offline English and German voices in device settings. Restart the app after changes.")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = audioMode == "all", onClick = { onAudioMode("all") }, label = { Text(if (language == "de") "Alles vorlesen" else "Read everything") })
                FilterChip(selected = audioMode == "questions", onClick = { onAudioMode("questions") }, label = { Text(if (language == "de") "Nur Fragen und Anweisungen" else "Questions and instructions only") })
                FilterChip(selected = audioMode == "off", onClick = { onAudioMode("off") }, label = { Text(if (language == "de") "Ton aus" else "Sound off") })
                Button(onClick = onResetTutorials) { Text(if (language == "de") "Einführungen wieder anhören" else "Replay activity introductions") }
            }
        }
        SettingCard(if (language == "de") "Fragen pro Runde" else "Questions per round") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = round == 5, onClick = { onRound(5) }, label = { Text("5") }); FilterChip(selected = round == 10, onClick = { onRound(10) }, label = { Text("10") }) }
        }
        SettingCard(if (language == "de") "Zahlen-Level" else "Number level") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(10, 20, 50, 100).forEach { max -> FilterChip(selected = numberMax == max, onClick = { onNumberMax(max) }, label = { Text(max.toString()) }) } }
            Text(if (language == "de") "Aktuelles Maximum: $numberMax" else "Current maximum: $numberMax", modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun SettingCard(title: String, content: @Composable () -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Ink); HorizontalDivider(); content() } } }

private val BastiColors = androidx.compose.material3.lightColorScheme(primary = Color(0xFF2E7D5B), secondary = Color(0xFFE59D28), background = Color(0xFFF9FCFE), surface = Color.White, onSurface = Ink)
