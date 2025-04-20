package com.example.mapalert.models;

public class Member {
    private String id;
    private String name;
    private double latitude;
    private double longitude;

    public Member() {
        // Empty constructor required for Firebase
    }

    public Member(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {  // ✅ Fix
        return latitude;
    }

    public double getLongitude() { // ✅ Fix
        return longitude;
    }
}
