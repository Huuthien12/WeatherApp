package com.example.myapplicationooo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddedCitiesAdapter extends RecyclerView.Adapter<AddedCitiesAdapter.ViewHolder> {

    private List<AddedCity> cityList;
    private OnCityClickListener listener;
    private boolean isDeleteMode = false;

    public interface OnCityClickListener {
        void onCityClick(String cityName);
        void onLongClick();
    }

    public AddedCitiesAdapter(List<AddedCity> cityList, OnCityClickListener listener) {
        this.cityList = cityList;
        this.listener = listener;
    }

    public void setDeleteMode(boolean deleteMode) {
        isDeleteMode = deleteMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_added_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddedCity city = cityList.get(position);
        holder.tvCityName.setText(city.getName());
        holder.tvTemp.setText(String.format("%.0f°", city.getTemp()));
        holder.tvDesc.setText(city.getDescription());
        holder.tvMinMax.setText(String.format("%.0f° / %.0f°", city.getMaxTemp(), city.getMinTemp()));

        // Hiện icon định vị nếu là vị trí hiện tại
        holder.imgLocation.setVisibility(city.isCurrentLocation() ? View.VISIBLE : View.GONE);

        // Chế độ xóa
        if (isDeleteMode && !city.isCurrentLocation()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(city.isSelected());
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> city.setSelected(isChecked));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isDeleteMode && !city.isCurrentLocation()) {
                city.setSelected(!city.isSelected());
                notifyItemChanged(position);
            } else {
                listener.onCityClick(city.getName());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName, tvTemp, tvDesc, tvMinMax;
        ImageView imgLocation;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvMinMax = itemView.findViewById(R.id.tvMinMax);
            imgLocation = itemView.findViewById(R.id.imgLocationIcon);
            checkBox = itemView.findViewById(R.id.checkBoxDelete);
        }
    }
}
