package com.tese.chargingtime;

import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;

import java.io.Serializable;

public class Station implements Serializable {

    private String name;
    private int cost;
    private int connectors;
    private int numberOfChargingPoints;
    private int hours;
    private String type;
    private double latitude;
    private double longitude;
    private double altitude;
    private int icon;
    private double occupancy;

    public double getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(double occupancy) {
        this.occupancy = occupancy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getConnectors() {
        return connectors;
    }

    public void setConnectors(int connectors) {
        this.connectors = connectors;
    }

    public int getNumberOfChargingPoints() {
        return numberOfChargingPoints;
    }

    public void setNumberOfChargingPoints(int numberOfChargingPoints) {
        this.numberOfChargingPoints = numberOfChargingPoints;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public int getIcon() {
        return R.drawable.electric_station;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
