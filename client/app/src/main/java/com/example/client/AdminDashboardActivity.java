package com.example.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.client.adapter.AdminBookingAdapter;
import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Bookings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ApiService apiService;
    private FloatingActionButton fabHome, fabLogout, fabAddPet;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("isAdmin", false);
        String isToken = prefs.getString("token", null);
        if (!isAdmin && !isToken.equals("admin")) {
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fabHome = findViewById(R.id.fabHome);
        fabLogout = findViewById(R.id.fabLogout);
        fabAddPet = findViewById(R.id.fabAddPet);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitInstance.getService(this);
        loadBookings();

        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        fabAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AddPetActivity.class);
                startActivity(intent);
            }
        });


        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBookings();
            }
        });
    }

    public void loadBookings() {
        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("token", null);

        swipeRefreshLayout.setRefreshing(true);

        apiService.getBookings("Bearer " + token).enqueue(new Callback<Bookings>() {
            @Override
            public void onResponse(Call<Bookings> call, Response<Bookings> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new AdminBookingAdapter(AdminDashboardActivity.this, response.body().getData()));
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Bookings> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);

                Toast.makeText(AdminDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}