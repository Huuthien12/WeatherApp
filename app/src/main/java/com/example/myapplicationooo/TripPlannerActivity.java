package com.example.myapplicationooo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class TripPlannerActivity extends AppCompatActivity {

    private TripPlannerViewModel viewModel;
    private TripSuggestionAdapter adapter;
    private View pbLoading;
    private TextView tvEmptyState;
    private final String[] activities = {
            "Beach trip", "Picnic", "Cafe outdoor", "Running",
            "Hiking", "Camping", "Traveling", "Photography",
            "Outdoor sports", "City walking"
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        viewModel = new ViewModelProvider(this).get(TripPlannerViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView rvTripPlans = findViewById(R.id.rvTripPlans);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        ExtendedFloatingActionButton fabAddTrip = findViewById(R.id.fabAddTrip);

        toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new TripSuggestionAdapter(tripId -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.manage_cities)) // Reusing string or add new
                    .setMessage("Are you sure you want to delete this AI generated plan?")
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deletePlan(tripId))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        rvTripPlans.setLayoutManager(new LinearLayoutManager(this));
        rvTripPlans.setAdapter(adapter);

        fabAddTrip.setOnClickListener(v -> showAddTripDialog());

        viewModel.getPlans().observe(this, plans -> {
            adapter.setTripPlans(plans);
            tvEmptyState.setVisibility(plans.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.fetchPlans(AuthManager.getInstance().getUserId());
    }

    private void showAddTripDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_trip_input, null);
        EditText etDestination = view.findViewById(R.id.etDestination);
        EditText etUserPrompt = view.findViewById(R.id.etUserPrompt);
        AutoCompleteTextView spinnerActivity = view.findViewById(R.id.spinnerActivity);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activities);
        spinnerActivity.setAdapter(arrayAdapter);

        new AlertDialog.Builder(this)
                .setTitle("AI Trip Planner")
                .setView(view)
                .setPositiveButton("Generate", (dialog, which) -> {
                    String dest = etDestination.getText().toString().trim();
                    String prompt = etUserPrompt.getText().toString().trim();
                    String activity = spinnerActivity.getText().toString();

                    if (dest.isEmpty() || activity.isEmpty()) {
                        Toast.makeText(this, "Please fill in Destination and Activity Type", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    viewModel.createNewPlan(AuthManager.getInstance().getUserId(), dest, activity, prompt);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
