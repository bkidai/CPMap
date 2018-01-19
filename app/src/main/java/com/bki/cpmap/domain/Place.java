package com.bki.cpmap.domain;

import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;

public class Place {
    private String addressName;
    private double latitude;
    private double longitude;

    public Place() {
    }

    public String getAddressName() {
        return addressName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Place parse(CarmenFeature feature) {
        this.setAddressName(feature.getPlaceName());
        this.setLatitude(feature.asPosition().getLatitude());
        this.setLongitude(feature.asPosition().getLongitude());
        return this;
    }
}
