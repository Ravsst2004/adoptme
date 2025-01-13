package com.example.client.model;

public class User {
    private int id;
    private String name;
    private String email;
    private int is_admin;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return is_admin == 1;
    }
}
