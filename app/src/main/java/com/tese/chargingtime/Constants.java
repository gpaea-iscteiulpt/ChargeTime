package com.tese.chargingtime;

public class Constants {

    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    public static final long UPDATE_INTERVAL = 4000;
    public static final long FASTEST_INTERVAL = 2000;

    public static int LEAF_BATTERY_CAPACITY = 30;
    public static int LEAF_MAXIMUM_DISTANCE = 172000;
    public static int LEAF_TOTAL_WEIGHT = 1481;
    public static int LEAF_CONSUMPTION_PER_METER = 0;

    public static int ZOE_BATTERY_CAPACITY = 41;
    public static int ZOE_MAXIMUM_DISTANCE = 400000;
    public static int ZOE_TOTAL_WEIGHT = 1468;
    public static int ZOE_CONSUMPTION_PER_METER = 0;

    public static int getLeafBatteryCapacity() {
        return LEAF_BATTERY_CAPACITY;
    }

    public static void setLeafBatteryCapacity(int leafBatteryCapacity) {
        LEAF_BATTERY_CAPACITY = leafBatteryCapacity;
    }

    public static int getLeafMaximumDistance() {
        return LEAF_MAXIMUM_DISTANCE;
    }

    public static void setLeafMaximumDistance(int leafMaximumDistance) {
        LEAF_MAXIMUM_DISTANCE = leafMaximumDistance;
    }

    public static int getLeafTotalWeight() {
        return LEAF_TOTAL_WEIGHT;
    }

    public static void setLeafTotalWeight(int leafTotalWeight) {
        LEAF_TOTAL_WEIGHT = leafTotalWeight;
    }

    public static int getLeafConsumptionPerMeter() {
        return LEAF_BATTERY_CAPACITY / (LEAF_MAXIMUM_DISTANCE * 100);
    }

    public static void setLeafConsumptionPerMeter(int leafConsumptionPerMeter) {
        LEAF_CONSUMPTION_PER_METER = LEAF_BATTERY_CAPACITY / (LEAF_MAXIMUM_DISTANCE * 100);
    }

    public static int getZoeBatteryCapacity() {
        return ZOE_BATTERY_CAPACITY;
    }

    public static void setZoeBatteryCapacity(int zoeBatteryCapacity) {
        ZOE_BATTERY_CAPACITY = zoeBatteryCapacity;
    }

    public static int getZoeMaximumDistance() {
        return ZOE_MAXIMUM_DISTANCE;
    }

    public static void setZoeMaximumDistance(int zoeMaximumDistance) {
        ZOE_MAXIMUM_DISTANCE = zoeMaximumDistance;
    }

    public static int getZoeTotalWeight() {
        return ZOE_TOTAL_WEIGHT;
    }

    public static void setZoeTotalWeight(int zoeTotalWeight) {
        ZOE_TOTAL_WEIGHT = zoeTotalWeight;
    }

    public static int getZoeConsumptionPerMeter() {
        return ZOE_BATTERY_CAPACITY / (ZOE_MAXIMUM_DISTANCE * 100);
    }

    public static void setZoeConsumptionPerMeter(int zoeConsumptionPerMeter) {
        ZOE_CONSUMPTION_PER_METER = ZOE_BATTERY_CAPACITY / (ZOE_MAXIMUM_DISTANCE * 100);
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



}
