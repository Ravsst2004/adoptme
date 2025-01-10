package com.example.client.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.client.R;
import com.example.client.model.Pet;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    private Context context;
    private List<Pet> petList;
    private OnPetEditListener onPetEditListener;

    public PetAdapter(Context context, List<Pet> petList) {
        this.context = context;
        this.petList = petList;
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

        holder.btnEditPet.setOnClickListener(v -> {
            if (onPetEditListener != null) {
                onPetEditListener.onEdit(pet);
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
        Button btnEditPet;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petName = itemView.findViewById(R.id.petName);
            petType = itemView.findViewById(R.id.petType);
            petDescription = itemView.findViewById(R.id.petDescription);
            petImage = itemView.findViewById(R.id.petImage);
            btnEditPet = itemView.findViewById(R.id.btnEditPet);
        }
    }

    public interface OnPetEditListener {
        void onEdit(Pet pet);
    }

}
