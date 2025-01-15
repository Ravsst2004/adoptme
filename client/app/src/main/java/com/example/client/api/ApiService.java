package com.example.client.api;

import com.example.client.model.Booking;
import com.example.client.model.Bookings;
import com.example.client.model.Login;
import com.example.client.model.Pet;
import com.example.client.model.Register;
import com.example.client.model.Result;
import com.example.client.model.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("pets")
    Call<Result> getPets(@Query("type") String type);

    @GET("pets/{id}")
    Call<Pet> getPet(@Path("id") int petId);

    @Multipart
    @POST("pets")
    Call<Pet> addPet(
            @Part("name") RequestBody name,
            @Part("type") RequestBody type,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("pets/{id}?_method=PUT")
    Call<Pet> updatePet(
            @Path("id") int petId,
            @Part("name") RequestBody name,
            @Part("type") RequestBody type,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part image
    );

    @DELETE("pets/{id}")
    Call<Void> deletePet(
            @Path("id") int petId
    );

    @Multipart
    @POST("login")
    Call<Login> login(
            @Part("email") RequestBody email,
            @Part("password") RequestBody password
    );

    @Multipart
    @POST("register")
    Call<Register> register(
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password
    );

    @Multipart
    @POST("booking")
    Call<Booking> bookingPet(
            @Part("pet_id") RequestBody petId,
            @Part("user_id") RequestBody userId
    );

    @GET("booking")
    Call<Booking> getUserBooking(
            @Query("user_id") int userId,
            @Header("Authorization") String token
    );

    @DELETE("booking/{bookingId}")
    Call<Void> deleteBooking(
            @Path("bookingId") int bookingId
    );

    @GET("bookings")
    Call<Bookings> getBookings(
            @Header("Authorization") String token
    );

}
