package com.example.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Register;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);

        apiService = RetrofitInstance.getService(this);

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if ( name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.GONE);

        Call<Register> call = apiService.register(nameBody, emailBody, passwordBody);
        call.enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            JSONObject errorJson = new JSONObject(errorBody);

                            JSONObject errors = errorJson.optJSONObject("errors");
                            if (errors != null) {
                                etName.setError(null);
                                etEmail.setError(null);
                                etPassword.setError(null);

                                if (errors.has("name")) {
                                    JSONArray nameErrors = errors.getJSONArray("name");
                                    etName.setError(nameErrors.getString(0));
                                }
                                if (errors.has("email")) {
                                    JSONArray emailErrors = errors.getJSONArray("email");
                                    etEmail.setError(emailErrors.getString(0));
                                }
                                if (errors.has("password")) {
                                    JSONArray passwordErrors = errors.getJSONArray("password");
                                    etPassword.setError(passwordErrors.getString(0));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }




            @Override
            public void onFailure(Call<Register> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Register", "Failure: " + t.getMessage());
            }
        });
    }
}