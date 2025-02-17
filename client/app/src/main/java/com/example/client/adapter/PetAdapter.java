package com.example.client.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.client.DetailPetActivity;
import com.example.client.R;
import com.example.client.UpdatePetActivity;
import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Pet;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    private Context context;
    private List<Pet> petList;
    private OnPetEditListener onPetEditListener;
    private ApiService apiService;
    SharedPreferences prefs;
    boolean isAdmin;

    public PetAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
        this.apiService = RetrofitInstance.getService(this.context);
        this.prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        this.isAdmin = prefs.getBoolean("isAdmin", false);
    }

    public void setPets(List<Pet> pets) {
        this.petList = pets;
        notifyDataSetChanged();
    }

    public void setOnPetEditListener(OnPetEditListener listener) {
        this.onPetEditListener = listener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.petName.setText(pet.getName());
        holder.petType.setText(pet.getType());
        holder.petDescription.setText(pet.getDescription());
        Glide.with(context)
                .load(pet.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.petImage);

        holder.btnEditPet.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnEditPet.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdatePetActivity.class);
            intent.putExtra("petId", pet.getId());
            intent.putExtra("petName", pet.getName());
            intent.putExtra("petType", pet.getType());
            intent.putExtra("petDescription", pet.getDescription());
            intent.putExtra("petImageUri", pet.getImage());
            context.startActivity(intent);
        });

        holder.btnDeletePet.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnDeletePet.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete this pet?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        holder.btnDeletePet.setVisibility(View.GONE);
                        holder.progressBarDelete.setVisibility(View.VISIBLE);

                        deletePet(pet.getId(), holder);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(true)
                    .show();
        });

        holder.btnSeeDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailPetActivity.class);
            intent.putExtra("petId", pet.getId());
            context.startActivity(intent);
        });
    }

    private void deletePet(int petId, PetViewHolder holder) {
        Call<Void> call = apiService.deletePet(petId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                holder.progressBarDelete.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Pet deleted successfully!", Toast.LENGTH_SHORT).show();
                    petList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                } else {
                    Toast.makeText(context, "Failed to delete pet", Toast.LENGTH_SHORT).show();
                    holder.btnDeletePet.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                holder.progressBarDelete.setVisibility(View.GONE);
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                holder.btnDeletePet.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView petName, petType, petDescription;
        ImageView petImage;
        Button btnEditPet, btnDeletePet, btnSeeDetail;
        public ProgressBar progressBarDelete;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petName = itemView.findViewById(R.id.petName);
            petType = itemView.findViewById(R.id.petType);
            petDescription = itemView.findViewById(R.id.petDescription);
            petImage = itemView.findViewById(R.id.petImage);
            btnEditPet = itemView.findViewById(R.id.btnEditPet);
            btnDeletePet = itemView.findViewById(R.id.btnDeletePet);
            progressBarDelete = itemView.findViewById(R.id.progressBarDelete);
            btnSeeDetail = itemView.findViewById(R.id.btnDetail);
        }
    }

    public interface OnPetEditListener {
        void onEdit(Pet pet);
    }

}
