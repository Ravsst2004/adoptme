package com.example.client.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://192.168.1.9:8000/api/";

    public static ApiService getService(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request originalRequest = chain.request();
                            String url = originalRequest.url().toString();
                            String method = originalRequest.method();

                            boolean requiresAuth = url.contains("pets") &&
                                    (method.equals("POST") || method.equals("PUT") || method.equals("DELETE"));

                            Request.Builder requestBuilder = originalRequest.newBuilder();
                            if (requiresAuth) {
                                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                                String token = prefs.getString("token", null);
                                if (token != null) {
                                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                                }
                            }

                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit.create(ApiService.class);
    }
}
