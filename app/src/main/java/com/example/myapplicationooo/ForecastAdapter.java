package com.example.myapplicationooo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private List<ForecastItem> forecastList;
    private String unit;

    public ForecastAdapter(List<ForecastItem> forecastList, String unit) {
        this.forecastList = forecastList;
        this.unit = unit;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastItem item = forecastList.get(position);

        // Định dạng giờ từ Timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(item.getDt() * 1000)));

        double tempC = item.getMain().getTemp();
        if ("F".equals(unit)) {
            double tempF = (tempC * 9/5) + 32;
            holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", tempF));
        } else {
            holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", tempC));
        }

        String iconUrl = "https://openweathermap.org/img/wn/" + item.getWeather().get(0).getIcon() + ".png";
        Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.imgIcon);
    }

    @Override
    public int getItemCount() {
        return forecastList != null ? Math.min(forecastList.size(), 8) : 0; // Lấy 8 mục cho 24h
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp;
        ImageView imgIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemp = itemView.findViewById(R.id.tvForecastTemp);
            imgIcon = itemView.findViewById(R.id.imgForecastIcon);
        }
    }
}
