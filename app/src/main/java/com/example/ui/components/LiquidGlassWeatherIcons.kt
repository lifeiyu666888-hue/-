package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.HourlyIconType
import com.example.model.MetricIconType
import com.example.model.WeatherCondition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Apple Design Award / iOS 18 Multilayered 3D Weather Icon Renderer.
 * Uses volumetric subsurface scattering, soft optical corona blooms (zero harsh stick-rays),
 * multi-puff 3D cloud shading with ambient occlusion, and razor-thin 0.8dp specular rims.
 */
@Composable
fun Animated3DLiquidGlassWeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    iconSize: Dp = 82.dp,
    speedMultiplier: Float = 1f,
    refractionStrength: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "apple_3d_weather_icon")
    val safeSpeed = speedMultiplier.coerceIn(0.3f, 2.5f)

    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (24000 / safeSpeed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slow_rotation"
    )

    val floatNorm by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (3200 / safeSpeed).toInt(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_norm"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (2600 / safeSpeed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (2200 / safeSpeed).toInt(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Canvas(modifier = modifier.size(iconSize)) {
        val w = size.width
        val h = size.height

        when (condition) {
            WeatherCondition.SUNNY -> drawAppleSunny3DIcon(
                w = w,
                h = h,
                rotation = slowRotation,
                floatNorm = floatNorm,
                breathe = breathe,
                refraction = refractionStrength
            )
            WeatherCondition.WINDY -> drawAppleWindy3DIcon(
                w = w,
                h = h,
                floatNorm = floatNorm,
                phase = wavePhase,
                breathe = breathe,
                refraction = refractionStrength
            )
            WeatherCondition.RAINY -> drawAppleRainy3DIcon(
                w = w,
                h = h,
                floatNorm = floatNorm,
                phase = wavePhase,
                breathe = breathe,
                refraction = refractionStrength
            )
            WeatherCondition.SNOWY -> drawAppleSnowy3DIcon(
                w = w,
                h = h,
                rotation = slowRotation,
                floatNorm = floatNorm,
                phase = wavePhase,
                breathe = breathe,
                refraction = refractionStrength
            )
        }
    }
}

/**
 * 1. Apple iOS 18 Sunny 3D Icon:
 * - Soft multi-layer optical solar corona bloom (no cartoon stick rays!)
 * - Feathered volumetric light petals rotating with BlendMode.Screen
 * - Luminous 3D golden orb with internal subsurface gradient
 * - Sculpted 3D translucent cloud with warm golden subsurface scattering & hairline specular rim
 */
private fun DrawScope.drawAppleSunny3DIcon(
    w: Float,
    h: Float,
    rotation: Float,
    floatNorm: Float,
    breathe: Float,
    refraction: Float
) {
    val sunCenter = Offset(w * 0.42f, h * 0.38f)
    val sunR = w * 0.23f

    // 1. Outer Atmospheric Solar Bloom
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color(0xFFFFF3B0).copy(alpha = 0.65f * refraction),
            0.40f to Color(0xFFFFB703).copy(alpha = 0.28f * refraction),
            0.75f to Color(0xFFFB8500).copy(alpha = 0.08f * refraction),
            1.0f to Color.Transparent,
            center = sunCenter,
            radius = sunR * 2.25f * breathe
        ),
        radius = sunR * 2.25f * breathe,
        center = sunCenter,
        blendMode = BlendMode.Screen
    )

    // 2. Soft Optical Light Petals (Feathered radial cones instead of hard lines)
    rotate(degrees = rotation, pivot = sunCenter) {
        for (i in 0 until 6) {
            rotate(degrees = i * 30f, pivot = sunCenter) {
                drawOval(
                    brush = Brush.radialGradient(
                        0.0f to Color(0xFFFFFBEB).copy(alpha = 0.26f * refraction),
                        0.6f to Color(0xFFFDE047).copy(alpha = 0.08f * refraction),
                        1.0f to Color.Transparent,
                        center = sunCenter,
                        radius = sunR * 1.75f * breathe
                    ),
                    topLeft = Offset(sunCenter.x - sunR * 0.42f, sunCenter.y - sunR * 1.75f * breathe),
                    size = Size(sunR * 0.84f, sunR * 3.5f * breathe),
                    blendMode = BlendMode.Screen
                )
            }
        }
    }

    // 3. 3D Luminous Solar Sphere (Apple Weather exact warm gold gradient)
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color(0xFFFFFFFF),
            0.25f to Color(0xFFFFF8D6),
            0.65f to Color(0xFFFFD60A),
            0.92f to Color(0xFFFF9F1C),
            1.0f to Color(0xFFF77F00),
            center = Offset(sunCenter.x - sunR * 0.25f, sunCenter.y - sunR * 0.28f),
            radius = sunR * 1.35f
        ),
        radius = sunR,
        center = sunCenter
    )

    // 4. Foreground 3D Apple Sculpted Glass Cloud with Subsurface Scattering
    withTransform({
        translate(left = w * 0.04f, top = floatNorm * (h * 0.024f))
    }) {
        drawApple3DCloud(
            w = w,
            h = h,
            topColor = Color.White.copy(alpha = 0.95f),
            bottomColor = Color(0xFFE2E8F0).copy(alpha = 0.72f),
            subsurfaceGlowColor = Color(0xFFFFD166).copy(alpha = 0.55f * refraction),
            subsurfaceCenter = Offset(w * 0.40f, h * 0.48f),
            ambientOcclusionColor = Color(0xFF64748B).copy(alpha = 0.18f)
        )
    }
}

