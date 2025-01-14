package com.example.client.model;

public class Booking {
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
        private int id;
        private int user_id;
        private int pet_id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public int getPet_id() {
            return pet_id;
        }

        public void setPet_id(int pet_id) {
            this.pet_id = pet_id;
        }
    }
}
