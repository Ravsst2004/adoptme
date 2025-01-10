package com.example.client.api;

import com.example.client.model.Pet;
import com.example.client.model.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @GET("pets")
    Call<Result> getPets();

    @Multipart
    @POST("pets")
    Call<Pet> addPet(
            @Part("name") RequestBody name,
            @Part("type") RequestBody type,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part image
    );

}
