package com.example.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.model.WeatherCondition
import org.intellij.lang.annotations.Language
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * iOS 18 Non-linear Quintic/Expo Easing Curve: cubic-bezier(0.16, 1.0, 0.3, 1.0)
 */
val Ios18FluidEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun quinticSmooth(x: Float): Float {
    val t = x.coerceIn(0f, 1f)
    return t * t * t * (t * (t * 6f - 15f) + 10f)
}

/**
 * GPU-Accelerated AGSL Fragment Shader for iOS 18 Atmospheric Weather Rendering.
 * Computes per-pixel Rayleigh/Mie sky scattering, volumetric fBM clouds,
 * smooth crepuscular sun shafts, domain-warped wind streams, storm sheet-lightning,
 * and arctic frost glow directly on the GPU.
 */
@Language("AGSL")
private const val IOS18_WEATHER_AGSL_SHADER = """
    uniform float2 uResolution;
    uniform float uTime;
    uniform float uWeatherIndex; // Continuous 0.0=Sunny, 1.0=Windy, 2.0=Rainy, 3.0=Snowy
    uniform float2 uTouch;       // Normalized touch coordinates (0..1)
    uniform float uIntensity;

    float hash21(float2 p) {
        p = fract(p * float2(234.34, 435.345));
        p += dot(p, p + 34.23);
        return fract(p.x * p.y);
    }

    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        // Quintic Hermite interpolation for ultra-smooth C2 continuity
        float2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
        float a = hash21(i);
        float b = hash21(i + float2(1.0, 0.0));
        float c = hash21(i + float2(0.0, 1.0));
        float d = hash21(i + float2(1.0, 1.0));
        return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
    }

    float fbm(float2 p) {
        float v = 0.0;
        float a = 0.5;
        float2 shift = float2(100.0, 100.0);
        for (int i = 0; i < 4; ++i) {
            v += a * noise(p);
            p = p * 2.03 + shift;
            a *= 0.5;
        }
        return v;
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / max(uResolution.xy, float2(1.0, 1.0));
        float aspect = uResolution.x / max(uResolution.y, 1.0);
        float2 p = float2(uv.x * aspect, uv.y);

        // Smooth weights for each weather state from continuous uWeatherIndex
        float wSunny = clamp(1.0 - abs(uWeatherIndex - 0.0), 0.0, 1.0);
        float wWindy = clamp(1.0 - abs(uWeatherIndex - 1.0), 0.0, 1.0);
        float wRainy = clamp(1.0 - abs(uWeatherIndex - 2.0), 0.0, 1.0);
        float wSnowy = clamp(1.0 - abs(uWeatherIndex - 3.0), 0.0, 1.0);

        // Quintic easing on state weights
        wSunny = smoothstep(0.0, 1.0, wSunny);
        wWindy = smoothstep(0.0, 1.0, wWindy);
        wRainy = smoothstep(0.0, 1.0, wRainy);
        wSnowy = smoothstep(0.0, 1.0, wSnowy);

        // 1. Base Sky Gradients per condition (Zenith -> Mid -> Horizon)
        float3 sunnySky = mix(
            mix(float3(0.07, 0.34, 0.64), float3(0.19, 0.53, 0.78), smoothstep(0.0, 0.55, uv.y)),
            float3(0.48, 0.74, 0.93),
            smoothstep(0.45, 1.0, uv.y)
        );
        float3 windySky = mix(
            mix(float3(0.07, 0.17, 0.28), float3(0.11, 0.34, 0.44), smoothstep(0.0, 0.55, uv.y)),
            float3(0.22, 0.50, 0.58),
            smoothstep(0.45, 1.0, uv.y)
        );
        float3 rainySky = mix(
            mix(float3(0.04, 0.06, 0.13), float3(0.08, 0.12, 0.22), smoothstep(0.0, 0.55, uv.y)),
            float3(0.13, 0.19, 0.32),
            smoothstep(0.45, 1.0, uv.y)
        );
        float3 snowySky = mix(
            mix(float3(0.10, 0.16, 0.25), float3(0.19, 0.28, 0.40), smoothstep(0.0, 0.55, uv.y)),
            float3(0.32, 0.44, 0.58),
            smoothstep(0.45, 1.0, uv.y)
        );

        float3 skyColor = sunnySky * wSunny + windySky * wWindy + rainySky * wRainy + snowySky * wSnowy;

        // 2. Volumetric fBM Atmospheric Clouds
        float windSpeed = mix(0.03, 0.18, wWindy) + wRainy * 0.06 + wSnowy * 0.04;
        float2 cloudUv = float2(p.x * 1.6 - uTime * windSpeed, p.y * 2.4);
        float n1 = fbm(cloudUv);
        float n2 = fbm(cloudUv * 1.8 + float2(n1 * 0.65, uTime * 0.02));
        float cloudMask = smoothstep(0.38, 0.82, n2) * smoothstep(0.85, 0.05, uv.y);

        // 3. Sunny Optical Mie Scattering & Volumetric Crepuscular God-Rays
        if (wSunny > 0.001) {
            float2 sunPos = float2(0.82 * aspect + (uTouch.x - 0.5) * 0.04, 0.10 + (uTouch.y - 0.5) * 0.03);
            float2 toSun = p - sunPos;
            float dist = length(toSun);

            // Exponential Mie solar bloom (utterly smooth, zero polygonal edges)
            float coreBloom = exp(-dist * 5.2) * 0.85 + exp(-dist * 1.9) * 0.38;
            float3 sunWarmth = float3(1.0, 0.91, 0.70) * coreBloom * uIntensity;

            // Volumetric Crepuscular Rays modulated by smooth angular noise
            float angle = atan(toSun.y, toSun.x);
            float rayWave = noise(float2(angle * 4.2 + uTime * 0.08, dist * 0.6 - uTime * 0.05));
            float rayWave2 = noise(float2(angle * 7.5 - uTime * 0.06, dist * 0.4));
            float rayMask = smoothstep(0.35, 0.85, rayWave * 0.65 + rayWave2 * 0.35);
            // Attenuate rays gently with distance and direction (downward-left cone)
            float dirWeight = smoothstep(-0.2, 0.7, toSun.y) * exp(-dist * 1.15);
            float3 godRays = float3(1.0, 0.96, 0.84) * (rayMask * dirWeight * 0.24 * uIntensity);

            // Coaxial Anamorphic Lens Flare Ring along Sun -> Center axis
            float2 centerPos = float2(0.5 * aspect, 0.48);
            float2 flareAxis = centerPos - sunPos;
            float2 ghost1 = sunPos + flareAxis * 0.62;
            float2 ghost2 = sunPos + flareAxis * 1.18;
            float gDist1 = length(p - ghost1);
            float gDist2 = length(p - ghost2);
            float flare1 = exp(-abs(gDist1 - 0.065) * 42.0) * smoothstep(0.09, 0.0, gDist1) * 0.12;
            float flare2 = exp(-gDist2 * 14.0) * 0.09;
            float3 lensFlare = float3(0.65, 0.90, 1.0) * flare1 + float3(1.0, 0.88, 0.60) * flare2;

            skyColor += (sunWarmth + godRays + lensFlare) * wSunny;
            skyColor = mix(skyColor, float3(1.0, 0.98, 0.94), cloudMask * 0.22 * wSunny);
        }

        // 4. Windy Aerodynamic Silk Flow Field
        if (wWindy > 0.001) {
            float flow = fbm(float2(p.x * 2.5 - uTime * 0.45, p.y * 6.5 + sin(p.x * 3.0 + uTime * 0.5) * 0.35));
            float silk = smoothstep(0.52, 0.78, flow) * 0.18 * uIntensity;
            skyColor += float3(0.45, 0.92, 0.86) * silk * wWindy;
            skyColor = mix(skyColor, float3(0.78, 0.94, 0.96), cloudMask * 0.20 * wWindy);
        }

        // 5. Rainy Storm Clouds & Internal Sheet Lightning
        if (wRainy > 0.001) {
            // Non-linear double-pulse lightning envelope
            float flashCycle = sin(uTime * 1.9) * sin(uTime * 4.7);
            float sheetFlash = pow( clamp((flashCycle - 0.84) / 0.16, 0.0, 1.0), 2.0 );
            float2 stormCenter = float2(0.58 * aspect, 0.18);
            float stormGlow = exp(-length(p - stormCenter) * 2.4) * sheetFlash * 0.55 * uIntensity;

            skyColor = mix(skyColor, float3(0.05, 0.07, 0.12), cloudMask * 0.45 * wRainy);
            skyColor += float3(0.68, 0.78, 1.0) * stormGlow * wRainy;
        }

        // 6. Snowy Arctic Albedo & Edge Frost Vignette
        if (wSnowy > 0.001) {
            float edgeDist = length((uv - float2(0.5, 0.5)) * float2(1.1, 0.95));
            float frostVignette = smoothstep(0.32, 0.76, edgeDist) * 0.26 * uIntensity;
            skyColor = mix(skyColor, float3(0.85, 0.93, 1.0), (cloudMask * 0.25 + frostVignette) * wSnowy);
        }

        // 7. Interactive Touch Caustic Ripple
        float2 touchPos = float2(uTouch.x * aspect, uTouch.y);
        float touchDist = length(p - touchPos);
        float touchGlow = exp(-touchDist * 4.5) * 0.07 * uIntensity;
        skyColor += float3(0.85, 0.94, 1.0) * touchGlow;

        return half4(half3(clamp(skyColor, 0.0, 1.0)), 1.0);
    }
"""

