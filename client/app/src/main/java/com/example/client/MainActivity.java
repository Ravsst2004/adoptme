package com.example.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.client.adapter.PetAdapter;
import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Pet;
import com.example.client.model.Result;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PetAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    FloatingActionButton fabAddPet, fabProfile, fabLogout, fabAdminDashboard;
    TextInputEditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("isAdmin", false);
        String isToken = prefs.getString("token", null);
        if (!prefs.contains("token")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.swipeRefreshLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAddPet = findViewById(R.id.fabAddPet);
        fabProfile = findViewById(R.id.fabProfile);
        fabLogout = findViewById(R.id.fabLogout);
        fabAdminDashboard = findViewById(R.id.fabAdminDashboard);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        adapter = new PetAdapter(this, petList);
        recyclerView.setAdapter(adapter);


        fabAddPet.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        fabProfile.setVisibility(!isAdmin && isToken != null ? View.VISIBLE : View.GONE);
        fabLogout.setVisibility(isAdmin && isToken != null ? View.VISIBLE : View.GONE);
        fabAdminDashboard.setVisibility(isAdmin && isToken != null ? View.VISIBLE : View.GONE);


        fabAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPetActivity.class);
                startActivity(intent);
            }
        });

        fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fabAdminDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                editTextSearch.setText("");
                searchPets(null);
                fetchPets();
            }
        });

        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = v.getText().toString().trim();
                searchPets(query);
                return true;
            }
            return false;
        });

        fetchPets();
    }

    private void fetchPets() {
        swipeRefreshLayout.setRefreshing(true);

        ApiService apiService = RetrofitInstance.getService(this);
        apiService.getPets(null).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    petList.clear();
                    petList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("API_ERROR", "Response is not successful");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("API_ERROR", "Error: " + t.getMessage());
            }
        });
    }

    private void searchPets(String type) {
        ApiService apiService = RetrofitInstance.getService(this);
        apiService.getPets(type).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pet> pets = response.body().getData();
                    adapter.setPets(pets);
                } else {
                    Toast.makeText(MainActivity.this, "No pets found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to fetch pets: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}