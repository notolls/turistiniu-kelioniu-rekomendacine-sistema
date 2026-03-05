package com.example.projectkrs.weather;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeatherLogicTest {

    @Test
    public void getParticleCountForWeather_returnsRainCount() {
        assertEquals(120, WeatherOverlayView.getParticleCountForWeather(WeatherOverlayView.WeatherType.RAIN));
    }

    @Test
    public void getParticleCountForWeather_returnsSnowCount() {
        assertEquals(100, WeatherOverlayView.getParticleCountForWeather(WeatherOverlayView.WeatherType.SNOW));
    }

    @Test
    public void getParticleCountForWeather_returnsZeroForNonParticleTypes() {
        assertEquals(0, WeatherOverlayView.getParticleCountForWeather(WeatherOverlayView.WeatherType.NONE));
        assertEquals(0, WeatherOverlayView.getParticleCountForWeather(WeatherOverlayView.WeatherType.NIGHT));
        assertEquals(0, WeatherOverlayView.getParticleCountForWeather(WeatherOverlayView.WeatherType.SUN));
    }

    @Test
    public void hasParticles_returnsTrueOnlyForRainAndSnow() {
        assertTrue(WeatherOverlayView.hasParticles(WeatherOverlayView.WeatherType.RAIN));
        assertTrue(WeatherOverlayView.hasParticles(WeatherOverlayView.WeatherType.SNOW));

        assertFalse(WeatherOverlayView.hasParticles(WeatherOverlayView.WeatherType.NONE));
        assertFalse(WeatherOverlayView.hasParticles(WeatherOverlayView.WeatherType.NIGHT));
        assertFalse(WeatherOverlayView.hasParticles(WeatherOverlayView.WeatherType.SUN));
    }

    @Test
    public void isOverlayOnly_returnsTrueForNightAndSun() {
        assertTrue(WeatherOverlayView.isOverlayOnly(WeatherOverlayView.WeatherType.NIGHT));
        assertTrue(WeatherOverlayView.isOverlayOnly(WeatherOverlayView.WeatherType.SUN));

        assertFalse(WeatherOverlayView.isOverlayOnly(WeatherOverlayView.WeatherType.NONE));
        assertFalse(WeatherOverlayView.isOverlayOnly(WeatherOverlayView.WeatherType.RAIN));
        assertFalse(WeatherOverlayView.isOverlayOnly(WeatherOverlayView.WeatherType.SNOW));
    }

    @Test
    public void getOverlayColor_returnsNightColor() {
        assertEquals(0x88000044, WeatherOverlayView.getOverlayColor(WeatherOverlayView.WeatherType.NIGHT));
    }

    @Test
    public void getOverlayColor_returnsSunColor() {
        assertEquals(0x33FFFF00, WeatherOverlayView.getOverlayColor(WeatherOverlayView.WeatherType.SUN));
    }

    @Test
    public void getOverlayColor_returnsZeroForNonOverlayTypes() {
        assertEquals(0, WeatherOverlayView.getOverlayColor(WeatherOverlayView.WeatherType.NONE));
        assertEquals(0, WeatherOverlayView.getOverlayColor(WeatherOverlayView.WeatherType.RAIN));
        assertEquals(0, WeatherOverlayView.getOverlayColor(WeatherOverlayView.WeatherType.SNOW));
    }

    @Test
    public void rainViewInitialDropCount_returnsExpectedValue() {
        assertEquals(120, RainView.getInitialDropCount());
    }

    @Test
    public void shouldResetDrop_returnsTrueOnlyWhenCurrentYIsGreaterThanHeight() {
        assertFalse(RainView.shouldResetDrop(100f, 100));
        assertFalse(RainView.shouldResetDrop(99.99f, 100));

        assertTrue(RainView.shouldResetDrop(100.01f, 100));
        assertTrue(RainView.shouldResetDrop(150f, 100));
    }
}
