package com.example.projectkrs.model;

import com.google.firebase.Timestamp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ModelClassesTest {

    @Test
    public void commentConstructorAndSetters_workCorrectly() {
        Comment comment = new Comment("Jonas", "Puiki vieta", 4.5f);

        assertEquals("Jonas", comment.getAuthor());
        assertEquals("Puiki vieta", comment.getText());
        assertEquals(4.5f, comment.getRating(), 0.0f);

        comment.setAuthor("Ieva");
        comment.setText("Labai patiko");
        comment.setRating(5.0f);

        assertEquals("Ieva", comment.getAuthor());
        assertEquals("Labai patiko", comment.getText());
        assertEquals(5.0f, comment.getRating(), 0.0f);
    }

    @Test
    public void shopMarkerSettersAndGetters_workCorrectly() {
        ShopMarker marker = new ShopMarker();

        marker.setName("Raudonas žymeklis");
        marker.setPrice(120);
        marker.setDrawable("marker_red");

        assertEquals("Raudonas žymeklis", marker.getName());
        assertEquals(120, marker.getPrice());
        assertEquals("marker_red", marker.getDrawable());
    }

    @Test
    public void shopBackgroundConstructor_setsAllFields() {
        ShopBackground background = new ShopBackground("Mėlynas fonas", 250, "bg_blue");

        assertEquals("Mėlynas fonas", background.getName());
        assertEquals(250, background.getPrice());
        assertEquals("bg_blue", background.getDrawable());
    }

    @Test
    public void placeWithCountConstructor_setsNameAndCount() {
        PlaceWithCount placeWithCount = new PlaceWithCount("Vilnius", 7);

        assertEquals("Vilnius", placeWithCount.getName());
        assertEquals(7, placeWithCount.getCount());
    }

    @Test
    public void chatMessageConstructor_setsAllFields() {
        Timestamp timestamp = Timestamp.now();
        ChatMessage message = new ChatMessage("Sveiki!", "user@example.com", "uid123", timestamp);

        assertEquals("Sveiki!", message.getText());
        assertEquals("user@example.com", message.getUserEmail());
        assertEquals("uid123", message.getUserId());
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    public void placeRecommendationConstructorsAndSetters_workCorrectly() {
        PlaceRecommendation recommendation = new PlaceRecommendation(
                "Trakai", "place123", "Karaimų g. 1", 54.64, 24.93, "photoUrl"
        );

        assertEquals("Trakai", recommendation.getName());
        assertEquals("place123", recommendation.getPlaceId());
        assertEquals("Karaimų g. 1", recommendation.getAddress());
        assertEquals(54.64, recommendation.getLat(), 0.0);
        assertEquals(24.93, recommendation.getLng(), 0.0);
        assertEquals("photoUrl", recommendation.getPhotoUrl());

        recommendation.setId("id-1");
        recommendation.setName("Kaunas");
        recommendation.setPlaceId("place999");
        recommendation.setAddress("Laisvės al. 1");
        recommendation.setLat(54.90);
        recommendation.setLng(23.90);
        recommendation.setPhotoUrl("newPhotoUrl");

        assertEquals("id-1", recommendation.getId());
        assertEquals("Kaunas", recommendation.getName());
        assertEquals("place999", recommendation.getPlaceId());
        assertEquals("Laisvės al. 1", recommendation.getAddress());
        assertEquals(54.90, recommendation.getLat(), 0.0);
        assertEquals(23.90, recommendation.getLng(), 0.0);
        assertEquals("newPhotoUrl", recommendation.getPhotoUrl());
    }

    @Test
    public void placeRecommendationEmptyConstructor_initializesWithNullsAndZeros() {
        PlaceRecommendation recommendation = new PlaceRecommendation();

        assertNull(recommendation.getId());
        assertNull(recommendation.getName());
        assertNull(recommendation.getPlaceId());
        assertNull(recommendation.getAddress());
        assertEquals(0.0, recommendation.getLat(), 0.0);
        assertEquals(0.0, recommendation.getLng(), 0.0);
        assertNull(recommendation.getPhotoUrl());
    }
}
