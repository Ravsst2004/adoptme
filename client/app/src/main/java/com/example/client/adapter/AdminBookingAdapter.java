package com.example.client.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.client.R;
import com.example.client.model.Bookings.BookingResult;

import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder> {
    private Context context;
    private List<BookingResult> bookings;

    public AdminBookingAdapter(Context context, List<BookingResult> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingResult booking = bookings.get(position);
        holder.tvBookingId.setText("Booking ID: " + booking.getId());
        holder.tvUserName.setText("User: " + booking.getUser().getName());
        holder.tvUserEmail.setText("Email: " + booking.getUser().getEmail());
        holder.tvPetName.setText("Pet: " + booking.getPet().getName());
        holder.tvPetType.setText("Type: " + booking.getPet().getType());
        holder.tvStatus.setText("Status: " + booking.getStatus());

        Glide.with(context)
                .load(booking.getPet().getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivPetImage);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvUserName, tvUserEmail, tvPetName, tvPetType, tvStatus;
        ImageView ivPetImage;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            tvPetType = itemView.findViewById(R.id.tvPetType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivPetImage = itemView.findViewById(R.id.ivPetImage);
        }
    }
}
