package com.example.projectkrs.activities;

import com.example.projectkrs.R;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CoreActivitiesLogicTest {

    @Test
    public void loginInputValid_returnsTrue_whenBothValuesPresent() {
        assertTrue(LoginActivity.isLoginInputValid("user@example.com", "secret"));
    }

    @Test
    public void loginInputValid_returnsFalse_whenEmailEmpty() {
        assertFalse(LoginActivity.isLoginInputValid("", "secret"));
    }

    @Test
    public void loginInputValid_returnsFalse_whenPasswordEmpty() {
        assertFalse(LoginActivity.isLoginInputValid("user@example.com", ""));
    }

    @Test
    public void loginInputValid_returnsFalse_whenAnyValueNull() {
        assertFalse(LoginActivity.isLoginInputValid(null, "secret"));
        assertFalse(LoginActivity.isLoginInputValid("user@example.com", null));
    }

    @Test
    public void loginBuildDefaultUserData_containsExpectedDefaults() {
        Map<String, Object> data = LoginActivity.buildDefaultUserData();

        assertEquals(2, data.size());
        assertEquals(100, data.get("points"));
        assertEquals("marker_default", data.get("selectedMarker"));
    }

    @Test
    public void loginBuildDefaultUserData_returnsNewInstanceEachCall() {
        Map<String, Object> first = LoginActivity.buildDefaultUserData();
        Map<String, Object> second = LoginActivity.buildDefaultUserData();

        assertNotSame(first, second);
        first.put("x", 1);
        assertNull(second.get("x"));
    }

    @Test
    public void loginBuildDefaultOwnedMarkerPayload_containsOwnedTrue() {
        Map<String, Object> payload = LoginActivity.buildDefaultOwnedMarkerPayload();

        assertEquals(1, payload.size());
        assertEquals(true, payload.get("owned"));
    }

    @Test
    public void mainGetLayoutForUserState_returnsSplashWhenLoggedIn() {
        assertEquals(R.layout.splash_screen, MainActivity.getLayoutForUserState(true));
    }

    @Test
    public void mainGetLayoutForUserState_returnsAuthWhenLoggedOut() {
        assertEquals(R.layout.activity_main, MainActivity.getLayoutForUserState(false));
    }

    @Test
    public void mainShouldRequestLocationPermission_returnsTrue_whenBothPermissionsMissing() {
        assertTrue(MainActivity.shouldRequestLocationPermission(false, false));
    }

    @Test
    public void mainShouldRequestLocationPermission_returnsFalse_whenFineGranted() {
        assertFalse(MainActivity.shouldRequestLocationPermission(true, false));
    }

    @Test
    public void mainShouldRequestLocationPermission_returnsFalse_whenCoarseGranted() {
        assertFalse(MainActivity.shouldRequestLocationPermission(false, true));
    }

    @Test
    public void mainIsPermissionGrantedResult_returnsTrue_whenFirstGranted() {
        assertTrue(MainActivity.isPermissionGrantedResult(new int[]{0}));
    }

    @Test
    public void mainIsPermissionGrantedResult_returnsFalse_forNullOrEmptyOrDenied() {
        assertFalse(MainActivity.isPermissionGrantedResult(null));
        assertFalse(MainActivity.isPermissionGrantedResult(new int[]{}));
        assertFalse(MainActivity.isPermissionGrantedResult(new int[]{-1}));
    }

    @Test
    public void homeCreateItemMap_setsAllFields() {
        Map<String, Object> map = HomeActivity.createItemMap("Name", 10, "drawable_name");

        assertEquals(3, map.size());
        assertEquals("Name", map.get("name"));
        assertEquals(10, map.get("price"));
        assertEquals("drawable_name", map.get("drawable"));
    }

    @Test
    public void homeBuildDefaultShopMarkersList_containsExpectedItems() {
        List<Map<String, Object>> markers = HomeActivity.buildDefaultShopMarkersList();

        assertEquals(3, markers.size());
        assertEquals("Violetinis markeris", markers.get(0).get("name"));
        assertEquals("marker_violet", markers.get(0).get("drawable"));
        assertEquals("Raudonas markeris", markers.get(1).get("name"));
        assertEquals("Mėlynas markeris", markers.get(2).get("name"));
    }

    @Test
    public void homeBuildDefaultBackgroundsList_containsExpectedItems() {
        List<Map<String, Object>> backgrounds = HomeActivity.buildDefaultBackgroundsList();

        assertEquals(2, backgrounds.size());
        assertEquals("Žalias gradientas", backgrounds.get(0).get("name"));
        assertEquals("green_gradient_background", backgrounds.get(0).get("drawable"));
        assertEquals("Mėlynas su taškais", backgrounds.get(1).get("name"));
        assertEquals("blue_dots_background", backgrounds.get(1).get("drawable"));
    }

    @Test
    public void homeIsValidDrawableResId_returnsFalseOnlyForZero() {
        assertFalse(HomeActivity.isValidDrawableResId(0));
        assertTrue(HomeActivity.isValidDrawableResId(1));
        assertTrue(HomeActivity.isValidDrawableResId(-1));
    }

    @Test
    public void postDetailBuildPhotoUrl_buildsExpectedString() {
        String url = PostDetailActivity.buildPhotoUrl("photoRef", "apiKey", 800);

        assertEquals(
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photoreference=photoRef&key=apiKey",
                url
        );
    }

    @Test
    public void postDetailBuildPlaceDetailsUrl_buildsExpectedString() {
        String url = PostDetailActivity.buildPlaceDetailsUrl("place123", "keyABC");

        assertEquals(
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=place123&fields=name,reviews&key=keyABC",
                url
        );
    }

    @Test
    public void postDetailBuildNearbySearchUrl_buildsExpectedString() {
        String url = PostDetailActivity.buildNearbySearchUrl(54.6872, 25.2797, "keyXYZ");

        assertEquals(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=54.6872,25.2797&radius=5000&language=lt&type=tourist_attraction&key=keyXYZ",
                url
        );
    }

    @Test
    public void postDetailShouldDisableVisitedButton_returnsTrueOnlyForExistingDocWithField() {
        assertTrue(PostDetailActivity.shouldDisableVisitedButton(true, true));
        assertFalse(PostDetailActivity.shouldDisableVisitedButton(true, false));
        assertFalse(PostDetailActivity.shouldDisableVisitedButton(false, true));
        assertFalse(PostDetailActivity.shouldDisableVisitedButton(false, false));
    }
}
