package com.example.mapalert.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingUtils {

    public interface GeocodingCallback {
        void onCoordinatesReceived(double latitude, double longitude);
        void onFailure(String errorMessage);
    }

    public static void getCoordinatesFromLocation(Context context, String locationName, GeocodingCallback callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                callback.onCoordinatesReceived(address.getLatitude(), address.getLongitude());
            } else {
                callback.onFailure("Location not found");
            }
        } catch (IOException e) {
            callback.onFailure("Geocoding error: " + e.getMessage());
        }
    }
}

