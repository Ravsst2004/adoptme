package com.example.client.model;

import java.util.List;

public class Bookings {
    private boolean status;
    private String message;
    private List<BookingResult> data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<BookingResult> getData() {
        return data;
    }

    public class BookingResult {
        private int id;
        private int user_id;
        private int pet_id;
        private String status;
        private String created_at;
        private String updated_at;
        private User user;
        private Pet pet;

        public int getId() {
            return id;
        }

        public int getUserId() {
            return user_id;
        }

        public int getPetId() {
            return pet_id;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return created_at;
        }

        public String getUpdatedAt() {
            return updated_at;
        }

        public User getUser() {
            return user;
        }

        public Pet getPet() {
            return pet;
        }
    }
}
