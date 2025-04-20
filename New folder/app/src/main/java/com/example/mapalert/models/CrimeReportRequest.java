package com.example.mapalert.models;

import com.google.gson.annotations.SerializedName;

public class CrimeReportRequest {

    @SerializedName("username")  // ✅ Ensure correct field name for API
    private String username;

    @SerializedName("crime_type")  // ✅ Renamed to match API
    private String crimeType;

    @SerializedName("description")
    private String description;

    @SerializedName("date")  // ✅ Fix date format
    private String date;

    @SerializedName("latitude")  // ✅ Send latitude separately
    private double latitude;

    @SerializedName("longitude")  // ✅ Send longitude separately
    private double longitude;

    public CrimeReportRequest(String username, String crimeType, String description, String date, double latitude, double longitude) {
        this.username = username;
        this.crimeType = crimeType;
        this.description = description;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
