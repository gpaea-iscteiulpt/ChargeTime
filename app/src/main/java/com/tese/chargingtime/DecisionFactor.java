package com.tese.chargingtime;

import com.google.android.gms.maps.model.Marker;

public class DecisionFactor {

    public double duration;
    public double occupancy;
    public Marker marker;
    public double distance;
    public double distanceDestinationToCS;
    public double weight;

    public DecisionFactor(double duration, double occupancy, double distance, double distanceDestinationToCS, Marker marker) {
        this.duration = duration;
        this.occupancy = occupancy;
        this.distanceDestinationToCS = distanceDestinationToCS;
        this.distance = distance;
        this.marker = marker;
    }

    public DecisionFactor() {
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistanceDestinationToCS() {
        return distanceDestinationToCS;
    }

    public void setDistanceDestinationToCS(double distanceDestinationToCS) {
        this.distanceDestinationToCS = distanceDestinationToCS;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(double occupancy) {
        this.occupancy = occupancy;
    }
}
