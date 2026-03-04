package com.example.projectkrs.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Modelis rekomenduojamai vietai.
 * Galima išplėsti su daugiau laukų (adresas, atstumas, nuotrauka ir t.t.)
 */
public class PlaceRecommendation implements Parcelable {

    private String id;
    private String name;
    private String placeId;

    private String address;
    private double lat;
    private double lng;
    private String photoUrl;

    public PlaceRecommendation() {
        // Required empty constructor for Firebase / JSON
    }

    public PlaceRecommendation(String id, String name, String address, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public PlaceRecommendation(String name, String placeId, String address, double lat, double lng, String photoUrl) {
        this.name = name;
        this.placeId = placeId;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.photoUrl = photoUrl;
    }

    protected PlaceRecommendation(Parcel in) {
        id = in.readString();
        name = in.readString();
        placeId = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        photoUrl = in.readString();
    }

    public static final Creator<PlaceRecommendation> CREATOR = new Creator<PlaceRecommendation>() {
        @Override
        public PlaceRecommendation createFromParcel(Parcel in) {
            return new PlaceRecommendation(in);
        }

        @Override
        public PlaceRecommendation[] newArray(int size) {
            return new PlaceRecommendation[size];
        }
    };

    // ============================
    // GETTERS & SETTERS
    // ============================
    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) { this.address = address; }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) { this.lat = lat; }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) { this.lng = lng; }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(placeId);
        parcel.writeString(address);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(photoUrl);
    }
}