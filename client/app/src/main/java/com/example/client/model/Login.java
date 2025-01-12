package com.example.client.model;

public class Login {
    private boolean status;
    private String message;
    private Data data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        private User user;
        private String token;

        public User getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }

}
