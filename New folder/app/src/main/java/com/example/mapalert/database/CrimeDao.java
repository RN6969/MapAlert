package com.example.mapalert.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mapalert.models.Crime;

import java.util.List;

@Dao
public interface CrimeDao {

    // ✅ Insert crimes into database (replace duplicates)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCrimes(List<Crime> crimes);

    // ✅ Delete all crimes
    @Query("DELETE FROM crime_reports")
    void clearCrimes();

    // ✅ Retrieve all stored crimes
    @Query("SELECT * FROM crime_reports")
    List<Crime> getAllCrimes();
}
