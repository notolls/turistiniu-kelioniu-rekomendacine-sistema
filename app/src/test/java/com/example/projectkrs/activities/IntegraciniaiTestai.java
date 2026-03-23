package com.example.projectkrs.activities;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IntegraciniaiTestai {

    @Test
    public void registerPayload_containsFirebaseUserDefaultsAndSelectedCategory() {
        Map<String, Object> payload = RegisterActivity.buildInitialUserData("museum");

        assertEquals(3, payload.size());
        assertEquals("museum", payload.get("categoryType"));
        assertEquals(100, payload.get("points"));
        assertEquals("marker_default", payload.get("selectedMarker"));
    }

    @Test
    public void registerPayload_allowsMissingCategoryWithoutBreakingDefaultValues() {
        Map<String, Object> payload = RegisterActivity.buildInitialUserData(null);

        assertTrue(payload.containsKey("categoryType"));
        assertNull(payload.get("categoryType"));
        assertEquals(100, payload.get("points"));
        assertEquals("marker_default", payload.get("selectedMarker"));
    }

    @Test
    public void registerAndLoginPayloads_shareTheSameFirebaseDefaults() {
        Map<String, Object> registerPayload = RegisterActivity.buildInitialUserData("park");
        Map<String, Object> loginPayload = LoginActivity.buildDefaultUserData();

        assertEquals(registerPayload.get("points"), loginPayload.get("points"));
        assertEquals(registerPayload.get("selectedMarker"), loginPayload.get("selectedMarker"));
        assertEquals("park", registerPayload.get("categoryType"));
        assertFalse(loginPayload.containsKey("categoryType"));
    }

    @Test
    public void loginPayload_containsExpectedFirestoreDefaults() {
        Map<String, Object> payload = LoginActivity.buildDefaultUserData();

        assertEquals(2, payload.size());
        assertEquals(100, payload.get("points"));
        assertEquals("marker_default", payload.get("selectedMarker"));
    }

    @Test
    public void ownedMarkerPayload_marksDefaultMarkerAsOwned() {
        Map<String, Object> payload = LoginActivity.buildDefaultOwnedMarkerPayload();

        assertEquals(1, payload.size());
        assertEquals(Boolean.TRUE, payload.get("owned"));
    }

    @Test
    public void firebaseDefaultPayloadBuilders_returnIndependentMaps() {
        Map<String, Object> firstRegisterPayload = RegisterActivity.buildInitialUserData("cafe");
        Map<String, Object> secondRegisterPayload = RegisterActivity.buildInitialUserData("restaurant");
        Map<String, Object> firstLoginPayload = LoginActivity.buildDefaultUserData();
        Map<String, Object> secondLoginPayload = LoginActivity.buildDefaultUserData();

        assertNotSame(firstRegisterPayload, secondRegisterPayload);
        assertNotSame(firstLoginPayload, secondLoginPayload);
    }

    @Test
    public void shopMarkerSeedData_containsAllExpectedDocumentsForFirestoreBootstrap() {
        List<Map<String, Object>> markers = HomeActivity.buildDefaultShopMarkersList();

        assertEquals(3, markers.size());
        assertEquals("Violetinis markeris", markers.get(0).get("name"));
        assertEquals(10, markers.get(0).get("price"));
        assertEquals("marker_violet", markers.get(0).get("drawable"));
        assertEquals("Raudonas markeris", markers.get(1).get("name"));
        assertEquals(20, markers.get(1).get("price"));
        assertEquals("marker_red", markers.get(1).get("drawable"));
        assertEquals("Mėlynas markeris", markers.get(2).get("name"));
        assertEquals(30, markers.get(2).get("price"));
        assertEquals("marker_blue", markers.get(2).get("drawable"));
    }

    @Test
    public void backgroundSeedData_containsAllExpectedDocumentsForFirestoreBootstrap() {
        List<Map<String, Object>> backgrounds = HomeActivity.buildDefaultBackgroundsList();

        assertEquals(2, backgrounds.size());
        assertEquals("Žalias gradientas", backgrounds.get(0).get("name"));
        assertEquals(25, backgrounds.get(0).get("price"));
        assertEquals("green_gradient_background", backgrounds.get(0).get("drawable"));
        assertEquals("Mėlynas su taškais", backgrounds.get(1).get("name"));
        assertEquals(40, backgrounds.get(1).get("price"));
        assertEquals("blue_dots_background", backgrounds.get(1).get("drawable"));
    }

    @Test
    public void everySeededFirestoreShopItem_containsNamePriceAndDrawableFields() {
        List<Map<String, Object>> markers = HomeActivity.buildDefaultShopMarkersList();
        List<Map<String, Object>> backgrounds = HomeActivity.buildDefaultBackgroundsList();

        for (Map<String, Object> item : markers) {
            assertEquals(3, item.size());
            assertTrue(item.containsKey("name"));
            assertTrue(item.containsKey("price"));
            assertTrue(item.containsKey("drawable"));
        }

        for (Map<String, Object> item : backgrounds) {
            assertEquals(3, item.size());
            assertTrue(item.containsKey("name"));
            assertTrue(item.containsKey("price"));
            assertTrue(item.containsKey("drawable"));
        }
    }

    @Test
    public void firestoreSeedLists_returnFreshCollectionsOnEveryCall() {
        List<Map<String, Object>> firstMarkers = HomeActivity.buildDefaultShopMarkersList();
        List<Map<String, Object>> secondMarkers = HomeActivity.buildDefaultShopMarkersList();
        List<Map<String, Object>> firstBackgrounds = HomeActivity.buildDefaultBackgroundsList();
        List<Map<String, Object>> secondBackgrounds = HomeActivity.buildDefaultBackgroundsList();

        assertNotSame(firstMarkers, secondMarkers);
        assertNotSame(firstBackgrounds, secondBackgrounds);
        assertNotSame(firstMarkers.get(0), secondMarkers.get(0));
        assertNotSame(firstBackgrounds.get(0), secondBackgrounds.get(0));
    }
}