/**
 * 2. Apple iOS 18 Windy 3D Icon:
 * - Sculpted 3D cloud in the background
 * - Three variable-width tapered aerodynamic Bezier streams with traveling specular light pulse
 */
private fun DrawScope.drawAppleWindy3DIcon(
    w: Float,
    h: Float,
    floatNorm: Float,
    phase: Float,
    breathe: Float,
    refraction: Float
) {
    // 1. Background 3D Cloud
    withTransform({
        translate(left = w * 0.05f, top = -h * 0.12f + floatNorm * (h * 0.02f))
        scale(0.86f, 0.86f, pivot = Offset(w * 0.5f, h * 0.45f))
    }) {
        drawApple3DCloud(
            w = w,
            h = h,
            topColor = Color.White.copy(alpha = 0.88f),
            bottomColor = Color(0xFFCBD5E1).copy(alpha = 0.58f),
            subsurfaceGlowColor = Color(0xFF5EEAD4).copy(alpha = 0.42f * refraction),
            subsurfaceCenter = Offset(w * 0.52f, h * 0.50f),
            ambientOcclusionColor = Color(0xFF0F172A).copy(alpha = 0.22f)
        )
    }

    // 2. Three Sleek Apple SF-Symbol Style Tapered Wind Streams
    val pulseProgress = (phase / (2f * PI.toFloat())).coerceIn(0f, 1f)
    val streams = listOf(
        Triple(h * 0.45f, w * 0.12f, w * 0.80f),
        Triple(h * 0.60f, w * 0.06f, w * 0.90f),
        Triple(h * 0.75f, w * 0.20f, w * 0.66f)
    )

    streams.forEachIndexed { idx, (yBase, startX, endX) ->
        val driftX = sin(phase + idx * 1.1f) * (w * 0.025f)
        val sx = startX + driftX
        val ex = endX + driftX * 0.5f
        val curlSign = if (idx == 0) -1f else 1f
        val curlR = w * (if (idx == 1) 0.08f else 0.062f)

        val streamPath = Path().apply {
            moveTo(sx, yBase)
            cubicTo(
                sx + (ex - sx) * 0.35f, yBase - h * 0.03f * sin(phase + idx),
                sx + (ex - sx) * 0.70f, yBase + h * 0.03f * sin(phase + idx),
                ex, yBase
            )
            cubicTo(
                ex + curlR * 1.05f, yBase,
                ex + curlR * 1.05f, yBase + curlSign * curlR * 1.55f,
                ex, yBase + curlSign * curlR * 1.55f
            )
            cubicTo(
                ex - curlR * 0.72f, yBase + curlSign * curlR * 1.55f,
                ex - curlR * 0.65f, yBase + curlSign * curlR * 0.55f,
                ex - curlR * 0.12f, yBase + curlSign * curlR * 0.58f
            )
        }

        // Soft drop shadow
        withTransform({ translate(left = 0f, top = h * 0.018f) }) {
            drawPath(
                path = streamPath,
                color = Color.Black.copy(alpha = 0.18f),
                style = Stroke(width = w * 0.048f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Traveling luminous highlight brush along the stream
        val highlightCenter = ((pulseProgress + idx * 0.28f) % 1f)
        val leftStop = (highlightCenter - 0.25f).coerceIn(0f, 1f)
        val rightStop = (highlightCenter + 0.25f).coerceIn(0f, 1f)

        drawPath(
            path = streamPath,
            brush = Brush.horizontalGradient(
                0.0f to Color.White.copy(alpha = 0.35f),
                leftStop to Color(0xFFBAE6FD).copy(alpha = 0.75f),
                highlightCenter to Color.White,
                rightStop to Color(0xFF5EEAD4).copy(alpha = 0.85f),
                1.0f to Color.White.copy(alpha = 0.45f),
                startX = sx,
                endX = ex + curlR
            ),
            style = Stroke(width = w * 0.042f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * 3. Apple iOS 18 Rainy Storm 3D Icon:
 * - Dual-layer 3D sculpted storm clouds with deep slate-blue ambient occlusion
 * - Internal sheet-lightning illumination glowing from within the translucent cloud
 * - Crisp golden lightning bolt & sleek motion-blurred 3D glass raindrops
 */
private fun DrawScope.drawAppleRainy3DIcon(
    w: Float,
    h: Float,
    floatNorm: Float,
    phase: Float,
    breathe: Float,
    refraction: Float
) {
    val flash = ((sin(phase * 2f) + 1f) * 0.5f).coerceIn(0f, 1f)

    // 1. Back Storm Cloud Puff
    withTransform({
        translate(left = -w * 0.07f, top = -h * 0.16f - floatNorm * h * 0.015f)
        scale(0.82f, 0.82f, pivot = Offset(w * 0.5f, h * 0.5f))
    }) {
        drawApple3DCloud(
            w = w,
            h = h,
            topColor = Color(0xFF94A3B8).copy(alpha = 0.78f),
            bottomColor = Color(0xFF334155).copy(alpha = 0.85f),
            subsurfaceGlowColor = Color(0xFF818CF8).copy(alpha = 0.35f * refraction),
            subsurfaceCenter = Offset(w * 0.5f, h * 0.5f),
            ambientOcclusionColor = Color(0xFF0F172A).copy(alpha = 0.45f)
        )
    }

    // 2. Foreground Storm Cloud with Internal Lightning Subsurface Glow
    withTransform({
        translate(left = 0f, top = -h * 0.10f + floatNorm * h * 0.02f)
    }) {
        drawApple3DCloud(
            w = w,
            h = h,
            topColor = Color(0xFFF1F5F9).copy(alpha = 0.92f),
            bottomColor = Color(0xFF475569).copy(alpha = 0.84f),
            subsurfaceGlowColor = Color(0xFFFDE047).copy(alpha = (0.22f + 0.52f * flash) * refraction),
            subsurfaceCenter = Offset(w * 0.48f, h * 0.56f),
            ambientOcclusionColor = Color(0xFF1E293B).copy(alpha = 0.40f)
        )
    }

    // 3. Sleek Apple-Style Golden Lightning Bolt
    val boltPath = Path().apply {
        moveTo(w * 0.52f, h * 0.46f)
        lineTo(w * 0.40f, h * 0.63f)
        lineTo(w * 0.50f, h * 0.63f)
        lineTo(w * 0.44f, h * 0.84f)
        lineTo(w * 0.61f, h * 0.59f)
        lineTo(w * 0.50f, h * 0.59f)
        close()
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFDE047).copy(alpha = (0.30f + 0.35f * flash) * refraction),
                Color.Transparent
            ),
            center = Offset(w * 0.50f, h * 0.65f),
            radius = w * 0.24f
        ),
        radius = w * 0.24f,
        center = Offset(w * 0.50f, h * 0.65f),
        blendMode = BlendMode.Screen
    )
    drawPath(
        path = boltPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFFD60A), Color(0xFFFF9F1C)),
            startY = h * 0.46f,
            endY = h * 0.84f
        )
    )

    // 4. Three Sleek Slanted Glass Rain Streaks
    val drops = listOf(
        Offset(w * 0.28f, h * 0.64f) to 0f,
        Offset(w * 0.70f, h * 0.62f) to 2.1f,
        Offset(w * 0.34f, h * 0.77f) to 4.2f
    )
    drops.forEach { (origin, offsetPhase) ->
        val prog = ((phase + offsetPhase) % (2f * PI.toFloat())) / (2f * PI.toFloat())
        val dy = prog * (h * 0.15f)
        val dx = -prog * (w * 0.045f)
        val alpha = sin(prog * PI.toFloat()).coerceIn(0.25f, 1f)
        val start = Offset(origin.x + dx, origin.y + dy)
        val end = Offset(start.x - w * 0.035f, start.y + h * 0.11f)

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.2f * alpha),
                    Color(0xFF64D2FF).copy(alpha = 0.95f * alpha)
                ),
                start = start,
                end = end
            ),
            start = start,
            end = end,
            strokeWidth = w * 0.036f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 4. Apple iOS 18 Snowy 3D Icon:
 * - Sculpted 3D frost cloud
 * - Delicate 6-fold crystalline snowflake with soft optical bloom
 */
