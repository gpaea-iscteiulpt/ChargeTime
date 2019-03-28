package com.tese.chargingtime;

import java.io.Serializable;

public class Park implements Serializable {

    private String name;
    private String description;
    private int iconPicture;
    private int occupancyPercentage;
    private double priceperhour;
    private int totalslots;
    private String workperiod;
    private String workhours;
    private double latitude;
    private double longitude;
    private String address;

//    public Park(String name, String description, long latitude, long longitude, String address, double pricePerHour, int totalSlots, String workPeriod, String workHours) {
//        this.name = name;
//        this.description = description;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.address = address;
//        this.pricePerHour = pricePerHour;
//        this.totalSlots = totalSlots;
//        this.workPeriod = workPeriod;
//        this.workHours = workHours;
//    }
//
//    public Park(String name, String description, long latitude, long longitude, String address, int occupancyPercentage, double pricePerHour, int totalSlots, String workPeriod, String workHours) {
//        this.name = name;
//        this.description = description;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.address = address;
//        if(occupancyPercentage >= 85){
//            iconPicture = R.drawable.parkingfull;
//        }else if(occupancyPercentage < 85 & occupancyPercentage >= 50){
//            iconPicture = R.drawable.parkinghalf;
//        }else{
//            iconPicture = R.drawable.parkingfree;
//        }
//        this.occupancyPercentage = occupancyPercentage;
//        this.pricePerHour = pricePerHour;
//        this.totalSlots = totalSlots;
//        this.workPeriod = workPeriod;
//        this.workHours = workHours;
//    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIconPicture() {
        return iconPicture;
    }

    public void setIconPicture(int iconPicture) {
        this.iconPicture = iconPicture;
    }

    public int getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(int occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public double getPricePerHour() {
        return priceperhour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.priceperhour = pricePerHour;
    }

    public int getTotalSlots() {
        return totalslots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalslots = totalSlots;
    }

    public String getWorkPeriod() {
        return workperiod;
    }

    public void setWorkPeriod(String workPeriod) {
        this.workperiod = workPeriod;
    }

    public String getWorkHours() {
        return workhours;
    }

    public void setWorkHours(String workHours) {
        this.workhours = workHours;
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
}
