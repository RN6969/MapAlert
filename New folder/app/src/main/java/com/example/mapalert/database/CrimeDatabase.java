package com.example.mapalert.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mapalert.models.Crime;

@Database(entities = {Crime.class}, version = 1, exportSchema = false)
public abstract class CrimeDatabase extends RoomDatabase {
    private static volatile CrimeDatabase INSTANCE;

    public abstract CrimeDao crimeDao();

    public static synchronized CrimeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CrimeDatabase.class, "crime_reports.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
