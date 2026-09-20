package com.bellfamily.bastischool;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.WindowInsets;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private WebView webView;
    private TextToSpeech tts;
    private boolean ttsReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
                tts.setSpeechRate(0.88f);
                tts.setPitch(1.0f);
            }
        });

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setBlockNetworkLoads(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !"file:///android_asset/index.html".equals(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !"file:///android_asset/index.html".equals(url);
            }
        });
        webView.addJavascriptInterface(new AppBridge(this), "Android");
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
        // Android 15 draws edge to edge; keep every control outside system bars.
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            webView.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
            webView.requestApplyInsets();
        }
    }

    public class AppBridge {
        private final Context context;
        AppBridge(Context context) { this.context = context; }

        @JavascriptInterface
        public void speak(String text, String language) {
            runOnUiThread(() -> {
                if (tts == null || !ttsReady) return;
                Locale locale = "de".equalsIgnoreCase(language) ? Locale.GERMANY : Locale.UK;
                // Engines may expose cloud voices; only use installed local voices.
                Set<Voice> voices = tts.getVoices();
                Voice chosen = null;
                if (voices != null) {
                    for (Voice voice : voices) {
                        if (voice.isNetworkConnectionRequired()
                                || !voice.getLocale().getLanguage().equals(locale.getLanguage())
                                || (voice.getFeatures() != null && voice.getFeatures().contains(
                                        TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))) continue;
                        if (chosen == null || voice.getLocale().equals(locale)) chosen = voice;
                    }
                }
                if (chosen != null && tts.setVoice(chosen) == TextToSpeech.SUCCESS) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "basti-school-question");
                }
            });
        }

        @JavascriptInterface
        public void stopSpeaking() {
            runOnUiThread(() -> { if (tts != null) tts.stop(); });
        }

        @JavascriptInterface
        public void vibrate() {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) return;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(45);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView == null) { super.onBackPressed(); return; }
        webView.evaluateJavascript("typeof navigateBack === 'function' && navigateBack()", result -> {
            if (!"true".equals(result)) finish();
        });
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.evaluateJavascript("typeof stopSpeech === 'function' && stopSpeech()", null);
            webView.onPause();
        }
        if (tts != null) tts.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        ttsReady = false;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
