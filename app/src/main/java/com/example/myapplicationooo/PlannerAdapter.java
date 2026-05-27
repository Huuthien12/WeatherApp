package com.example.myapplicationooo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlannerAdapter extends RecyclerView.Adapter<PlannerAdapter.ViewHolder> {

    private List<PlanModel> planList = new ArrayList<>();
    private final OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onDeleteClick(String planId);
    }

    public PlannerAdapter(OnPlanClickListener listener) {
        this.listener = listener;
    }

    public void setPlans(List<PlanModel> plans) {
        this.planList = plans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanModel plan = planList.get(position);

        holder.tvTitle.setText(plan.getTitle());
        holder.tvCity.setText(plan.getCityName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        holder.tvTime.setText(sdf.format(plan.getPlanTime().toDate()));

        holder.tvStatus.setText(plan.getWeatherStatus());
        holder.tvRecommendation.setText(plan.getRecommendation());

        // Update UI based on status
        if (plan.getStatusType() == 1) { // Rain/Alert
            holder.layoutBadge.setBackgroundResource(R.drawable.bg_temp_bar); // Or a red one
            holder.tvRecommendation.setTextColor(0xFFFF5252); // Red
        } else {
            holder.layoutBadge.setBackgroundResource(R.drawable.bg_circle_blue);
            holder.tvRecommendation.setTextColor(0xFF81C784); // Green
        }

        if (plan.getWeatherIcon() != null && !plan.getWeatherIcon().isEmpty()) {
            String iconUrl = "https://openweathermap.org/img/wn/" + plan.getWeatherIcon() + "@2x.png";
            Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.ivWeatherIcon);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(plan.getId()));
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCity, tvTime, tvStatus, tvRecommendation;
        ImageView ivWeatherIcon;
        LinearLayout layoutBadge;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPlanTitle);
            tvCity = itemView.findViewById(R.id.tvPlanCity);
            tvTime = itemView.findViewById(R.id.tvPlanTime);
            tvStatus = itemView.findViewById(R.id.tvWeatherStatus);
            tvRecommendation = itemView.findViewById(R.id.tvRecommendation);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            layoutBadge = itemView.findViewById(R.id.layoutBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
