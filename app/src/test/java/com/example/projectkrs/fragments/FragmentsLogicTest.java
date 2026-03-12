package com.example.projectkrs.fragments;

import com.example.projectkrs.model.PlaceWithCount;
import com.example.projectkrs.model.PlaceWithDistance;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    @Test
    public void chatIsMessageSendAllowed_returnsExpectedValues() {
        assertTrue(ChatFragment.isMessageSendAllowed("Sveiki", true));
        assertFalse(ChatFragment.isMessageSendAllowed("", true));
        assertFalse(ChatFragment.isMessageSendAllowed("Sveiki", false));
    }

    @Test
    public void chatShouldOpenUserMap_preventsOpeningOwnMap() {
        assertFalse(ChatFragment.shouldOpenUserMap("u1", "u1"));
        assertTrue(ChatFragment.shouldOpenUserMap("u2", "u1"));
        assertFalse(ChatFragment.shouldOpenUserMap(null, "u1"));
    }

    @Test
    public void chatFormatChatListItem_buildsExpectedText() {
        assertEquals("user@mail.com: Labas", ChatFragment.formatChatListItem("user@mail.com", "Labas"));
    }

    @Test
    public void homeToRadiusMeters_convertsKilometersToMeters() {
        assertEquals(5000, HomeFragment.toRadiusMeters(5));
    }

    @Test
    public void homeSelectTopPlaces_returnsLimitedPrefix() {
        List<Place> places = Arrays.asList(
                Place.builder().setId("1").setName("A").build(),
                Place.builder().setId("2").setName("B").build(),
                Place.builder().setId("3").setName("C").build()
        );

        List<Place> top2 = HomeFragment.selectTopPlaces(places, 2);

        assertEquals(2, top2.size());
        assertEquals("1", top2.get(0).getId());
        assertEquals("2", top2.get(1).getId());
    }

    @Test
    public void mapIsSamePoint_andCanOpenRoute_behaveAsExpected() {
        LatLng p1 = new LatLng(54.0, 25.0);
        LatLng p2 = new LatLng(54.0, 25.0);

        assertTrue(MapFragment.isSamePoint(p1, p2));
        assertFalse(MapFragment.isSamePoint(p1, new LatLng(55.0, 25.0)));
        assertTrue(MapFragment.canOpenRoute(2));
        assertFalse(MapFragment.canOpenRoute(1));
    }

    @Test
    public void mapBuildGoogleMapsRouteUrl_buildsOriginDestinationAndWaypoints() {
        List<LatLng> points = Arrays.asList(
                new LatLng(54.0, 25.0),
                new LatLng(54.1, 25.1),
                new LatLng(54.2, 25.2)
        );

        String url = MapFragment.buildGoogleMapsRouteUrl(points);

        assertTrue(url.contains("origin=54.0,25.0"));
        assertTrue(url.contains("destination=54.2,25.2"));
        assertTrue(url.contains("waypoints=54.1,25.1"));
    }

    @Test
    public void profileHelpers_returnExpectedValues() {
        assertTrue(ProfileFragment.shouldShowChangePasswordFailureToast(1, 0));
        assertFalse(ProfileFragment.shouldShowChangePasswordFailureToast(1, -1));
        assertTrue(ProfileFragment.isAchievementUnlockedByVisitedPlaces(35, 30));
        assertFalse(ProfileFragment.isAchievementUnlockedByVisitedPlaces(15, 20));
    }

    @Test
    public void searchHelpers_matchFilterAndQueryAndText() {
        assertTrue(SearchFragment.matchesFilter("all", "visited"));
        assertTrue(SearchFragment.matchesFilter("visited", "visited"));
        assertFalse(SearchFragment.matchesFilter("want", "visited"));

        assertTrue(SearchFragment.matchesQuery("", "Vilnius"));
        assertTrue(SearchFragment.matchesQuery("vil", "Vilnius"));
        assertFalse(SearchFragment.matchesQuery("klaip", "Vilnius"));

        assertEquals("Arčiausi", SearchFragment.getSortButtonText(true));
        assertEquals("Tolimiausi", SearchFragment.getSortButtonText(false));
        assertEquals("Rasta: 3", SearchFragment.buildResultCountText(3));
    }

    @Test
    public void searchContainsPlaceId_detectsExistingId() {
        List<PlaceWithDistance> places = new ArrayList<>();
        Place p1 = Place.builder().setId("id-1").setName("A").build();
        places.add(new PlaceWithDistance(p1, 10));

        assertTrue(SearchFragment.containsPlaceId(places, "id-1"));
        assertFalse(SearchFragment.containsPlaceId(places, "id-2"));
    }

    @Test
    public void statisticsFragmentHelpers_resolveAndSort() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("park", 1);
        counts.put("museum", 4);
        assertEquals("museum", StatisticsFragment.resolveMostVisitedType(counts));
        assertEquals("Nėra duomenų", StatisticsFragment.resolveMostVisitedType(new HashMap<>()));

        List<PlaceWithCount> places = new ArrayList<>();
        places.add(new PlaceWithCount("A", 2));
        places.add(new PlaceWithCount("B", 5));
        StatisticsFragment.sortPlacesByCountDesc(places);
        assertEquals("B", places.get(0).getName());
    }

}
