package com.example.mapalert.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapalert.models.Crime;

import java.util.List;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<List<Crime>> crimeResults = new MutableLiveData<>();

    public void setCrimeResults(List<Crime> results) {
        crimeResults.setValue(results);
    }

    public LiveData<List<Crime>> getCrimeResults() {
        return crimeResults;
    }
}
