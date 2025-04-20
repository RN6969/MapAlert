package com.example.mapalert;


import com.example.mapalert.models.Crime;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CrimeIncident {
    @SerializedName("crimes")
    private List<Crime> crimes;

    public List<Crime> getCrimes() {
        return crimes;
    }
}
