package com.example.myapplicationooo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView rvCurrentLocation, rvAddedCities, rvHistory, rvPopular;
    private AddedCitiesAdapter currentAdapter, addedAdapter;
    private HistoryChipAdapter historyAdapter, popularAdapter;
    private List<AddedCity> currentLocList = new ArrayList<>();
    private List<AddedCity> addedCityList = new ArrayList<>();
    private List<String> historyList = new ArrayList<>();
    private List<String> popularList = new ArrayList<>();

    private final String API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isDeleteMode = false;
    private ImageButton btnDeleteMode;
    private LinearLayout layoutHistory;
    private View scrollViewMain;
    private TextView tvCancel;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SearchView searchView = findViewById(R.id.searchView);
        rvCurrentLocation = findViewById(R.id.rvCurrentLocation);
        rvAddedCities = findViewById(R.id.rvAddedCities);
        rvHistory = findViewById(R.id.rvHistory);
        rvPopular = findViewById(R.id.rvPopular);
        layoutHistory = findViewById(R.id.layoutHistory);
        scrollViewMain = findViewById(R.id.scrollViewMain);
        tvCancel = findViewById(R.id.tvCancel);
        btnDeleteMode = findViewById(R.id.btnDeleteMode);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnClearHistory = findViewById(R.id.btnClearHistory);

        btnBack.setOnClickListener(v -> finish());
        tvCancel.setOnClickListener(v -> closeSearchMode(searchView));
        btnClearHistory.setOnClickListener(v -> clearSearchHistory());

        setupPopularLocations();
        setupAdapters();

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) openSearchMode();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    String cityName = query.trim();
                    
                    if (AuthManager.getInstance().isGuest()) {
                        // NẾU LÀ GUEST: Cho phép xem kết quả nhưng không lưu
                        returnSelectedCity(cityName);
                    } else {
                        // NẾU ĐÃ LOGIN: Lưu và đồng bộ
                        addNewCityToFirestore(cityName);
                        addToSearchHistory(cityName);
                        closeSearchMode(searchView);
                        returnSelectedCity(cityName);
                    }
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        btnDeleteMode.setOnClickListener(v -> {
            if (isDeleteMode) showDeleteConfirmation();
            else toggleDeleteMode();
        });

        loadAddedCities();
        loadSearchHistory();
        getCurrentLocation();
    }

    private void showDeleteConfirmation() {
        if (AuthManager.getInstance().isGuest()) return;
        List<String> citiesToDelete = new ArrayList<>();
        for (AddedCity city : addedCityList) if (city.isSelected()) citiesToDelete.add(city.getName());
        if (citiesToDelete.isEmpty()) { toggleDeleteMode(); return; }

        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_cities)
                .setMessage("Delete selected cities?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSelectedCities(citiesToDelete))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupPopularLocations() {
        popularList.add("Hanoi"); popularList.add("Saigon"); popularList.add("Da Nang");
        popularList.add("Tokyo"); popularList.add("Paris"); popularList.add("London");
        popularList.add("New York");
    }

    private void setupAdapters() {
        AddedCitiesAdapter.OnCityClickListener standardListener = new AddedCitiesAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(String cityName) { returnSelectedCity(cityName); }
            @Override
            public void onLongClick() { 
                if (!AuthManager.getInstance().isGuest()) toggleDeleteMode(); 
                else LoginRequiredHelper.checkAndProceed(SearchActivity.this, () -> {});
            }
        };

        currentAdapter = new AddedCitiesAdapter(currentLocList, standardListener);
        addedAdapter = new AddedCitiesAdapter(addedCityList, standardListener);

        HistoryChipAdapter.OnChipClickListener chipListener = this::returnSelectedCity;

        historyAdapter = new HistoryChipAdapter(historyList, chipListener);
        popularAdapter = new HistoryChipAdapter(popularList, chipListener);

        rvCurrentLocation.setLayoutManager(new LinearLayoutManager(this));
        rvCurrentLocation.setAdapter(currentAdapter);
        rvAddedCities.setLayoutManager(new LinearLayoutManager(this));
        rvAddedCities.setAdapter(addedAdapter);
        rvHistory.setLayoutManager(new GridLayoutManager(this, 3));
        rvHistory.setAdapter(historyAdapter);
        rvPopular.setLayoutManager(new GridLayoutManager(this, 3));
        rvPopular.setAdapter(popularAdapter);
    }

    private void openSearchMode() {
        layoutHistory.setVisibility(View.VISIBLE);
        scrollViewMain.setVisibility(View.GONE);
        tvCancel.setVisibility(View.VISIBLE);
    }

    private void closeSearchMode(SearchView searchView) {
        layoutHistory.setVisibility(View.GONE);
        scrollViewMain.setVisibility(View.VISIBLE);
        tvCancel.setVisibility(View.GONE);
        searchView.clearFocus();
    }

    private void addToSearchHistory(String cityName) {
        if (AuthManager.getInstance().isGuest()) return;
        String userId = AuthManager.getInstance().getUserId();
        if (userId == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("search_history")
                .whereEqualTo("city", cityName).get().addOnSuccessListener(qs -> {
                    for (DocumentSnapshot doc : qs) doc.getReference().delete();
                    Map<String, Object> h = new HashMap<>();
                    h.put("city", cityName); h.put("time", FieldValue.serverTimestamp());
                    db.collection("users").document(userId).collection("search_history").add(h)
                            .addOnSuccessListener(dr -> loadSearchHistory());
                });
    }

    private void loadSearchHistory() {
        if (AuthManager.getInstance().isGuest()) return;
        String userId = AuthManager.getInstance().getUserId();
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("search_history")
                .orderBy("time", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(qs -> {
                    historyList.clear();
                    for (DocumentSnapshot doc : qs) historyList.add(doc.getString("city"));
                    historyAdapter.notifyDataSetChanged();
                });
    }

    private void clearSearchHistory() {
        if (AuthManager.getInstance().isGuest()) return;
        String userId = AuthManager.getInstance().getUserId();
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("search_history")
                .get().addOnSuccessListener(qs -> {
                    for (DocumentSnapshot doc : qs) doc.getReference().delete();
                    historyList.clear(); historyAdapter.notifyDataSetChanged();
                });
    }

    private void returnSelectedCity(String cityName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_city", cityName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void toggleDeleteMode() {
        if (AuthManager.getInstance().isGuest()) return;
        isDeleteMode = !isDeleteMode;
        addedAdapter.setDeleteMode(isDeleteMode);
        btnDeleteMode.setImageResource(isDeleteMode ? android.R.drawable.ic_menu_save : android.R.drawable.ic_menu_delete);
    }

    private void deleteSelectedCities(List<String> citiesToDelete) {
        String userId = AuthManager.getInstance().getUserId();
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("favoriteCities", FieldValue.arrayRemove(citiesToDelete.toArray()))
                    .addOnSuccessListener(aVoid -> { toggleDeleteMode(); loadAddedCities(); });
        }
    }

    private void loadAddedCities() {

        if (AuthManager.getInstance().isGuest()) {

            addedCityList.clear();
            addedAdapter.notifyDataSetChanged();
            return;
        }

        String userId = AuthManager.getInstance().getUserId();

        if (userId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(ds -> {

                    addedCityList.clear();

                    if (ds.exists()) {

                        Object data = ds.get("favoriteCities");

                        List<String> cities = new ArrayList<>();

                        if (data instanceof List<?>) {

                            for (Object item : (List<?>) data) {

                                if (item instanceof String) {

                                    cities.add((String) item);
                                }
                            }
                        }

                        for (String name : cities) {

                            AddedCity city = new AddedCity(name);

                            addedCityList.add(city);

                            fetchWeatherData(city, addedAdapter);
                        }
                    }

                    addedAdapter.notifyDataSetChanged();
                });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String cityName = address.getLocality();
                        if (cityName == null) cityName = address.getSubAdminArea();
                        if (cityName == null) cityName = address.getAdminArea();
                        AddedCity city = new AddedCity(cityName);
                        city.setCurrentLocation(true);
                        currentLocList.clear(); currentLocList.add(city);
                        fetchWeatherData(city, currentAdapter);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void fetchWeatherData(AddedCity city, AddedCitiesAdapter adapterToNotify) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + city.getName() + "&units=metric&appid=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    WeatherResponse data = new Gson().fromJson(new InputStreamReader(conn.getInputStream()), WeatherResponse.class);
                    runOnUiThread(() -> {
                        city.setTemp(data.getMain().getTemp());
                        city.setDescription(data.getWeather().get(0).getMain());
                        city.setMinTemp(data.getMain().getTempMin());
                        city.setMaxTemp(data.getMain().getTempMax());
                        adapterToNotify.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void addNewCityToFirestore(String cityName) {
        String userId = AuthManager.getInstance().getUserId();
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("favoriteCities", FieldValue.arrayUnion(cityName))
                    .addOnSuccessListener(aVoid -> loadAddedCities());
        }
    }
}