private fun DrawScope.drawAppleSnowy3DIcon(
    w: Float,
    h: Float,
    rotation: Float,
    floatNorm: Float,
    phase: Float,
    breathe: Float,
    refraction: Float
) {
    // 1. Upper 3D Cloud
    withTransform({
        translate(left = 0f, top = -h * 0.13f + floatNorm * h * 0.02f)
        scale(0.90f, 0.90f, pivot = Offset(w * 0.5f, h * 0.42f))
    }) {
        drawApple3DCloud(
            w = w,
            h = h,
            topColor = Color.White.copy(alpha = 0.95f),
            bottomColor = Color(0xFFBAE6FD).copy(alpha = 0.62f),
            subsurfaceGlowColor = Color(0xFF7DD3FC).copy(alpha = 0.48f * refraction),
            subsurfaceCenter = Offset(w * 0.5f, h * 0.5f),
            ambientOcclusionColor = Color(0xFF334155).copy(alpha = 0.22f)
        )
    }

    // 2. Delicate Crystalline Snowflake + Soft Bokeh Snow Dots
    val flakeCenter = Offset(w * 0.50f, h * 0.68f)
    val flakeR = w * 0.17f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE0F2FE).copy(alpha = 0.50f * refraction),
                Color.Transparent
            ),
            center = flakeCenter,
            radius = flakeR * 1.9f * breathe
        ),
        radius = flakeR * 1.9f * breathe,
        center = flakeCenter,
        blendMode = BlendMode.Screen
    )

    rotate(degrees = rotation, pivot = flakeCenter) {
        for (i in 0 until 6) {
            rotate(degrees = i * 60f, pivot = flakeCenter) {
                val tip = Offset(flakeCenter.x, flakeCenter.y - flakeR)
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFF7DD3FC)),
                        startY = tip.y,
                        endY = flakeCenter.y
                    ),
                    start = flakeCenter,
                    end = tip,
                    strokeWidth = w * 0.028f,
                    cap = StrokeCap.Round
                )
                val branchY = flakeCenter.y - flakeR * 0.58f
                val wing = flakeR * 0.28f
                val wingY = flakeCenter.y - flakeR * 0.80f
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(flakeCenter.x, branchY),
                    end = Offset(flakeCenter.x - wing, wingY),
                    strokeWidth = w * 0.022f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(flakeCenter.x, branchY),
                    end = Offset(flakeCenter.x + wing, wingY),
                    strokeWidth = w * 0.022f,
                    cap = StrokeCap.Round
                )
            }
        }
        drawCircle(Color.White, radius = w * 0.032f, center = flakeCenter)
    }

    // Soft falling snow bokeh dots on left & right
    val leftFlake = Offset(w * 0.26f, h * 0.66f + sin(phase) * h * 0.04f)
    val rightFlake = Offset(w * 0.74f, h * 0.64f - cos(phase) * h * 0.04f)
    drawCircle(Color.White.copy(alpha = 0.85f), radius = w * 0.032f, center = leftFlake)
    drawCircle(Color(0xFFBAE6FD).copy(alpha = 0.85f), radius = w * 0.028f, center = rightFlake)
}