/**
 * Stateful 3D Depth-of-Field Particle for Frame-Accurate Delta-Time Physics
 */
private class DepthParticle(
    var xNorm: Float,
    var yNorm: Float,
    val radiusNorm: Float,
    val speedNorm: Float,
    var phase: Float,
    val depth: Float // 0..0.35 Far, 0.35..0.78 Mid, 0.78..1.0 Near Bokeh
)

private class SplashDroplet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float // 1.0 -> 0.0
)

/**
 * GPU-Accelerated AGSL + Frame-Accurate Delta-Time 3D Particle Physics Weather Backdrop.
 * Combines per-pixel AGSL atmospheric scattering on Android 13+ with a 3-layer depth-of-field
 * interactive particle system driven by `withFrameNanos` and non-linear easing curves.
 */
@Composable
fun FullScreenWeatherCanvas(
    condition: WeatherCondition,
    particleIntensity: Float = 1f,
    pagerOffsetContinuous: Float = 0f,
    interactiveTouchNorm: Offset = Offset(0.5f, 0.35f),
    modifier: Modifier = Modifier
) {
    // Smoothly interpolate continuous weather index (0f=Sunny, 1f=Windy, 2f=Rainy, 3f=Snowy)
    val targetIndex = pagerOffsetContinuous.coerceIn(0f, 3f)
    val smoothWeatherIndex by animateFloatAsState(
        targetValue = targetIndex,
        animationSpec = tween(durationMillis = 420, easing = Ios18FluidEasing),
        label = "smooth_weather_index"
    )

    val smoothTouchX by animateFloatAsState(
        targetValue = interactiveTouchNorm.x.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 360, easing = Ios18FluidEasing),
        label = "smooth_touch_x"
    )
    val smoothTouchY by animateFloatAsState(
        targetValue = interactiveTouchNorm.y.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 360, easing = Ios18FluidEasing),
        label = "smooth_touch_y"
    )

    // Initialize 3-Depth Particle Pool
    val particles = remember {
        val rng = Random(2026)
        List(115) {
            DepthParticle(
                xNorm = rng.nextFloat(),
                yNorm = rng.nextFloat(),
                radiusNorm = rng.nextFloat(),
                speedNorm = 0.55f + rng.nextFloat() * 0.90f,
                phase = rng.nextFloat() * (2f * PI.toFloat()),
                depth = rng.nextFloat()
            )
        }
    }
    val splashes = remember { ArrayList<SplashDroplet>(36) }

    // Frame-accurate delta-time clock via withFrameNanos
    var elapsedSec by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(condition, particleIntensity) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = frameNanos
                elapsedSec += dt

                // Update particle kinematics with non-linear wake repulsion around touch point
                particles.forEach { p ->
                    p.phase += dt * (1.2f + p.speedNorm * 0.8f)

                    // Touch wake repulsion vector in normalized space
                    val dxTouch = p.xNorm - smoothTouchX
                    val dyTouch = p.yNorm - smoothTouchY
                    val distSq = dxTouch * dxTouch + dyTouch * dyTouch
                    val wakePush = if (distSq < 0.04f && distSq > 0.0001f) {
                        val f = (1f - distSq / 0.04f) * 0.18f * dt
                        dxTouch / kotlin.math.sqrt(distSq) * f
                    } else 0f

                    when (condition) {
                        WeatherCondition.SUNNY -> {
                            p.yNorm -= dt * 0.022f * p.speedNorm * (0.4f + p.depth * 0.6f)
                            p.xNorm += sin(p.phase * 0.7f) * 0.0003f + wakePush * 0.3f
                            if (p.yNorm < -0.05f) p.yNorm = 1.05f
                            if (p.xNorm < -0.05f) p.xNorm = 1.05f
                            if (p.xNorm > 1.05f) p.xNorm = -0.05f
                        }
                        WeatherCondition.WINDY -> {
                            p.xNorm += dt * (0.24f + p.depth * 0.32f) * p.speedNorm + abs(wakePush)
                            p.yNorm += sin(p.phase * 1.6f) * dt * 0.035f
                            if (p.xNorm > 1.15f) {
                                p.xNorm = -0.15f
                                p.yNorm = (p.yNorm + 0.37f) % 1f
                            }
                        }
                        WeatherCondition.RAINY -> {
                            val fallVel = (0.72f + p.depth * 0.85f) * p.speedNorm
                            p.yNorm += dt * fallVel
                            p.xNorm -= dt * fallVel * 0.11f - wakePush * 0.4f
                            if (p.yNorm >= 0.96f) {
                                if (p.depth > 0.65f && splashes.size < 28 && p.radiusNorm > 0.45f) {
                                    splashes.add(
                                        SplashDroplet(
                                            x = (p.xNorm % 1f + 1f) % 1f,
                                            y = 0.95f,
                                            vx = (p.radiusNorm - 0.5f) * 0.18f,
                                            vy = -(0.14f + p.speedNorm * 0.10f),
                                            life = 1.0f
                                        )
                                    )
                                }
                                p.yNorm = -0.06f
                                p.xNorm = (p.xNorm + 0.29f) % 1.1f
                            }
                            if (p.xNorm < -0.1f) p.xNorm += 1.15f
                        }
                        WeatherCondition.SNOWY -> {
                            val snowVel = (0.065f + p.depth * 0.13f) * p.speedNorm
                            p.yNorm += dt * snowVel
                            p.xNorm += sin(p.phase) * dt * (0.025f + p.depth * 0.03f) + wakePush * 0.5f
                            if (p.yNorm > 1.05f) {
                                p.yNorm = -0.05f
                            }
                            if (p.xNorm > 1.05f) p.xNorm = -0.05f
                            if (p.xNorm < -0.05f) p.xNorm = 1.05f
                        }
                    }
                }

                // Update rain splash droplets with gravity
                val iter = splashes.iterator()
                while (iter.hasNext()) {
                    val s = iter.next()
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.vy += 0.55f * dt // gravity
                    s.life -= dt * 2.6f
                    if (s.life <= 0f) iter.remove()
                }
            }
        }
    }

    // Prepare AGSL RuntimeShader on Android 13+ (API 33+)
    val agslHolder = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AgslShaderHolder.create()
        } else {
            null
        }
    }

    val activeParticleCount = (particles.size * particleIntensity.coerceIn(0.4f, 1.5f))
        .toInt()
        .coerceIn(20, particles.size)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Layer 1: GPU-Accelerated AGSL Atmospheric Sky & Volumetric Shader (or smooth fallback on API < 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && agslHolder != null) {
            agslHolder.drawAgslSky(
                scope = this,
                width = w,
                height = h,
                time = elapsedSec,
                weatherIndex = smoothWeatherIndex,
                touchX = smoothTouchX,
                touchY = smoothTouchY,
                intensity = particleIntensity
            )
        } else {
            drawFallbackAtmosphericSky(
                w = w,
                h = h,
                t = elapsedSec,
                condition = condition,
                intensity = particleIntensity
            )
        }

        // Layer 2: 3-Layer Depth-of-Field Particle & Light Physics Feedback
        val parallaxBase = -(pagerOffsetContinuous - pagerOffsetContinuous.toInt()) * w * 0.08f

        when (condition) {
            WeatherCondition.SUNNY -> drawSunnyDepthParticles(
                w = w,
                h = h,
                t = elapsedSec,
                parallaxX = parallaxBase,
                touch = Offset(smoothTouchX * w, smoothTouchY * h),
                particles = particles.take(activeParticleCount / 3)
            )
            WeatherCondition.WINDY -> drawWindyDepthStreams(
                w = w,
                h = h,
                t = elapsedSec,
                parallaxX = parallaxBase,
                particles = particles.take(activeParticleCount / 2)
            )
            WeatherCondition.RAINY -> drawRainyDepthParticles(
                w = w,
                h = h,
                parallaxX = parallaxBase,
                particles = particles.take(activeParticleCount),
                splashes = splashes
            )
            WeatherCondition.SNOWY -> drawSnowyDepthParticles(
                w = w,
                h = h,
                parallaxX = parallaxBase,
                particles = particles.take(activeParticleCount)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class AgslShaderHolder(
    private val shader: RuntimeShader,
    private val brush: ShaderBrush
) {
    fun drawAgslSky(
        scope: DrawScope,
        width: Float,
        height: Float,
        time: Float,
        weatherIndex: Float,
        touchX: Float,
        touchY: Float,
        intensity: Float
    ) {
        shader.setFloatUniform("uResolution", max(width, 1f), max(height, 1f))
        shader.setFloatUniform("uTime", time)
        shader.setFloatUniform("uWeatherIndex", weatherIndex)
        shader.setFloatUniform("uTouch", touchX, touchY)
        shader.setFloatUniform("uIntensity", intensity)
        scope.drawRect(brush = brush, size = Size(width, height))
    }

    companion object {
        fun create(): AgslShaderHolder? {
            return try {
                val rt = RuntimeShader(IOS18_WEATHER_AGSL_SHADER)
                AgslShaderHolder(rt, ShaderBrush(rt))
            } catch (_: Throwable) {
                null
            }
        }
    }
}

/**
 * High-precision non-linear fallback sky renderer for devices running Android < 13
 */
private fun DrawScope.drawFallbackAtmosphericSky(
    w: Float,
    h: Float,
    t: Float,
    condition: WeatherCondition,
    intensity: Float
) {
    val colors = when (condition) {
        WeatherCondition.SUNNY -> listOf(Color(0xFF125699), Color(0xFF2E86C1), Color(0xFF7AC1F0))
        WeatherCondition.WINDY -> listOf(Color(0xFF122B45), Color(0xFF1D566E), Color(0xFF397E94))
        WeatherCondition.RAINY -> listOf(Color(0xFF0A0F1E), Color(0xFF141E36), Color(0xFF22304C))
        WeatherCondition.SNOWY -> listOf(Color(0xFF1A283D), Color(0xFF2F4562), Color(0xFF516E8E))
    }
    drawRect(brush = Brush.verticalGradient(colors), size = Size(w, h))

    if (condition == WeatherCondition.SUNNY) {
        val sunPos = Offset(w * 0.82f, h * 0.10f)
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color.White.copy(alpha = 0.88f),
                0.18f to Color(0xFFFFF3C4).copy(alpha = 0.55f * intensity),
                0.48f to Color(0xFFFBBF24).copy(alpha = 0.18f * intensity),
                1.0f to Color.Transparent,
                center = sunPos,
                radius = max(w, h) * 0.65f
            ),
            radius = max(w, h) * 0.65f,
            center = sunPos,
            blendMode = BlendMode.Screen
        )
    }
}

