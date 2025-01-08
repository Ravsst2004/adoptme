package com.example.client.api;

import com.example.client.model.Result;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("pets")
    Call<Result> getPets();
}
