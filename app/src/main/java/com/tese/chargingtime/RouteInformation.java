package com.tese.chargingtime;

public class RouteInformation {

    public double duration;
    public int spaceProbability;

    public RouteInformation(double duration, int spaceProbability) {
        this.duration = duration;
        this.spaceProbability = spaceProbability;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getSpaceProbability() {
        return spaceProbability;
    }

    public void setSpaceProbability(int spaceProbability) {
        this.spaceProbability = spaceProbability;
    }
}
