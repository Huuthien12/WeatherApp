package com.example.myapplicationooo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class FiveDayForecastAdapter extends RecyclerView.Adapter<FiveDayForecastAdapter.ViewHolder> {

    private List<FiveDayItem> forecastList;
    private String unit = "C";

    public FiveDayForecastAdapter(List<FiveDayItem> forecastList) {
        this.forecastList = forecastList;
    }

    public void setUnit(String unit) {
        this.unit = unit;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_five_day_column, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FiveDayItem item = forecastList.get(position);

        holder.tvDayName.setText(item.getDayName());
        holder.tvDate.setText(item.getDate());
        
        holder.tvTempMax.setText(formatTemp(item.getMaxTemp()));
        holder.tvTempMin.setText(formatTemp(item.getMinTemp()));
        holder.tvWindForce.setText("Force " + item.getWindForce());

        String iconUrl = "https://openweathermap.org/img/wn/" + item.getIcon() + ".png";
        Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.imgWeatherIcon);
        Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.imgNightIcon);
    }

    private String formatTemp(double tempC) {
        if ("F".equals(unit)) {
            return String.format(Locale.getDefault(), "%.0f°", (tempC * 9/5) + 32);
        }
        return String.format(Locale.getDefault(), "%.0f°", tempC);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDate, tvTempMax, tvTempMin, tvWindForce;
        ImageView imgWeatherIcon, imgNightIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTempMax = itemView.findViewById(R.id.tvTempMax);
            tvTempMin = itemView.findViewById(R.id.tvTempMin);
            tvWindForce = itemView.findViewById(R.id.tvWindForce);
            imgWeatherIcon = itemView.findViewById(R.id.imgWeatherIcon);
            imgNightIcon = itemView.findViewById(R.id.imgNightIcon);
        }
    }

    public static class FiveDayItem {
        private String dayName;
        private String date;
        private double minTemp;
        private double maxTemp;
        private String icon;
        private int windForce;

        public FiveDayItem(String dayName, String date, double minTemp, double maxTemp, String icon, int windForce) {
            this.dayName = dayName;
            this.date = date;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.icon = icon;
            this.windForce = windForce;
        }

        public String getDayName() { return dayName; }
        public String getDate() { return date; }
        public double getMinTemp() { return minTemp; }
        public void setMinTemp(double minTemp) { this.minTemp = minTemp; }
        public double getMaxTemp() { return maxTemp; }
        public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }
        public String getIcon() { return icon; }
        public int getWindForce() { return windForce; }
    }
}
