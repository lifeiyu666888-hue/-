package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.WeatherCardData
import com.example.ui.components.Animated3DLiquidGlassWeatherIcon
import com.example.ui.components.FullScreenWeatherCanvas
import com.example.ui.components.Ios18FluidEasing
import com.example.ui.components.LiquidGlassIconLabSheet
import com.example.ui.components.LiquidGlassWeatherCard
import com.example.ui.components.WeatherDetailBottomSheet
import com.example.viewmodel.WeatherViewModel
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@Composable
fun WeatherDashboardScreen(
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = uiState.selectedIndex,
        pageCount = { uiState.cards.size }
    )

    // Continuous floating-point pager position (0.0 -> 3.0) driving AGSL shader cross-fade & parallax
    val continuousPagerOffset by remember {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 3f)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != uiState.selectedIndex) {
                viewModel.selectCardIndex(page)
            }
        }
    }

    LaunchedEffect(uiState.selectedIndex) {
        if (pagerState.currentPage != uiState.selectedIndex) {
            pagerState.animateScrollToPage(
                page = uiState.selectedIndex,
                animationSpec = tween(durationMillis = 480, easing = Ios18FluidEasing)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 0 & 1: GPU-Accelerated AGSL Sky Shader + 3-Depth Delta-Time Particle Engine
        FullScreenWeatherCanvas(
            condition = uiState.activeCard.condition,
            particleIntensity = uiState.refractionStrength,
            pagerOffsetContinuous = continuousPagerOffset,
            interactiveTouchNorm = uiState.interactiveTouchNorm,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Native iOS 18 Foreground Layout (Free of Web-style Fullscreen Clutter)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Clean Native iOS 18 Top Segmented City/Weather Pill Bar + °C/°F Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SlidingLiquidGlassSegmentedBar(
                        cards = uiState.cards,
                        continuousPosition = if (isWideScreen) uiState.selectedIndex.toFloat() else continuousPagerOffset,
                        selectedIndex = uiState.selectedIndex,
                        onSelectIndex = { idx ->
                            viewModel.selectCardIndex(idx)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = idx,
                                    animationSpec = tween(durationMillis = 480, easing = Ios18FluidEasing)
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Minimalist iOS Temperature Unit Pill (°C / °F)
                    Box(
                        modifier = Modifier
                            .testTag("unit_toggle_btn")
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.22f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                            .clickable { viewModel.toggleTemperatureUnit() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.useFahrenheit) "°F" else "°C",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Main Weather Cards Stage with Non-Linear 3D Perspective & Parallax
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    if (isWideScreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            uiState.cards.forEachIndexed { idx, cardData ->
                                val isActive = idx == uiState.selectedIndex
                                val cardWeight by animateFloatAsState(
                                    targetValue = if (isActive) 1.72f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "accordion_weight_$idx"
                                )
                                LiquidGlassWeatherCard(
                                    cardData = cardData,
                                    isActive = isActive,
                                    pageOffsetSigned = 0f,
                                    useFahrenheit = uiState.useFahrenheit,
                                    iconSpeedMultiplier = uiState.iconSpeedMultiplier,
                                    refractionStrength = uiState.refractionStrength,
                                    onCardSelect = { viewModel.selectCardIndex(idx) },
                                    onInspect3DIcon = {
                                        viewModel.selectCardIndex(idx)
                                        viewModel.openIconLab()
                                    },
                                    onOpenDetailSheet = {
                                        viewModel.selectCardIndex(idx)
                                        viewModel.openDetailSheet()
                                    },
                                    onTouchMoveNorm = viewModel::updateInteractiveTouch,
                                    modifier = Modifier
                                        .weight(cardWeight)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 22.dp),
                            pageSpacing = 14.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("weather_cards_pager")
                        ) { page ->
                            val cardData = uiState.cards[page]
                            val signedOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val absOffset = signedOffset.absoluteValue.coerceIn(0f, 1f)

                            // Non-linear iOS 18 Fluid Easing on card scale, opacity, and 3D rotation
                            val easedProgress = Ios18FluidEasing.transform(absOffset)
                            val scale = 1f - (easedProgress * 0.065f)
                            val alpha = 1f - (easedProgress * 0.24f)
                            val rotY = -signedOffset.coerceIn(-1f, 1f) * 7.5f

                            LiquidGlassWeatherCard(
                                cardData = cardData,
                                isActive = page == uiState.selectedIndex,
                                pageOffsetSigned = signedOffset.coerceIn(-1f, 1f),
                                useFahrenheit = uiState.useFahrenheit,
                                iconSpeedMultiplier = uiState.iconSpeedMultiplier,
                                refractionStrength = uiState.refractionStrength,
                                onCardSelect = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = page,
                                            animationSpec = tween(durationMillis = 480, easing = Ios18FluidEasing)
                                        )
                                    }
                                },
                                onInspect3DIcon = {
                                    viewModel.selectCardIndex(page)
                                    viewModel.openIconLab()
                                },
                                onOpenDetailSheet = {
                                    viewModel.selectCardIndex(page)
                                    viewModel.openDetailSheet()
                                },
                                onTouchMoveNorm = viewModel::updateInteractiveTouch,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                        rotationY = rotY
                                        cameraDistance = 16f * density
                                    }
                            )
                        }
                    }
                }

                // Bottom iOS 18 Page Dots Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.20f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.cards.forEachIndexed { index, card ->
                            val isSelected = index == uiState.selectedIndex
                            val dotWidth by animateDpAsState(
                                targetValue = if (isSelected) 18.dp else 6.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "dot_width_$index"
                            )
                            Box(
                                modifier = Modifier
                                    .testTag("page_dot_${card.condition.id}")
                                    .width(dotWidth)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White
                                        else Color.White.copy(alpha = 0.35f)
                                    )
                                    .clickable {
                                        viewModel.selectCardIndex(index)
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(
                                                page = index,
                                                animationSpec = tween(durationMillis = 480, easing = Ios18FluidEasing)
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Interactive 3D Liquid Glass Icon Lab Modal Sheet (triggered by tapping any 3D weather icon)
        if (uiState.showIconLabSheet) {
            LiquidGlassIconLabSheet(
                initialCondition = uiState.activeCard.condition,
                speedMultiplier = uiState.iconSpeedMultiplier,
                refractionStrength = uiState.refractionStrength,
                onSpeedChange = viewModel::updateIconSpeed,
                onRefractionChange = viewModel::updateRefractionStrength,
                onSelectCondition = viewModel::selectCondition,
                onDismiss = viewModel::closeIconLab
            )
        }

        // 5-Day Forecast & Optical Weather Detail Sheet
        if (uiState.showDetailSheet) {
            WeatherDetailBottomSheet(
                cardData = uiState.activeCard,
                useFahrenheit = uiState.useFahrenheit,
                onDismiss = viewModel::closeDetailSheet
            )
        }
    }
}

/**
 * Native iOS 18 Sliding Liquid Glass Segmented Bar:
 * The active frosted glass pill smoothly glides across segments in sync with the pager's continuous offset.
 */
@Composable
private fun SlidingLiquidGlassSegmentedBar(
    cards: List<WeatherCardData>,
    continuousPosition: Float,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.22f))
            .border(0.5.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .padding(3.5.dp)
    ) {
        val count = cards.size.coerceAtLeast(1)
        val segmentWidth = maxWidth / count
        val pillOffset = segmentWidth * continuousPosition.coerceIn(0f, (count - 1).toFloat())

        // Gliding Liquid Glass Active Pill
        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(segmentWidth)
                .height(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
                .border(0.5.dp, Color.White.copy(alpha = 0.42f), CircleShape)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cards.forEachIndexed { idx, card ->
                val isSelected = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .testTag("tab_${card.condition.id}")
                        .clip(CircleShape)
                        .clickable { onSelectIndex(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Animated3DLiquidGlassWeatherIcon(
                            condition = card.condition,
                            iconSize = 18.dp
                        )
                        Text(
                            text = card.city,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}
