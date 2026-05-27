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
import java.util.TimeZone;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private List<ForecastItem> forecastList;
    private String unit;
    private int timezoneOffset;

    public ForecastAdapter(List<ForecastItem> forecastList, String unit, int timezoneOffset) {
        this.forecastList = forecastList;
        this.unit = unit;
        this.timezoneOffset = timezoneOffset;
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

        // Định dạng giờ theo timezone của thành phố
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + (timezoneOffset >= 0 ? "+" : "") + (timezoneOffset / 3600)));
        holder.tvTime.setText(sdf.format(new Date(item.getDt() * 1000)));

        double tempC = item.getMain().getTemp();
        if ("F".equals(unit)) {
            double tempF = (tempC * 9/5) + 32;
            holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", tempF));
        } else {
            holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", tempC));
        }

        // Sử dụng icon local cho rõ nét hơn
        int iconRes = WeatherIconHelper.getWeatherIcon(item.getWeather().get(0).getIcon());
        Glide.with(holder.itemView.getContext()).load(iconRes).into(holder.imgIcon);
    }

    @Override
    public int getItemCount() {
        return forecastList != null ? Math.min(forecastList.size(), 8) : 0;
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