/**
 * 1. Sunny 3-Depth Light Motes & Interactive Lens Shimmer
 */
private fun DrawScope.drawSunnyDepthParticles(
    w: Float,
    h: Float,
    t: Float,
    parallaxX: Float,
    touch: Offset,
    particles: List<DepthParticle>
) {
    particles.forEach { p ->
        val depthParallax = parallaxX * (0.3f + p.depth * 1.1f)
        val px = ((p.xNorm * w + depthParallax) % w + w) % w
        val py = p.yNorm * h

        // Non-linear quintic breathing envelope
        val wave = (sin(p.phase) + 1f) * 0.5f
        val smoothPulse = quinticSmooth(wave)

        // Proximity boost near user touch
        val distToTouch = hypot(px - touch.x, py - touch.y)
        val touchBoost = smoothstep(w * 0.35f, 0f, distToTouch) * 0.12f

        val radius = when {
            p.depth > 0.80f -> 14f + p.radiusNorm * 18f // Near out-of-focus bokeh orb
            p.depth > 0.35f -> 4f + p.radiusNorm * 6f   // Mid-plane warm mote
            else -> 2f + p.radiusNorm * 2.5f            // Far plane speck
        }
        val alpha = ((0.04f + p.depth * 0.10f) * smoothPulse + touchBoost).coerceIn(0f, 0.32f)

        drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color(0xFFFFFBEB).copy(alpha = alpha),
                0.55f to Color(0xFFFDE68A).copy(alpha = alpha * 0.45f),
                1.0f to Color.Transparent,
                center = Offset(px, py),
                radius = radius
            ),
            radius = radius,
            center = Offset(px, py),
            blendMode = BlendMode.Screen
        )
    }
}

