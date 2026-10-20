package com.example.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.example.model.WeatherCardData
import com.example.model.WeatherCondition
import com.example.model.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WeatherUiState(
    val cards: List<WeatherCardData> = WeatherRepository.allWeatherCards,
    val selectedIndex: Int = 0,
    val useFahrenheit: Boolean = false,
    val showIconLabSheet: Boolean = false,
    val showDetailSheet: Boolean = false,
    val iconSpeedMultiplier: Float = 1.0f,
    val refractionStrength: Float = 1.0f,
    val interactiveTouchNorm: Offset = Offset(0.75f, 0.22f)
) {
    val activeCard: WeatherCardData
        get() = cards.getOrElse(selectedIndex) { cards.first() }
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun selectCardIndex(index: Int) {
        val validIndex = index.coerceIn(0, _uiState.value.cards.lastIndex)
        _uiState.update { it.copy(selectedIndex = validIndex) }
    }

    fun selectCondition(condition: WeatherCondition) {
        val idx = _uiState.value.cards.indexOfFirst { it.condition == condition }
        if (idx >= 0) {
            selectCardIndex(idx)
        }
    }

    fun toggleTemperatureUnit() {
        _uiState.update { it.copy(useFahrenheit = !it.useFahrenheit) }
    }

    fun openIconLab() {
        _uiState.update { it.copy(showIconLabSheet = true) }
    }

    fun closeIconLab() {
        _uiState.update { it.copy(showIconLabSheet = false) }
    }

    fun openDetailSheet() {
        _uiState.update { it.copy(showDetailSheet = true) }
    }

    fun closeDetailSheet() {
        _uiState.update { it.copy(showDetailSheet = false) }
    }

    fun updateIconSpeed(speed: Float) {
        _uiState.update { it.copy(iconSpeedMultiplier = speed.coerceIn(0.4f, 2.2f)) }
    }

    fun updateRefractionStrength(strength: Float) {
        _uiState.update { it.copy(refractionStrength = strength.coerceIn(0.5f, 1.5f)) }
    }

    fun updateInteractiveTouch(normOffset: Offset) {
        _uiState.update { it.copy(interactiveTouchNorm = normOffset) }
    }
}
