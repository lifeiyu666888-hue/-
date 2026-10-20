package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BentoMetricItem
import com.example.model.HourlyForecastItem
import com.example.model.MetricIconType
import com.example.model.WeatherCardData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * iOS 18 Native-Grade Liquid Glass Weather Card with Multi-Layer Parallax & Non-Linear Easing:
 * - Reacts to continuous pager offset (`pageOffsetSigned`) with internal 3D icon parallax & specular shift
 * - Forwards touch coordinates to the AGSL atmospheric shader & particle wake field
 * - Uses 0.6.dp hairline vibrancy borders and native Apple Weather visual instruments
 */
@Composable
fun LiquidGlassWeatherCard(
    cardData: WeatherCardData,
    isActive: Boolean,
    pageOffsetSigned: Float = 0f,
    useFahrenheit: Boolean,
    iconSpeedMultiplier: Float,
    refractionStrength: Float,
    onCardSelect: () -> Unit,
    onInspect3DIcon: () -> Unit,
    onOpenDetailSheet: () -> Unit,
    onTouchMoveNorm: (Offset) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var touchOffset by remember { mutableStateOf(Offset(260f, 180f)) }
    var isPressed by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.986f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ios_card_press"
    )

    // Smooth non-linear specular & parallax response during card swipe
    val specularShiftX = -pageOffsetSigned * 140f

    val cardShape = RoundedCornerShape(34.dp)
    val baseSkyTint = cardData.skyColors.first()

    // Razor-thin iOS 18 135° Specular Hairline Border
    val hairlineRimBrush = Brush.linearGradient(
        0.0f to Color.White.copy(alpha = if (isActive) 0.46f else 0.24f),
        0.38f to Color.White.copy(alpha = 0.12f),
        0.75f to Color.White.copy(alpha = 0.05f),
        1.0f to cardData.ambientOrbColor.copy(alpha = if (isActive) 0.30f else 0.14f),
        start = Offset(specularShiftX, 0f),
        end = Offset(800f + specularShiftX, 1200f)
    )

    Box(
        modifier = modifier
            .testTag("weather_card_${cardData.condition.id}")
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .shadow(
                elevation = if (isActive) 22.dp else 10.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.42f),
                spotColor = Color.Black.copy(alpha = 0.48f)
            )
            .clip(cardShape)
            .background(
                brush = Brush.verticalGradient(
                    0.0f to baseSkyTint.copy(alpha = 0.36f),
                    0.48f to baseSkyTint.copy(alpha = 0.50f),
                    1.0f to Color(0xFF070C17).copy(alpha = 0.64f)
                )
            )
            .drawBehind {
                // 1. Subtle Frosted White Vibrancy Overlay
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to Color.White.copy(alpha = if (isActive) 0.15f else 0.09f),
                        0.5f to Color.White.copy(alpha = 0.04f),
                        1.0f to Color.White.copy(alpha = 0.08f)
                    )
                )

                // 2. Soft Subsurface Atmospheric Glow in Upper-Right (shifts with parallax)
                val orbCenter = Offset(
                    size.width * 0.84f - pageOffsetSigned * size.width * 0.15f,
                    size.height * 0.13f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to cardData.ambientOrbColor.copy(alpha = 0.28f * refractionStrength),
                        0.6f to cardData.ambientOrbColor.copy(alpha = 0.08f * refractionStrength),
                        1.0f to Color.Transparent,
                        center = orbCenter,
                        radius = size.width * 0.62f
                    ),
                    radius = size.width * 0.62f,
                    center = orbCenter,
                    blendMode = BlendMode.Screen
                )

                // 3. Interactive Specular Caustic Sheen Following Touch
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to Color.White.copy(alpha = 0.12f * refractionStrength),
                        0.55f to cardData.ambientOrbColor.copy(alpha = 0.04f * refractionStrength),
                        1.0f to Color.Transparent,
                        center = touchOffset,
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = touchOffset,
                    blendMode = BlendMode.Screen
                )

                // 4. Top 1dp Optical Specular Edge Highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.40f),
                        1.0f to Color.Transparent,
                        startY = 0f,
                        endY = 5.dp.toPx()
                    ),
                    cornerRadius = CornerRadius(34.dp.toPx(), 34.dp.toPx())
                )
            }
            .border(
                width = 0.7.dp,
                brush = hairlineRimBrush,
                shape = cardShape
            )
            .pointerInput(cardData.condition) {
                detectTapGestures(
                    onPress = { offset ->
                        touchOffset = offset
                        if (size.width > 0 && size.height > 0) {
                            onTouchMoveNorm(
                                Offset(
                                    (offset.x / size.width).coerceIn(0f, 1f),
                                    (offset.y / size.height).coerceIn(0f, 1f)
                                )
                            )
                        }
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { offset ->
                        touchOffset = offset
                        onCardSelect()
                    }
                )
            }
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==================== 1. iOS Weather Hero Header ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            translationX = -pageOffsetSigned * 16.dp.toPx()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = cardData.city,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Icon(
                            imageVector = Icons.Filled.NearMe,
                            contentDescription = cardData.city,
                            tint = Color.White.copy(alpha = 0.82f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = cardData.district,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.60f),
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    val displayTemp = formatTemp(cardData.currentTempC, useFahrenheit)
                    val displayLow = formatTemp(cardData.lowTempC, useFahrenheit)
                    val displayHigh = formatTemp(cardData.highTempC, useFahrenheit)

                    Text(
                        text = displayTemp,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color.White,
                        letterSpacing = (-4).sp,
                        lineHeight = 80.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = cardData.summary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "最高 $displayHigh   最低 $displayLow",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                // Right Column: 3D Volumetric Apple Weather Icon with Foreground Parallax Depth
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.graphicsLayer {
                        // Foreground depth parallax when swiping cards
                        translationX = -pageOffsetSigned * 34.dp.toPx()
                        rotationZ = -pageOffsetSigned * 6f
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("icon_3d_${cardData.condition.id}")
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onInspect3DIcon() }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Animated3DLiquidGlassWeatherIcon(
                            condition = cardData.condition,
                            iconSize = 92.dp,
                            speedMultiplier = iconSpeedMultiplier,
                            refractionStrength = refractionStrength
                        )
                    }

                    // Clean iOS Weather Warning / Condition Pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable { onOpenDetailSheet() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${cardData.badgeText} · 5日预报 ›",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.90f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== 2. Apple Temperature Range Bar Module ====================
            IosTemperatureRangeModule(
                lowLabel = formatTemp(cardData.lowTempC, useFahrenheit),
                highLabel = formatTemp(cardData.highTempC, useFahrenheit),
                knobFraction = cardData.rangeKnobPosition,
                gradientColors = cardData.rangeGradientColors,
                onClick = onOpenDetailSheet
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ==================== 3. iOS 18 Hourly Forecast Glass Module ====================
            IosHourlyForecastModule(
                title = cardData.hourlyTitle,
                items = cardData.hourlyItems,
                accentColor = cardData.rangeGradientColors.last(),
                useFahrenheit = useFahrenheit
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ==================== 4. 2x2 Apple Weather Visual Instrument Bento Grid ====================
            IosBentoInstrumentsGrid(
                metrics = cardData.bentoMetrics,
                onMetricClick = onOpenDetailSheet
            )
        }
    }
}

@Composable
private fun IosTemperatureRangeModule(
    lowLabel: String,
    highLabel: String,
    knobFraction: Float,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detail_range_bar")
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "今日温差",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.56f)
        )

        Text(
            text = lowLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.72f)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val trackW = maxWidth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.30f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(gradientColors))
                )
            }

            val clamped = knobFraction.coerceIn(0.06f, 0.94f)
            Box(
                modifier = Modifier
                    .offset(x = (trackW - 10.dp) * clamped)
                    .size(10.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.8.dp, Color(0xFF0F172A), CircleShape)
            )
        }

        Text(
            text = highLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun IosHourlyForecastModule(
    title: String,
    items: List<HourlyForecastItem>,
    accentColor: Color,
    useFahrenheit: Boolean
) {
    val moduleShape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(moduleShape)
            .background(Color.Black.copy(alpha = 0.20f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), moduleShape)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(bottom = 7.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.55f)
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.White.copy(alpha = 0.13f),
            modifier = Modifier.padding(bottom = 9.dp)
        )

        val temps = remember(items) { items.map { it.tempC.toFloat() } }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .padding(horizontal = 18.dp)
        ) {
            if (temps.size >= 2) {
                val minT = (temps.minOrNull() ?: 0f) - 1f
                val maxT = (temps.maxOrNull() ?: 1f) + 1f
                val span = (maxT - minT).coerceAtLeast(1f)
                val stepX = size.width / (temps.size - 1)

                val points = temps.mapIndexed { idx, t ->
                    val x = idx * stepX
                    val y = size.height - ((t - minT) / span) * (size.height * 0.72f) - size.height * 0.14f
                    Offset(x, y)
                }

                val curvePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.lastIndex) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val midX = (p0.x + p1.x) * 0.5f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = curvePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF64D2FF), accentColor)
                    ),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )

                points.forEachIndexed { idx, pt ->
                    drawCircle(
                        color = if (idx == 0) Color.White else Color.White.copy(alpha = 0.75f),
                        radius = if (idx == 0) 3.dp.toPx() else 2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { idx, item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.timeLabel,
                        fontSize = 12.sp,
                        fontWeight = if (idx == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (idx == 0) Color.White else Color.White.copy(alpha = 0.75f)
                    )

                    HourlyVectorIcon(
                        iconType = item.iconType,
                        size = 24.dp
                    )

                    if (item.popLabel != null) {
                        Text(
                            text = item.popLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64D2FF)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = formatTemp(item.tempC, useFahrenheit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun IosBentoInstrumentsGrid(
    metrics: List<BentoMetricItem>,
    onMetricClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        metrics.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { metric ->
                    IosBentoInstrumentTile(
                        metric = metric,
                        onClick = onMetricClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun IosBentoInstrumentTile(
    metric: BentoMetricItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .clip(tileShape)
            .background(Color.Black.copy(alpha = 0.20f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), tileShape)
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MetricVectorIcon(
                iconType = metric.iconType,
                size = 12.dp,
                tint = Color.White.copy(alpha = 0.55f)
            )
            Text(
                text = metric.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = metric.mainValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )

            when (metric.iconType) {
                MetricIconType.COMPASS_GUST -> IosMiniCompassDial()
                MetricIconType.SUNSET_HORIZON -> IosMiniSolarArc()
                MetricIconType.BAROMETER -> IosMiniBarometerArc()
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (metric.progress != null && metric.progressColors.isNotEmpty()) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val barW = maxWidth
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (metric.progressColors.size > 1) {
                                Brush.horizontalGradient(metric.progressColors)
                            } else {
                                Brush.horizontalGradient(
                                    listOf(metric.progressColors.first(), metric.progressColors.first())
                                )
                            }
                        )
                )
                val p = metric.progress.coerceIn(0.06f, 0.94f)
                Box(
                    modifier = Modifier
                        .offset(x = (barW - 8.dp) * p)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF0F172A), CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = metric.subText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.68f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun IosMiniCompassDial() {
    val infiniteTransition = rememberInfiniteTransition(label = "compass_needle")
    val needleAngle by infiniteTransition.animateFloat(
        initialValue = -38f,
        targetValue = -26f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = Ios18FluidEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "needle_angle"
    )

    Canvas(modifier = Modifier.size(32.dp)) {
        val c = Offset(size.width * 0.5f, size.height * 0.5f)
        val r = size.width * 0.44f

        for (i in 0 until 16) {
            val a = (i * 22.5) * PI / 180.0
            val inner = if (i % 4 == 0) r * 0.74f else r * 0.82f
            drawLine(
                color = Color.White.copy(alpha = if (i % 4 == 0) 0.65f else 0.28f),
                start = Offset(c.x + (cos(a) * inner).toFloat(), c.y + (sin(a) * inner).toFloat()),
                end = Offset(c.x + (cos(a) * r).toFloat(), c.y + (sin(a) * r).toFloat()),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        rotate(degrees = needleAngle, pivot = c) {
            drawLine(
                color = Color.White,
                start = Offset(c.x, c.y + r * 0.55f),
                end = Offset(c.x, c.y - r * 0.65f),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFF64D2FF),
                radius = 2.2.dp.toPx(),
                center = Offset(c.x, c.y - r * 0.65f)
            )
        }
    }
}

@Composable
private fun IosMiniSolarArc() {
    Canvas(modifier = Modifier.width(44.dp).height(26.dp)) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.65f

        drawLine(
            color = Color.White.copy(alpha = 0.30f),
            start = Offset(0f, horizonY),
            end = Offset(w, horizonY),
            strokeWidth = 0.8.dp.toPx()
        )

        val path = Path().apply {
            val steps = 20
            for (s in 0..steps) {
                val frac = s.toFloat() / steps
                val x = frac * w
                val y = horizonY - sin(frac * PI.toFloat() * 1.3f - 0.3f) * (h * 0.42f)
                if (s == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFFFD60A), Color(0xFFFF9F1C), Color(0xFF64748B))
            ),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )

        val sunPt = Offset(w * 0.68f, horizonY - sin(0.68f * PI.toFloat() * 1.3f - 0.3f) * (h * 0.42f))
        drawCircle(
            color = Color(0xFFFFD60A).copy(alpha = 0.45f),
            radius = 5.dp.toPx(),
            center = sunPt
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = sunPt
        )
    }
}

@Composable
private fun IosMiniBarometerArc() {
    Canvas(modifier = Modifier.size(30.dp)) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = Color.White.copy(alpha = 0.22f),
            startAngle = 140f,
            sweepAngle = 260f,
            useCenter = false,
            style = stroke
        )
        drawArc(
            brush = Brush.sweepGradient(
                listOf(Color(0xFF64D2FF), Color.White, Color(0xFF64D2FF))
            ),
            startAngle = 140f,
            sweepAngle = 185f,
            useCenter = false,
            style = stroke
        )
    }
}

fun formatTemp(tempC: Int, useFahrenheit: Boolean): String {
    return if (useFahrenheit) {
        val f = (tempC * 9.0 / 5.0 + 32.0).roundToInt()
        "${f}°"
    } else {
        "${tempC}°"
    }
}
