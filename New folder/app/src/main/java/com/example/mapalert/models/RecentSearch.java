package com.example.mapalert.models;

public class RecentSearch {
    private final String searchText;
    private final String mapImageUrl;

    public RecentSearch(String searchText, String mapImageUrl) {
        this.searchText = searchText;
        this.mapImageUrl = mapImageUrl;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getMapImageUrl() {
        return mapImageUrl;
    }
}