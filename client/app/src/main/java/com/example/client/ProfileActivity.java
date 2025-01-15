package com.example.client;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Booking;
import com.example.client.model.Pet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    FloatingActionButton fabLogout, fabHome;
    TextView tvName, tvEmail, tvPetName, tvPetDescription, tvPetType;
    ImageView ivPetImage;
    Button btnDeleteBooking;
    ApiService apiService;
    LinearLayout llCardBookedPet;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", 0);
        String isToken = prefs.getString("token", null);
        String name = prefs.getString("name", "Default Name");
        String email = prefs.getString("email", "Default Email");
        if (!prefs.contains("token")) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fabLogout = findViewById(R.id.fabLogout);
        fabHome = findViewById(R.id.fabHome);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPetName = findViewById(R.id.tvPetName);
        tvPetDescription = findViewById(R.id.tvPetDescription);
        tvPetType = findViewById(R.id.tvPetType);
        ivPetImage = findViewById(R.id.ivPetImage);
        btnDeleteBooking = findViewById(R.id.btnDeleteBooking);
        llCardBookedPet = findViewById(R.id.llCardBookedPet);
        progressBar = findViewById(R.id.progressBar);
        apiService = RetrofitInstance.getService(this);

        tvName.setText(name);
        tvEmail.setText(email);

        fabLogout.setVisibility(isToken != null ? View.VISIBLE : View.GONE);

        fetchBookingAndPetData(userId, isToken);

        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnDeleteBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this pet?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            fetchBookingAndDeletePet(userId, isToken);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setCancelable(true)
                        .show();
            }
        });

    }

    private void fetchBookingAndDeletePet(int userId, String token) {
        Call<Booking> bookingCall = apiService.getUserBooking(userId, "Bearer " + token);

        progressBar.setVisibility(View.VISIBLE);
        btnDeleteBooking.setVisibility(View.GONE);

        bookingCall.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Booking booking = response.body();
                    int bookingId = booking.getData().getId();
                    deleteBooking(bookingId, token);
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteBooking(int bookingId, String token) {
        Call<Void> deleteCall = apiService.deleteBooking(bookingId);

        deleteCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    btnDeleteBooking.setVisibility(View.VISIBLE);

                    Toast.makeText(ProfileActivity.this, "Booking deleted successfully.", Toast.LENGTH_SHORT).show();
                    clearPetDetails();
                    llCardBookedPet.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to delete booking.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchBookingAndPetData(int userId, String token) {
        Call<Booking> bookingCall = apiService.getUserBooking(userId, "Bearer " + token);

        bookingCall.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Booking booking = response.body();
                    int petId = booking.getData().getPet_id();
                    String status = booking.getData().getStatus();
                    Log.d("Status", status);
                    if (status.equals("active")) {
                        llCardBookedPet.setVisibility(View.VISIBLE);
                        fetchPetDetails(petId);
                    } else {
                        llCardBookedPet.setVisibility(View.GONE);
                        clearPetDetails();
                    }

                } else {
                    llCardBookedPet.setVisibility(View.GONE);
                    clearPetDetails();
                    Toast.makeText(ProfileActivity.this, "No booking data available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchPetDetails(int petId) {
        Call<Pet> petCall = apiService.getPet(petId);

        petCall.enqueue(new Callback<Pet>() {
            @Override
            public void onResponse(Call<Pet> call, Response<Pet> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pet pet = response.body();
                    displayPetDetails(pet);
                } else {
                    clearPetDetails();
                    Toast.makeText(ProfileActivity.this, "Failed to fetch pet details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Pet> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPetDetails(Pet pet) {
        tvPetName.setText(pet.getName());
        tvPetDescription.setText(pet.getDescription());
        tvPetType.setText(pet.getType());
        Glide.with(this).load(pet.getImage()).into(ivPetImage);
    }

    private void clearPetDetails() {
        tvPetName.setText("");
        tvPetDescription.setText("");
        tvPetType.setText("");
        ivPetImage.setImageResource(0);
    }
}