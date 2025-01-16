package com.example.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Pet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePetActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_REQUEST = 1;
    private EditText etPetName, etPetDescription;
    private Spinner spinnerPetType;
    private ImageView ivPetImage;
    private Button btnChooseImage, btnUpdatePet;
    private Uri selectedImageUri;
    private int petId;
    private ProgressBar progressBar;
    FloatingActionButton fabHome, fabLogout;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("isAdmin", false);
        if (!prefs.contains("token") && !isAdmin) {
            Intent intent = new Intent(UpdatePetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_pet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.updatePetActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etPetName = findViewById(R.id.etPetName);
        etPetDescription = findViewById(R.id.etPetDescription);
        spinnerPetType = findViewById(R.id.spinnerPetType);
        ivPetImage = findViewById(R.id.ivPetImage);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnUpdatePet = findViewById(R.id.btnUpdatePet);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitInstance.getService(this);

        Intent intent = getIntent();
        petId = intent.getIntExtra("petId", -1);
        String petName = intent.getStringExtra("petName");
        String petType = intent.getStringExtra("petType");
        String petDescription = intent.getStringExtra("petDescription");
        String petImageUri = intent.getStringExtra("petImageUri");

        fabHome = findViewById(R.id.fabHome);
        fabLogout = findViewById(R.id.fabLogout);
        etPetName.setText(petName);
        etPetDescription.setText(petDescription);
        ivPetImage.setImageURI(Uri.parse(petImageUri));

        if (petImageUri != null && !petImageUri.isEmpty()) {
            Glide.with(this)
                    .load(petImageUri)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivPetImage);
            ivPetImage.setVisibility(View.VISIBLE);
        } else {
            ivPetImage.setImageResource(R.drawable.ic_launcher_background);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.pet_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPetType.setAdapter(adapter);

        if (petType != null) {
            int spinnerPosition = adapter.getPosition(petType);
            if (spinnerPosition >= 0) {
                spinnerPetType.setSelection(spinnerPosition);
            }
        }

        btnChooseImage.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickImage, IMAGE_PICK_REQUEST);
        });

        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdatePetActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(UpdatePetActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnUpdatePet.setOnClickListener(v -> updatePet());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivPetImage.setImageURI(selectedImageUri);
            ivPetImage.setVisibility(View.VISIBLE);
        }
    }

    private void updatePet() {
        String petName = etPetName.getText().toString().trim();
        String petType = spinnerPetType.getSelectedItem().toString();
        String petDescription = etPetDescription.getText().toString().trim();

        if (petName.isEmpty() || petDescription.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdatePet.setVisibility(View.GONE);

        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), petName.isEmpty() ? "" : petName);
        RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), petType.isEmpty() ? "" : petType);
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), petDescription.isEmpty() ? "" : petDescription);

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = new File(getRealPathFromURI(selectedImageUri));
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("image", file.getName(), fileBody);
        }

        Call<Pet> call = apiService.updatePet(petId, nameBody, typeBody, descriptionBody, imagePart);

        call.enqueue(new Callback<Pet>() {
            @Override
            public void onResponse(Call<Pet> call, Response<Pet> response) {
                if (response.isSuccessful() && response.body() != null) {
                    progressBar.setVisibility(View.GONE);
                    btnUpdatePet.setVisibility(View.VISIBLE);

                    Toast.makeText(UpdatePetActivity.this, "Pet updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdatePetActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e("UPDATE PET", "Error: " + response.code() + ", " + response.message());
                    Toast.makeText(UpdatePetActivity.this, "Failed to update pet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Pet> call, Throwable t) {
                Toast.makeText(UpdatePetActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(columnIndex);
            cursor.close();
            return result;
        }
        return null;
    }
}