/**
 * 2. Windy 3-Depth Aerodynamic Tapered Filaments & Tumbling Leaves
 */
private fun DrawScope.drawWindyDepthStreams(
    w: Float,
    h: Float,
    t: Float,
    parallaxX: Float,
    particles: List<DepthParticle>
) {
    // 10 Tapered aerodynamic wind filaments with non-linear alpha envelope
    particles.take(10).forEachIndexed { idx, p ->
        val streamLen = w * (0.34f + p.radiusNorm * 0.28f)
        val headX = ((p.xNorm * w + parallaxX * (0.5f + p.depth)) % (w * 1.3f))
        val baseY = p.yNorm * h

        val path = Path().apply {
            val steps = 14
            for (s in 0..steps) {
                val frac = s.toFloat() / steps
                val x = headX - streamLen * (1f - frac)
                val y = baseY + sin(frac * PI.toFloat() * 2f + t * 2.8f + idx) * (8f + p.radiusNorm * 14f)
                if (s == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val fade = sin(p.xNorm.coerceIn(0f, 1f) * PI.toFloat())
        val alpha = (0.07f + p.depth * 0.15f) * quinticSmooth(fade)

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                0.0f to Color.Transparent,
                0.4f to Color.White.copy(alpha = alpha * 0.55f),
                0.75f to Color(0xFF99F6E4).copy(alpha = alpha),
                1.0f to Color.Transparent,
                startX = headX - streamLen,
                endX = headX
            ),
            style = Stroke(width = 1.0f + p.depth * 1.6f, cap = StrokeCap.Round),
            blendMode = BlendMode.Screen
        )
    }

    // 3D Tumbling leaves with depth scaling
    particles.drop(10).take(14).forEach { p ->
        val x = ((p.xNorm * w + parallaxX * p.depth) % w + w) % w
        val y = p.yNorm * h
        val leafSize = (5f + p.depth * 7f)
        val rotDeg = (p.phase * 57.3f) % 360f

        withTransform({
            rotate(degrees = rotDeg, pivot = Offset(x, y))
        }) {
            val leaf = Path().apply {
                moveTo(x - leafSize, y)
                quadraticTo(x, y - leafSize * 0.55f, x + leafSize, y)
                quadraticTo(x, y + leafSize * 0.55f, x - leafSize, y)
                close()
            }
            drawPath(
                path = leaf,
                color = Color(0xFF6EE7B7).copy(alpha = 0.25f + p.depth * 0.45f)
            )
        }
    }
}

/**
 * 3. Rainy 3-Depth Motion-Blurred Streaks & Gravity Splash Droplets
 */
private fun DrawScope.drawRainyDepthParticles(
    w: Float,
    h: Float,
    parallaxX: Float,
    particles: List<DepthParticle>,
    splashes: List<SplashDroplet>
) {
    particles.forEach { p ->
        val isForeground = p.depth > 0.72f
        val isMidground = p.depth > 0.32f
        val x = ((p.xNorm * w + parallaxX * (0.3f + p.depth)) % w + w) % w
        val y = p.yNorm * h

        val len = when {
            isForeground -> 58f + p.radiusNorm * 30f
            isMidground -> 34f + p.radiusNorm * 18f
            else -> 18f + p.radiusNorm * 10f
        }
        val dx = -len * 0.11f
        val alpha = when {
            isForeground -> 0.34f + p.radiusNorm * 0.18f
            isMidground -> 0.18f + p.radiusNorm * 0.12f
            else -> 0.08f + p.radiusNorm * 0.06f
        }

        drawLine(
            brush = Brush.linearGradient(
                0.0f to Color.Transparent,
                0.45f to Color(0xFF93C5FD).copy(alpha = alpha * 0.5f),
                1.0f to Color.White.copy(alpha = alpha),
                start = Offset(x, y),
                end = Offset(x + dx, y + len)
            ),
            start = Offset(x, y),
            end = Offset(x + dx, y + len),
            strokeWidth = if (isForeground) 1.7f else if (isMidground) 1.1f else 0.7f,
            cap = StrokeCap.Round
        )
    }

    // Render parabolic water splash micro-droplets & expanding surface rings
    splashes.forEach { s ->
        val sx = s.x * w
        val sy = s.y * h
        val alpha = quinticSmooth(s.life) * 0.55f
        drawCircle(
            color = Color(0xFFBAE6FD).copy(alpha = alpha),
            radius = 1.8f,
            center = Offset(sx, sy)
        )
        val ringR = (1f - s.life) * 14f
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.5f),
            radius = ringR,
            center = Offset(sx, h * 0.955f),
            style = Stroke(width = 0.9f)
        )
    }
}