/**
 * Renders an authentic Apple iOS 18 3D volumetric cloud:
 * - Multi-puff sculpted silhouette
 * - Soft bottom ambient occlusion shading
 * - Subsurface scattering glow from behind
 * - Individual spherical 3D puff highlights so the cloud has real volume rather than looking flat
 * - Razor-thin 0.8dp top specular rim
 */
private fun DrawScope.drawApple3DCloud(
    w: Float,
    h: Float,
    topColor: Color,
    bottomColor: Color,
    subsurfaceGlowColor: Color,
    subsurfaceCenter: Offset,
    ambientOcclusionColor: Color
) {
    val sx = w / 80f
    val sy = h / 80f
    val cloudPath = Path().apply {
        moveTo(24f * sx, 60f * sy)
        lineTo(61f * sx, 60f * sy)
        cubicTo(68.2f * sx, 60f * sy, 74f * sx, 54.2f * sy, 74f * sx, 47f * sy)
        cubicTo(74f * sx, 40.5f * sy, 69.2f * sx, 35.1f * sy, 62.9f * sx, 34.2f * sy)
        cubicTo(61.0f * sx, 25.5f * sy, 53.2f * sx, 19f * sy, 43.9f * sx, 19f * sy)
        cubicTo(33.4f * sx, 19f * sy, 24.8f * sx, 27.5f * sy, 24.8f * sx, 38.0f * sy)
        cubicTo(24.8f * sx, 38.6f * sy, 24.8f * sx, 39.2f * sy, 24.9f * sx, 39.8f * sy)
        cubicTo(19.0f * sx, 40.9f * sy, 14.5f * sx, 46.1f * sx, 14.5f * sx, 52.3f * sy)
        cubicTo(14.5f * sx, 56.6f * sy, 18.0f * sx, 60f * sy, 22.3f * sx, 60f * sy)
        close()
    }

    // 1. Soft Elevation Drop Shadow
    withTransform({ translate(left = 0f, top = h * 0.032f) }) {
        drawPath(cloudPath, color = Color.Black.copy(alpha = 0.18f))
    }

    // 2. Base 3D Vertical Shading
    drawPath(
        path = cloudPath,
        brush = Brush.verticalGradient(
            0.0f to topColor,
            0.68f to bottomColor,
            1.0f to ambientOcclusionColor,
            startY = h * 0.22f,
            endY = h * 0.76f
        )
    )

    // 3. Internal Subsurface Scattering (Light bleeding through cloud body)
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to subsurfaceGlowColor,
            0.65f to subsurfaceGlowColor.copy(alpha = subsurfaceGlowColor.alpha * 0.3f),
            1.0f to Color.Transparent,
            center = subsurfaceCenter,
            radius = w * 0.28f
        ),
        radius = w * 0.28f,
        center = subsurfaceCenter,
        blendMode = BlendMode.Screen
    )

    // 4. Sculpted 3D Spherical Puff Highlights (Gives true Apple 3D volume to each cloud lobe)
    val mainLobeCenter = Offset(42f * sx, 35f * sy)
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color.White.copy(alpha = 0.65f),
            0.65f to Color.White.copy(alpha = 0.15f),
            1.0f to Color.Transparent,
            center = mainLobeCenter,
            radius = 17f * sx
        ),
        radius = 17f * sx,
        center = mainLobeCenter
    )

    val rightLobeCenter = Offset(60f * sx, 45f * sy)
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to Color.White.copy(alpha = 0.45f),
            1.0f to Color.Transparent,
            center = rightLobeCenter,
            radius = 12f * sx
        ),
        radius = 12f * sx,
        center = rightLobeCenter
    )

    // 5. Hairline 0.8dp Specular Top Rim
    drawPath(
        path = cloudPath,
        brush = Brush.linearGradient(
            0.0f to Color.White.copy(alpha = 0.90f),
            0.45f to Color.White.copy(alpha = 0.25f),
            1.0f to Color.White.copy(alpha = 0.10f),
            start = Offset(w * 0.2f, h * 0.2f),
            end = Offset(w * 0.8f, h * 0.75f)
        ),
        style = Stroke(width = w * 0.015f, join = StrokeJoin.Round)
    )
}

