package com.bellfamily.bastischool.ui.months

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bellfamily.bastischool.learning.content.SeasonIds
import com.bellfamily.bastischool.learning.models.ContentId
import com.bellfamily.bastischool.learning.months.MonthContent
import com.bellfamily.bastischool.learning.months.MonthsWeather
import kotlin.math.*

/** Matches the existing celebration policy, also observing animation-scale changes while visible. */
@Composable
internal fun weatherMotionAllowed(): Boolean {
    val resolver = LocalContext.current.contentResolver
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    fun enabled() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    var systemMotion by remember { mutableStateOf(enabled()) }
    var resumed by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle, resolver) {
        val lifecycleObserver = LifecycleEventObserver { _, _ ->
            resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            systemMotion = enabled()
        }
        val settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { systemMotion = enabled() }
        }
        lifecycle.addObserver(lifecycleObserver)
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, settingsObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            resolver.unregisterContentObserver(settingsObserver)
        }
    }
    return resumed && systemMotion
}

private data class WeatherScene(val month: ContentId, val weather: MonthsWeather)
private data class WeatherParticle(val x: Float, val y: Float, val size: Float)

/** One stationary, circle-clipped layer; its only clock is removed on pause or reduced motion. */
@Composable
internal fun MonthsWeatherOverlay(month: ContentId, weather: MonthsWeather, motion: Boolean,
    modifier: Modifier = Modifier) {
    val target = WeatherScene(month, weather)
    var displayed by remember { mutableStateOf(target) }
    val opacity = remember { Animatable(1f) }
    // Cancellation replaces the pending target, never accumulating crossfade layers.
    LaunchedEffect(target, motion) {
        if (!motion) {
            displayed = target
            opacity.snapTo(1f)
        } else if (displayed != target) {
            opacity.animateTo(0f, tween(160))
            displayed = target
            opacity.animateTo(1f, tween(240))
        } else opacity.animateTo(1f, tween(240))
    }
    val phase: State<Float> = if (motion) {
        rememberInfiniteTransition(label = "seasonal-weather").animateFloat(0f, 1f,
            infiniteRepeatable(tween(12_000, easing = LinearEasing)), label = "weather-drift")
    } else remember { mutableFloatStateOf(.25f) }
    val particles = remember {
        List(24) { i -> WeatherParticle(((i * 37 + 13) % 101) / 101f,
            ((i * 61 + 7) % 103) / 103f, .012f + (i % 3) * .005f) }
    }
    val wind = remember { Path().apply {
        moveTo(0f, .06f); cubicTo(.12f, -.03f, .25f, .15f, .37f, .06f)
        cubicTo(.44f, 0f, .37f, -.04f, .33f, 0f)
    } }
    Canvas(modifier.clip(CircleShape).testTag("wheel-weather-overlay")) {
        val season = MonthContent.month(displayed.month).season
        val half = size.minDimension / 2f
        val left = if (season == SeasonIds.SPRING || season == SeasonIds.SUMMER) half else 0f
        val top = if (season == SeasonIds.AUTUMN || season == SeasonIds.SUMMER) half else 0f
        clipRect(left, top, left + half, top + half) {
            translate(left, top) {
                scale(half, half, pivot = Offset.Zero) {
                    paintWeather(displayed.weather, season == SeasonIds.AUTUMN, season == SeasonIds.SUMMER, phase.value,
                        opacity.value, particles, wind)
                }
            }
        }
    }
}

