package com.example.myapplicationooo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class FeatureActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvFeatures = findViewById(R.id.rvFeatures);
        rvFeatures.setLayoutManager(new LinearLayoutManager(this));

        List<FeatureModel> features = new ArrayList<>();
        // Guest Features
        features.add(new FeatureModel(getString(R.string.twenty_four_hour_forecast), "Real-time updates and detailed forecast.", android.R.drawable.ic_menu_day, false));
        features.add(new FeatureModel("Weather Radar", "Interactive rain and wind radar maps.", android.R.drawable.ic_dialog_map, false));
        features.add(new FeatureModel("Marine Weather", "Wave height and sea temperature data.", android.R.drawable.ic_menu_compass, false));
        
        // Premium Features
        // Fixed: Replaced non-existent ic_menu_chat_bubble with ic_menu_send
        features.add(new FeatureModel("AI Health Assistant", "Personalized health advice based on weather.", android.R.drawable.ic_menu_send, true));
        features.add(new FeatureModel("Weather Planner", "Plan your activities with smart scheduling.", android.R.drawable.ic_menu_today, true));
        features.add(new FeatureModel("Smart Notifications", "Get alerts for rain, UV, and more.", android.R.drawable.ic_popup_reminder, true));
        features.add(new FeatureModel("Cloud Sync", "Keep your favorite cities synced on all devices.", android.R.drawable.stat_notify_sync, true));

        FeatureAdapter adapter = new FeatureAdapter(features);
        rvFeatures.setAdapter(adapter);
    }

    private static class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {
        private final List<FeatureModel> list;

        FeatureAdapter(List<FeatureModel> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guest_feature, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FeatureModel item = list.get(position);
            holder.title.setText(item.getTitle());
            holder.desc.setText(item.getDescription());
            holder.icon.setImageResource(item.getIconRes());
            holder.badge.setVisibility(item.isPremium() ? View.VISIBLE : View.GONE);
            
            holder.itemView.setOnClickListener(v -> {
                if (item.isPremium()) {
                    LoginRequiredHelper.checkAndProceed(v.getContext(), () -> {
                        // Already logged in logic
                    });
                }
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc, badge;
            ImageView icon;
            ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.tvFeatureTitle);
                desc = v.findViewById(R.id.tvFeatureDesc);
                icon = v.findViewById(R.id.ivFeatureIcon);
                badge = v.findViewById(R.id.tvPremiumBadge);
            }
        }
    }
}
