package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.WeatherCondition
import com.example.model.WeatherRepository
import com.example.viewmodel.WeatherViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("琉璃天气", appName)
    }

    @Test
    fun `weather repository provides four conditions and viewmodel switches cleanly`() {
        val cards = WeatherRepository.allWeatherCards
        assertEquals(4, cards.size)
        assertEquals(
            listOf(
                WeatherCondition.SUNNY,
                WeatherCondition.WINDY,
                WeatherCondition.RAINY,
                WeatherCondition.SNOWY
            ),
            cards.map { it.condition }
        )

        val vm = WeatherViewModel()
        assertEquals(WeatherCondition.SUNNY, vm.uiState.value.activeCard.condition)

        vm.selectCondition(WeatherCondition.RAINY)
        assertEquals(WeatherCondition.RAINY, vm.uiState.value.activeCard.condition)

        vm.openIconLab()
        assertTrue(vm.uiState.value.showIconLabSheet)
    }
}