private fun DrawScope.paintWeather(weather: MonthsWeather, autumn: Boolean, summer: Boolean, phase: Float, alpha: Float,
    particles: List<WeatherParticle>, wind: Path) {
    val ice = Color(0xFFB4E9FF).copy(alpha = alpha * .85f)
    val white = Color.White.copy(alpha = alpha * .85f)
    when (weather) {
        MonthsWeather.SNOW -> repeat(16) { i ->
            val p = particles[i]
            val x = (p.x + .045f * sin(phase * 2f * PI.toFloat() + i) + 1f) % 1f
            val y = (p.y + phase * (1 + i % 2)) % 1f
            val c = Offset(x, y)
            drawCircle(Color(0xFF547D99).copy(alpha = alpha * .24f), p.size * 1.65f, c)
            repeat(3) { branch ->
                val a = branch * PI / 3
                val d = Offset(cos(a).toFloat(), sin(a).toFloat()) * p.size
                drawLine(white, c - d, c + d, .009f, StrokeCap.Round)
            }
        }
        MonthsWeather.FROST -> {
            repeat(6) { i ->
                val c = Offset(.12f + (i % 3) * .32f, .18f + (i / 3) * .62f)
                val sparkle = .8f + .15f * sin(phase * 2f * PI.toFloat() + i)
                repeat(6) { ray ->
                    val a = ray * PI / 3
                    val d = Offset(cos(a).toFloat(), sin(a).toFloat())
                    drawLine(ice.copy(alpha = ice.alpha * sparkle), c, c + d * .095f, .012f, StrokeCap.Round)
                    val side = Offset(-d.y, d.x)
                    drawLine(white, c + d * .055f, c + d * .075f + side * .025f, .007f)
                    drawLine(white, c + d * .055f, c + d * .075f - side * .025f, .007f)
                }
            }
        }
        MonthsWeather.LIGHT_RAIN, MonthsWeather.RAIN -> {
            clouds(phase, alpha * .7f)
            val count = if (weather == MonthsWeather.LIGHT_RAIN) 12 else 24
            repeat(count) { i ->
                val p = particles[i]
                val y = (p.y + phase * 5f) % 1f
                val x = (p.x - phase + 1f) % 1f
                val start = Offset(x, y)
                val end = start + Offset(-.025f, .065f)
                drawLine(Color.White.copy(alpha = alpha * .7f), start, end, .019f, StrokeCap.Round)
                drawLine(Color(0xFF4CA8DA).copy(alpha = alpha * .9f), start, end, .010f, StrokeCap.Round)
            }
        }
        MonthsWeather.SUNSHINE, MonthsWeather.SUN_AND_CLOUDS -> {
            // Summer's supplied illustration already contains a sun: illuminate it rather
            // than placing a second sun beside it. Other quadrants have no painted sun.
            val c = if (summer) Offset(.80f, .14f) else Offset(.57f, .28f)
            val glow = .15f + .025f * sin(phase * 2f * PI.toFloat())
            drawCircle(Color(0xFFFFD54F).copy(alpha = alpha * glow), .32f, c)
            drawCircle(Color(0xFFFFE082).copy(alpha = alpha * .25f), .23f, c)
            repeat(10) { i ->
                val a = i * PI / 5 + sin(phase * 2 * PI) * .08
                val d = Offset(cos(a).toFloat(), sin(a).toFloat())
                drawLine(Color(0xFFFFDF6B).copy(alpha = alpha * .8f), c + d * .15f,
                    c + d * .23f, .018f, StrokeCap.Round)
            }
            drawCircle(Color(0xFFFFE579).copy(alpha = alpha * .8f), .12f, c)
            if (weather == MonthsWeather.SUN_AND_CLOUDS) clouds(phase, alpha * .75f)
        }
        MonthsWeather.WIND -> {
            repeat(3) { i ->
                translate(((phase + i / 3f) % 1f) * 1.5f - .4f, .2f + i * .25f) {
                    drawPath(wind, Color(0xFF426677).copy(alpha = alpha * .3f), style = Stroke(.026f, cap = StrokeCap.Round))
                    drawPath(wind, white, style = Stroke(.012f, cap = StrokeCap.Round))
                }
            }
            repeat(5) { i ->
                val p = particles[i]
                val c = Offset((p.x + phase) % 1f, (p.y + phase) % 1f)
                if (autumn) rotate(phase * 360f + i * 30f, c) {
                    drawOval(Color(if (i % 2 == 0) 0xFFEAA344 else 0xFFB9572D).copy(alpha = alpha * .9f),
                        c - Offset(.035f, .017f), Size(.07f, .034f))
                    drawLine(Color(0xFF754629).copy(alpha = alpha * .8f), c - Offset(.03f, 0f), c + Offset(.03f, 0f), .005f)
                } else drawCircle(white, .009f, c)
            }
        }
        MonthsWeather.CLOUDS -> clouds(phase, alpha)
    }
}

private fun DrawScope.clouds(phase: Float, alpha: Float) {
    repeat(3) { i ->
        val x = ((phase + i / 3f) % 1f) * 1.5f - .4f
        val y = .15f + i * .25f
        val cloud = Color(0xFFE8F0F6).copy(alpha = alpha * .48f)
        drawOval(Color(0xFF8299AF).copy(alpha = alpha * .25f), Offset(x, y + .055f), Size(.35f, .10f))
        drawOval(cloud, Offset(x, y + .035f), Size(.35f, .10f))
        drawCircle(cloud, .07f, Offset(x + .12f, y + .04f))
        drawCircle(cloud, .055f, Offset(x + .23f, y + .05f))
    }
}
