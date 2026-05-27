package com.example.myapplicationooo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripSuggestionAdapter extends RecyclerView.Adapter<TripSuggestionAdapter.ViewHolder> {

    private List<TripPlan> tripPlans = new ArrayList<>();
    private final OnTripClickListener listener;

    public interface OnTripClickListener {
        void onDeleteClick(String tripId);
    }

    public TripSuggestionAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setTripPlans(List<TripPlan> tripPlans) {
        this.tripPlans = tripPlans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripPlan plan = tripPlans.get(position);

        holder.tvTitle.setText(plan.getDestination());
        holder.tvActivity.setText(plan.getActivityType());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(plan.getCreatedAt().toDate()));

        // Hiển thị nội dung phân tích từ AI
        holder.tvAnalysis.setText(plan.getAiRecommendation());

        // Hiển thị điểm số đánh giá
        holder.tvComfort.setText("Comfort: " + plan.getComfortScore() + "/10");
        holder.tvSuitability.setText("Suitability: " + plan.getSuitabilityScore() + "/10");

        // Đổi màu điểm số dựa trên mức độ (Tốt: Xanh, Kém: Đỏ)
        if (plan.getSuitabilityScore() >= 7) {
            holder.tvSuitability.setTextColor(0xFF4CAF50);
        } else if (plan.getSuitabilityScore() >= 5) {
            holder.tvSuitability.setTextColor(0xFFFFC107);
        } else {
            holder.tvSuitability.setTextColor(0xFFFF5252);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(plan.getId()));
    }

    @Override
    public int getItemCount() {
        return tripPlans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvActivity, tvDate, tvAnalysis, tvComfort, tvSuitability;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTripTitle);
            tvActivity = itemView.findViewById(R.id.tvActivityType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAnalysis = itemView.findViewById(R.id.tvAIAnalysis);
            tvComfort = itemView.findViewById(R.id.tvComfortScore);
            tvSuitability = itemView.findViewById(R.id.tvSuitabilityScore);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
