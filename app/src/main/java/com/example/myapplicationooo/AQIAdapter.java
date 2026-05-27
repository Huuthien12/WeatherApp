package com.example.myapplicationooo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AQIAdapter extends RecyclerView.Adapter<AQIAdapter.ViewHolder> {

    private List<AQIDetail> details = new ArrayList<>();

    public void setDetails(List<AQIDetail> details) {
        this.details = details;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aqi_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AQIDetail detail = details.get(position);
        holder.tvName.setText(detail.getName());
        holder.tvValue.setText(detail.getValue());
        holder.tvUnit.setText(detail.getUnit());
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvValue, tvUnit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPollutantName);
            tvValue = itemView.findViewById(R.id.tvPollutantValue);
            tvUnit = itemView.findViewById(R.id.tvPollutantUnit);
        }
    }
}
