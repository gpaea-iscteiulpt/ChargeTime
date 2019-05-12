package com.tese.chargingtime;

public class Constants {

    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    public static final long UPDATE_INTERVAL = 4000;
    public static final long FASTEST_INTERVAL = 2000;

    public static int CURRENT_BATTERY_LEVEL = 100;
    public static int MAXIMUM_BATTERY_CAPACITY = 75;
    public static int MAXIMUM_DISTANCE = 300;

    public static int getMaximumBatteryCapacity() {
        return MAXIMUM_BATTERY_CAPACITY;
    }

    public static void setMaximumBatteryCapacity(int maximumBatteryCapacity) {
        MAXIMUM_BATTERY_CAPACITY = maximumBatteryCapacity;
    }

    public static double getConsumptionPerMeter() {
        return CONSUMPTION_PER_METER;
    }

    public static void setConsumptionPerMeter(double consumptionPerMeter) {
        CONSUMPTION_PER_METER = consumptionPerMeter;
    }

    public static double CONSUMPTION_PER_METER = 0.00025;

    public static void setMaximumDistance(int value){
        MAXIMUM_DISTANCE = value;
    }

    public static void setCurrentBatteryLevel(int value){
        CURRENT_BATTERY_LEVEL = value;
    }

    //public static String USERNAME = "";
    //public static int USER_POINTS = 0;

    public static String USERNAME = "John Doe";
    public static int USER_POINTS = 50;

    public static void setUsername(String str){
        USERNAME = str;
    }

    public static void setUserPoints(int value){ USER_POINTS = value; }

    public static void incrementUserPoints(){ USER_POINTS += 10; }

    public static String getUsername(){ return USERNAME; }

    public static int getUserPoints(){ return USER_POINTS; }

    public static int getCurrentBatteryLevel() {
        return CURRENT_BATTERY_LEVEL;
    }

    public static int getMaximumDistance() {
        return MAXIMUM_DISTANCE;
    }


}
