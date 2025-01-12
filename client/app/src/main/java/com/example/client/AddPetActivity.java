package com.example.client;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Pet;
import com.example.client.model.Result;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.ProgressBar;


public class AddPetActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_REQUEST = 1;
    private EditText etPetName, etPetDescription;
    private Spinner spinnerPetType;
    private ImageView ivPetImage;
    private Button btnChooseImage, btnSavePet;
    private Uri selectedImageUri;
    private ProgressBar progressBar;

    private ApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_pet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etPetName = findViewById(R.id.etPetName);
        etPetDescription = findViewById(R.id.etPetDescription);
        spinnerPetType = findViewById(R.id.spinnerPetType);
        ivPetImage = findViewById(R.id.ivPetImage);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSavePet = findViewById(R.id.btnSavePet);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitInstance.getService(this);

        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_REQUEST);
        });

        btnSavePet.setOnClickListener(v -> savePet());
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

    private void savePet() {
        String petName = etPetName.getText().toString().trim();
        String petType = spinnerPetType.getSelectedItem().toString();
        String petDescription = etPetDescription.getText().toString().trim();

        if (petName.isEmpty() || petDescription.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSavePet.setVisibility(View.GONE);

        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), petName);
        RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), petType);
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), petDescription);

        // Convert image file to MultipartBody.Part
        File file = new File(getRealPathFromURI(selectedImageUri));
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), fileBody);

        Call<Pet> call = apiService.addPet(nameBody, typeBody, descriptionBody, imagePart);

        call.enqueue(new Callback<Pet>() {
            @Override
            public void onResponse(Call<Pet> call, Response<Pet> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pet pet = response.body();

                    progressBar.setVisibility(View.GONE);
                    btnSavePet.setVisibility(View.VISIBLE);

                    if (response.isSuccessful()) {
                        Toast.makeText(AddPetActivity.this, "Pet added successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddPetActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("ADD PET", "Error: " + response.code() + ", " + response.message());
                        Toast.makeText(AddPetActivity.this, "Failed to add pet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddPetActivity.this, "Failed to add pet", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Pet> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddPetActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
        return null;
    }
}