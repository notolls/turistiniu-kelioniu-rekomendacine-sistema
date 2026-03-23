package com.example.projectkrs.adapters;

import com.google.android.libraries.places.api.model.Place;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdaptersLogicTest {

    @Test
    public void topPlaceAdapterFormatVisitCount_formatsPositiveCount() {
        assertEquals("Lankyta: 15", TopPlaceAdapter.formatVisitCount(15));
    }

    @Test
    public void topPlaceAdapterFormatVisitCount_formatsZeroCount() {
        assertEquals("Lankyta: 0", TopPlaceAdapter.formatVisitCount(0));
    }

    @Test
    public void topPlaceAdapterFormatVisitCount_formatsNegativeCount() {
        assertEquals("Lankyta: -3", TopPlaceAdapter.formatVisitCount(-3));
    }

    @Test
    public void topPlaceAdapterGetItemCount_returnsListSize() {
        TopPlaceAdapter adapter = new TopPlaceAdapter(Collections.emptyList());

        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void shopAdapterFormatPrice_formatsPositivePrice() {
        assertEquals("120 pts", ShopAdapter.formatPrice(120));
    }

    @Test
    public void shopAdapterFormatPrice_formatsZeroPrice() {
        assertEquals("0 pts", ShopAdapter.formatPrice(0));
    }

    @Test
    public void shopAdapterFormatPrice_formatsNegativePrice() {
        assertEquals("-1 pts", ShopAdapter.formatPrice(-1));
    }

    @Test
    public void shopAdapterGetItemCount_returnsListSize() {
        ShopAdapter adapter = new ShopAdapter(Collections.emptyList(), null);

        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void recommendationAdapterHasPhotoUrl_returnsFalse_forNull() {
        assertFalse(RecommendationAdapter.hasPhotoUrl(null));
    }

    @Test
    public void recommendationAdapterHasPhotoUrl_returnsFalse_forEmpty() {
        assertFalse(RecommendationAdapter.hasPhotoUrl(""));
    }

    @Test
    public void recommendationAdapterHasPhotoUrl_returnsTrue_forWhitespaceOnly() {
        assertTrue(RecommendationAdapter.hasPhotoUrl("   "));
    }

    @Test
    public void recommendationAdapterHasPhotoUrl_returnsTrue_forValidUrl() {
        assertTrue(RecommendationAdapter.hasPhotoUrl("https://example.com/image.jpg"));
    }

    @Test
    public void recommendationAdapterGetItemCount_returnsListSize() {
        RecommendationAdapter adapter = new RecommendationAdapter(null, Collections.emptyList(), place -> { });

        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void imageSliderAdapterBuildGooglePhotoUrl_buildsExpectedUrl() {
        String url = ImageSliderAdapter.buildGooglePhotoUrl("photoRef123", "apiKey456");

        assertEquals(
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=photoRef123&key=apiKey456",
                url
        );
    }

    @Test
    public void imageSliderAdapterBuildGooglePhotoUrl_handlesEmptyParts() {
        String url = ImageSliderAdapter.buildGooglePhotoUrl("", "");

        assertEquals(
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=&key=",
                url
        );
    }

    @Test
    public void imageSliderAdapterUpdateData_replacesListContents() {
        ArrayList<Place> places = new ArrayList<>();
        places.add(null);
        ImageSliderAdapter adapter = new ImageSliderAdapter(null, places);

        adapter.updateData(Arrays.asList(null, null));

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void postAdapterFormatPlaceWithDistanceLabel_formatsMetersToKilometers() {
        String label = PostAdapter.formatPlaceWithDistanceLabel("Vilnius", 1234.56);

        assertEquals("Vilnius - 1.23 Kilometrai", label);
    }

    @Test
    public void postAdapterFormatPlaceWithDistanceLabel_formatsZeroDistance() {
        String label = PostAdapter.formatPlaceWithDistanceLabel("Kaunas", 0);

        assertEquals("Kaunas - 0.00 Kilometrai", label);
    }

    @Test
    public void postAdapterFormatPlaceWithDistanceLabel_formatsNegativeDistance() {
        String label = PostAdapter.formatPlaceWithDistanceLabel("Klaipėda", -500);

        assertEquals("Klaipėda - -0.50 Kilometrai", label);
    }

    @Test
    public void postAdapterFormatPlaceWithDistanceLabel_roundsValue() {
        String label = PostAdapter.formatPlaceWithDistanceLabel("Panevėžys", 999);

        assertEquals("Panevėžys - 1.00 Kilometrai", label);
    }

    @Test
    public void postAdapterUpdateData_replacesListContents() {
        ArrayList<Object> items = new ArrayList<>();
        items.add(new Object());
        PostAdapter<Object> adapter = new PostAdapter<>(items);

        adapter.updateData(Arrays.asList(new Object(), new Object(), new Object()));

        assertEquals(3, adapter.getItemCount());
    }
}
