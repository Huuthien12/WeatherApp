package com.example.myapplicationooo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryChipAdapter extends RecyclerView.Adapter<HistoryChipAdapter.ViewHolder> {

    private List<String> items;
    private OnChipClickListener listener;

    public interface OnChipClickListener {
        void onChipClick(String cityName);
    }

    public HistoryChipAdapter(List<String> items, OnChipClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = items.get(position);
        holder.tvName.setText(name);
        holder.itemView.setOnClickListener(v -> listener.onChipClick(name));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChipName);
        }
    }
}
