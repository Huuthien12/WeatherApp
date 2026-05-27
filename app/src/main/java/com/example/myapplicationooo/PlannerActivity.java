package com.example.myapplicationooo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;

import java.util.Calendar;

public class PlannerActivity extends AppCompatActivity {

    private PlannerViewModel viewModel;
    private PlannerAdapter adapter;
    private View loadingBar;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        viewModel = new ViewModelProvider(this).get(PlannerViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView rvPlanner = findViewById(R.id.rvPlanner);
        loadingBar = findViewById(R.id.loadingBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        FloatingActionButton fabAddPlan = findViewById(R.id.fabAddPlan);

        toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new PlannerAdapter(planId -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Plan")
                    .setMessage("Are you sure you want to delete this plan?")
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deletePlan(planId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        rvPlanner.setLayoutManager(new LinearLayoutManager(this));
        rvPlanner.setAdapter(adapter);

        fabAddPlan.setOnClickListener(v -> showAddPlanDialog());

        viewModel.getPlans().observe(this, plans -> {
            adapter.setPlans(plans);
            tvEmptyState.setVisibility(plans.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            loadingBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.fetchPlans();
    }

    private void showAddPlanDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_plan, null);
        EditText etTitle = view.findViewById(R.id.etPlanTitle);
        EditText etCity = view.findViewById(R.id.etPlanCity);

        new AlertDialog.Builder(this)
                .setTitle("Create New Plan")
                .setView(view)
                .setPositiveButton("Set Time", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String city = etCity.getText().toString().trim();

                    if (title.isEmpty() || city.isEmpty()) {
                        Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showDateTimePicker(title, city);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDateTimePicker(String title, String city) {
        Calendar calendar = Calendar.getInstance();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            calendar.setTimeInMillis(selection);

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());

                viewModel.addPlan(title, city, new Timestamp(calendar.getTime()));
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}