/**
 * 4. Snowy 3-Depth Bokeh & Crystalline Flakes
 */
private fun DrawScope.drawSnowyDepthParticles(
    w: Float,
    h: Float,
    parallaxX: Float,
    particles: List<DepthParticle>
) {
    particles.forEach { p ->
        val x = ((p.xNorm * w + parallaxX * (0.25f + p.depth * 1.1f)) % w + w) % w
        val y = p.yNorm * h

        val radius = when {
            p.depth > 0.84f -> 7.5f + p.radiusNorm * 5.0f // Near lens soft bokeh flake
            p.depth > 0.38f -> 2.8f + p.radiusNorm * 2.4f // Mid-plane crisp snow
            else -> 1.3f + p.radiusNorm * 1.1f            // Distant snow speck
        }
        val peakAlpha = when {
            p.depth > 0.84f -> 0.34f
            p.depth > 0.38f -> 0.74f
            else -> 0.32f
        }

        drawCircle(
            brush = Brush.radialGradient(
                0.0f to Color.White.copy(alpha = peakAlpha),
                0.48f to Color(0xFFE0F2FE).copy(alpha = peakAlpha * 0.6f),
                1.0f to Color.Transparent,
                center = Offset(x, y),
                radius = radius
            ),
            radius = radius,
            center = Offset(x, y)
        )
    }
}