/**
 * Crisp SF-Symbols Style Mini-Icons for Hourly & Daily Rows
 */
@Composable
fun HourlyVectorIcon(
    iconType: HourlyIconType,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val c = Offset(w * 0.5f, h * 0.5f)

        when (iconType) {
            HourlyIconType.SUNNY -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFBEB), Color(0xFFFFD60A), Color(0xFFFF9F1C)),
                        center = Offset(c.x - w * 0.06f, c.y - h * 0.06f),
                        radius = w * 0.36f
                    ),
                    radius = w * 0.30f,
                    center = c
                )
            }
            HourlyIconType.PARTLY_CLOUDY -> {
                drawCircle(Color(0xFFFFD60A), radius = w * 0.22f, center = Offset(w * 0.36f, h * 0.36f))
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.94f),
                    topLeft = Offset(w * 0.22f, h * 0.44f),
                    size = Size(w * 0.62f, h * 0.32f),
                    cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
                )
            }
            HourlyIconType.SUNSET -> {
                drawLine(Color.White.copy(alpha = 0.65f), Offset(w * 0.12f, h * 0.70f), Offset(w * 0.88f, h * 0.70f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFFD60A), Color(0xFFFF9F1C))),
                    radius = w * 0.24f,
                    center = Offset(w * 0.5f, h * 0.52f)
                )
            }
            HourlyIconType.CLEAR_NIGHT -> {
                drawCircle(Color(0xFFE2E8F0), radius = w * 0.27f, center = c)
                drawCircle(Color(0xFF1E293B), radius = w * 0.21f, center = Offset(w * 0.61f, h * 0.39f))
            }
            HourlyIconType.WIND_GUST, HourlyIconType.LEAF_BREEZE -> {
                val col = Color(0xFF64D2FF)
                drawLine(col, Offset(w * 0.16f, h * 0.36f), Offset(w * 0.78f, h * 0.36f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
                drawLine(Color.White.copy(alpha = 0.9f), Offset(w * 0.10f, h * 0.54f), Offset(w * 0.86f, h * 0.54f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
                drawLine(col, Offset(w * 0.26f, h * 0.72f), Offset(w * 0.68f, h * 0.72f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
            }
            HourlyIconType.THUNDERSTORM -> {
                drawRoundRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(w * 0.16f, h * 0.22f),
                    size = Size(w * 0.68f, h * 0.34f),
                    cornerRadius = CornerRadius(w * 0.17f, w * 0.17f)
                )
                val bolt = Path().apply {
                    moveTo(w * 0.54f, h * 0.46f)
                    lineTo(w * 0.39f, h * 0.68f)
                    lineTo(w * 0.52f, h * 0.68f)
                    lineTo(w * 0.44f, h * 0.90f)
                    lineTo(w * 0.63f, h * 0.62f)
                    lineTo(w * 0.50f, h * 0.62f)
                    close()
                }
                drawPath(bolt, Color(0xFFFFD60A))
            }
            HourlyIconType.HEAVY_RAIN, HourlyIconType.SHOWER -> {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.90f),
                    topLeft = Offset(w * 0.16f, h * 0.22f),
                    size = Size(w * 0.68f, h * 0.34f),
                    cornerRadius = CornerRadius(w * 0.17f, w * 0.17f)
                )
                drawLine(Color(0xFF64D2FF), Offset(w * 0.34f, h * 0.64f), Offset(w * 0.26f, h * 0.84f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
                drawLine(Color(0xFF64D2FF), Offset(w * 0.54f, h * 0.64f), Offset(w * 0.46f, h * 0.84f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
                drawLine(Color(0xFF64D2FF), Offset(w * 0.74f, h * 0.64f), Offset(w * 0.66f, h * 0.84f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
            }
            HourlyIconType.HEAVY_SNOW, HourlyIconType.BLIZZARD, HourlyIconType.FROST_ICE -> {
                for (i in 0 until 3) {
                    val a = (i * 60.0) * PI / 180.0
                    val dx = (cos(a) * w * 0.32f).toFloat()
                    val dy = (sin(a) * h * 0.32f).toFloat()
                    drawLine(
                        color = Color(0xFFBAE6FD),
                        start = Offset(c.x - dx, c.y - dy),
                        end = Offset(c.x + dx, c.y + dy),
                        strokeWidth = w * 0.08f,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(Color.White, radius = w * 0.09f, center = c)
            }
        }
    }
}

/**
 * Vector Icon for Bento Micro-Widget Headers
 */
@Composable
fun MetricVectorIcon(
    iconType: MetricIconType,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    tint: Color = Color.White.copy(alpha = 0.60f)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val c = Offset(w * 0.5f, h * 0.5f)
        val stroke = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (iconType) {
            MetricIconType.UV_INDEX -> {
                drawCircle(tint, radius = w * 0.24f, center = c, style = stroke)
                for (i in 0 until 8) {
                    val a = (i * 45.0) * PI / 180.0
                    drawLine(
                        color = tint,
                        start = Offset(c.x + (cos(a) * w * 0.35f).toFloat(), c.y + (sin(a) * h * 0.35f).toFloat()),
                        end = Offset(c.x + (cos(a) * w * 0.46f).toFloat(), c.y + (sin(a) * h * 0.46f).toFloat()),
                        strokeWidth = w * 0.10f,
                        cap = StrokeCap.Round
                    )
                }
            }
            MetricIconType.AQI_LEAF, MetricIconType.RAINFALL_DROP, MetricIconType.HUMIDITY -> {
                val drop = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    quadraticTo(w * 0.86f, h * 0.55f, w * 0.5f, h * 0.88f)
                    quadraticTo(w * 0.14f, h * 0.55f, w * 0.5f, h * 0.12f)
                    close()
                }
                drawPath(drop, color = tint, style = stroke)
            }
            MetricIconType.SUNSET_HORIZON -> {
                drawLine(tint, Offset(w * 0.1f, h * 0.72f), Offset(w * 0.9f, h * 0.72f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.22f, h * 0.36f),
                    size = Size(w * 0.56f, h * 0.56f),
                    style = stroke
                )
            }
            MetricIconType.THERMOMETER -> {
                drawLine(tint, Offset(w * 0.5f, h * 0.15f), Offset(w * 0.5f, h * 0.68f), strokeWidth = w * 0.14f, cap = StrokeCap.Round)
                drawCircle(tint, radius = w * 0.20f, center = Offset(w * 0.5f, h * 0.74f))
            }
            MetricIconType.WIND_SPEED -> {
                drawLine(tint, Offset(w * 0.12f, h * 0.35f), Offset(w * 0.82f, h * 0.35f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.12f, h * 0.65f), Offset(w * 0.68f, h * 0.65f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
            }
            MetricIconType.COMPASS_GUST, MetricIconType.BAROMETER -> {
                drawCircle(tint, radius = w * 0.40f, center = c, style = stroke)
                drawLine(tint, c, Offset(w * 0.72f, h * 0.28f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
            }
            MetricIconType.VISIBILITY_EYE -> {
                drawOval(tint, topLeft = Offset(w * 0.12f, h * 0.26f), size = Size(w * 0.76f, h * 0.48f), style = stroke)
                drawCircle(tint, radius = w * 0.12f, center = c)
            }
            MetricIconType.LIGHTNING_BOLT -> {
                val bolt = Path().apply {
                    moveTo(w * 0.56f, h * 0.10f)
                    lineTo(w * 0.24f, h * 0.54f)
                    lineTo(w * 0.50f, h * 0.54f)
                    lineTo(w * 0.42f, h * 0.90f)
                    lineTo(w * 0.76f, h * 0.46f)
                    lineTo(w * 0.50f, h * 0.46f)
                    close()
                }
                drawPath(bolt, tint)
            }
            MetricIconType.SNOW_DEPTH, MetricIconType.WARNING_ICE -> {
                drawLine(tint, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.88f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.12f, h * 0.5f), Offset(w * 0.88f, h * 0.5f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
            }
        }
    }
}
