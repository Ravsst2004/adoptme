package com.example.client.adapter;

import android.app.AlertDialog;
import android.content.Context;
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
import com.example.client.AdminDashboardActivity;
import com.example.client.DetailPetActivity;
import com.example.client.R;
import com.example.client.api.ApiService;
import com.example.client.api.RetrofitInstance;
import com.example.client.model.Booking;
import com.example.client.model.Bookings.BookingResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder> {
    private Context context;
    private List<BookingResult> bookings;
    private ApiService apiService;
    private AdminDashboardActivity adminDashboardActivity;

    public AdminBookingAdapter(Context context, List<BookingResult> bookings) {
        this.context = context;
        this.bookings = bookings;
        this.apiService = RetrofitInstance.getService(context);
        this.adminDashboardActivity = (AdminDashboardActivity) context;
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

        holder.btnStatusDone.setOnClickListener(view -> {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.btnStatusDone.setVisibility(View.GONE);

            new AlertDialog.Builder(context)
                    .setTitle("Update Status Confirmation")
                    .setMessage("Are you sure you want to update the status?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        apiService.updateStatusBooking(booking.getId()).enqueue(new Callback<Booking>() {

                            @Override
                            public void onResponse(Call<Booking> call, Response<Booking> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    holder.tvStatus.setText("Status: " + response.body().getData().getStatus());
                                    Toast.makeText(context, "Pet already done", Toast.LENGTH_SHORT).show();
                                    holder.progressBar.setVisibility(View.GONE);
                                    holder.btnStatusDone.setVisibility(View.VISIBLE);
                                    adminDashboardActivity.loadBookings();
                                } else {
                                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Booking> call, Throwable t) {
                                holder.progressBar.setVisibility(View.GONE);
                                holder.btnStatusDone.setVisibility(View.VISIBLE);
                                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.btnStatusDone.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    })
                    .setCancelable(true)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvUserName, tvUserEmail, tvPetName, tvPetType, tvStatus;
        ImageView ivPetImage;
        Button btnStatusDone;
        ProgressBar progressBar;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            tvPetType = itemView.findViewById(R.id.tvPetType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivPetImage = itemView.findViewById(R.id.ivPetImage);
            btnStatusDone = itemView.findViewById(R.id.btnStatusDone);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
