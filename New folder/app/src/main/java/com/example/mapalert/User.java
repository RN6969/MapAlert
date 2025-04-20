package com.example.mapalert;

public class User {
    public String name, email, username, password;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;

    }
}
