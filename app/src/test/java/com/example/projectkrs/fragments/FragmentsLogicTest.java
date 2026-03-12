package com.example.projectkrs.fragments;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FragmentsLogicTest {

    @Test
    public void formatPointsText_formatsPositivePoints() {
        assertEquals("Taškai: 250", ShopFragment.formatPointsText(250));
    }

    @Test
    public void formatPointsText_formatsZeroPoints() {
        assertEquals("Taškai: 0", ShopFragment.formatPointsText(0));
    }

    @Test
    public void formatPointsText_formatsNegativePoints() {
        assertEquals("Taškai: -5", ShopFragment.formatPointsText(-5));
    }

    @Test
    public void canAffordPurchase_returnsTrue_whenPointsGreaterThanPrice() {
        assertTrue(ShopFragment.canAffordPurchase(200, 150));
    }

    @Test
    public void canAffordPurchase_returnsTrue_whenPointsEqualPrice() {
        assertTrue(ShopFragment.canAffordPurchase(150, 150));
    }

    @Test
    public void canAffordPurchase_returnsFalse_whenPointsLowerThanPrice() {
        assertFalse(ShopFragment.canAffordPurchase(149, 150));
    }

    @Test
    public void isMarkerDrawable_returnsTrue_forMarkerName() {
        assertTrue(ShopFragment.isMarkerDrawable("marker_default"));
    }

    @Test
    public void isMarkerDrawable_returnsTrue_whenMarkerIsInMiddle() {
        assertTrue(ShopFragment.isMarkerDrawable("shop_marker_violet"));
    }

    @Test
    public void isMarkerDrawable_returnsFalse_forBackgroundName() {
        assertFalse(ShopFragment.isMarkerDrawable("bg_blue"));
    }

    @Test
    public void isMarkerDrawable_returnsFalse_forNullName() {
        assertFalse(ShopFragment.isMarkerDrawable(null));
    }

    @Test
    public void buildPurchaseUpdate_buildsMarkerUpdate() {
        Map<String, Object> update = ShopFragment.buildPurchaseUpdate(90, "marker_red");

        assertEquals(2, update.size());
        assertEquals(90, update.get("points"));
        assertEquals("marker_red", update.get("selectedMarker"));
        assertNull(update.get("selectedBackground"));
    }

    @Test
    public void buildPurchaseUpdate_buildsBackgroundUpdate() {
        Map<String, Object> update = ShopFragment.buildPurchaseUpdate(80, "bg_black");

        assertEquals(2, update.size());
        assertEquals(80, update.get("points"));
        assertEquals("bg_black", update.get("selectedBackground"));
        assertNull(update.get("selectedMarker"));
    }

    @Test
    public void buildPurchaseUpdate_treatsNullDrawableAsBackground() {
        Map<String, Object> update = ShopFragment.buildPurchaseUpdate(70, null);

        assertEquals(2, update.size());
        assertEquals(70, update.get("points"));
        assertTrue(update.containsKey("selectedBackground"));
        assertNull(update.get("selectedBackground"));
    }

    @Test
    public void buildPurchaseUpdate_overwritesPointsWithProvidedValue() {
        Map<String, Object> update = ShopFragment.buildPurchaseUpdate(-20, "marker_default");

        assertEquals(-20, update.get("points"));
    }
}
