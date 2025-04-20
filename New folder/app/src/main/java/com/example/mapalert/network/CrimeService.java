package com.example.mapalert.network;

import com.example.mapalert.models.Crime;
import com.example.mapalert.models.CrimeReportRequest;
import com.example.mapalert.models.ReportResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CrimeService {
    @Headers("Content-Type: application/json")
    @POST("crimes/report") // Make sure this matches your FastAPI endpoint
    Call<ReportResponse> reportCrime(@Body CrimeReportRequest request);

    @GET("crimes/nearby")  // Ensure this matches your FastAPI endpoint
    Call<List<Crime>> getCrimesNearby(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius") double radius
    );

    @GET("/crimes/search")
    Call<List<Crime>> searchCrimes(
            @Query("crime_type") String crimeType,
            @Query("location") String location,
            @Query("user_lat") Double userLat,
            @Query("user_lon") Double userLon
    );


    @GET("crimes/all") // ✅ New endpoint to fetch all crimes
    Call<List<Crime>> getAllCrimes();

}

