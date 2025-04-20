package com.example.mapalert.models;

import com.google.gson.annotations.SerializedName;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "crime_reports")
public class Crime {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    public String username;

    @SerializedName("crime_type")
    public String crimeType;

    @SerializedName("description")
    public String description;

    @SerializedName("date")
    public String date;

    @SerializedName("location")
    public String location;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    // ✅ Constructor used by Room (ID auto-generated)
    public Crime(String username, String crimeType, String description, String date, String location, double latitude, double longitude) {
        this.username = username;
        this.crimeType = crimeType;
        this.description = description;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ✅ Constructor used by API (Ignored by Room)
    public Crime(int id, String username, String crimeType, String description, String date, String location, double latitude, double longitude) {
        this.id = id;
        this.username = username;
        this.crimeType = crimeType;
        this.description = description;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Crime() {

    }


    // ✅ Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public String getCrimeType() { return crimeType; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
