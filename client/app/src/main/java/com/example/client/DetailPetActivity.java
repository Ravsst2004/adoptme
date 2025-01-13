package com.example.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.example.client.model.Pet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPetActivity extends AppCompatActivity {
    private TextView petName, petDescription, petType;
    private ImageView petImage;
    private ApiService apiService;
    FloatingActionButton fabAddPet, fabProfile, fabLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("isAdmin", false);
        String isToken = prefs.getString("token", null);
        if (!prefs.contains("token")) {
            Intent intent = new Intent(DetailPetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_pet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailPetActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        petName = findViewById(R.id.tvPetName);
        petDescription = findViewById(R.id.tvPetDescription);
        petType = findViewById(R.id.tvPetType);
        petImage = findViewById(R.id.ivPetImage);
        fabAddPet = findViewById(R.id.fabAddPet);
        fabProfile = findViewById(R.id.fabProfile);
        fabLogout = findViewById(R.id.fabLogout);

        apiService = RetrofitInstance.getService(this);

        int petId = getIntent().getIntExtra("petId", -1);
        if (petId != -1) {
            getPetDetails(petId);
        } else {
            Toast.makeText(this, "Invalid Pet ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        fabAddPet.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        fabProfile.setVisibility(!isAdmin && isToken != null ? View.VISIBLE : View.GONE);
        fabLogout.setVisibility(isAdmin && isToken != null ? View.VISIBLE : View.GONE);


        fabAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailPetActivity.this, AddPetActivity.class);
                startActivity(intent);
            }
        });

        fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailPetActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(DetailPetActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void getPetDetails(int petId) {
        Call<Pet> call = apiService.getPet(petId);
        call.enqueue(new Callback<Pet>() {
            @Override
            public void onResponse(Call<Pet> call, Response<Pet> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pet pet = response.body();
                    petName.setText(pet.getName());
                    petDescription.setText(pet.getDescription());
                    petType.setText(pet.getType());
                    Glide.with(DetailPetActivity.this)
                            .load(pet.getImage())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(petImage);
                } else {
                    Toast.makeText(DetailPetActivity.this, "Failed to load pet details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Pet> call, Throwable t) {
                Toast.makeText(DetailPetActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}