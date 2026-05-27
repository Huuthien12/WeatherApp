package com.example.myapplicationooo;

import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.RasterLayer;
import org.maplibre.android.style.sources.RasterSource;
import org.maplibre.android.style.sources.TileSet;

public class MarineMapLayerManager {
    private final String OWM_API_KEY = "22b3362b92ebbde69c2e8145c14d2da2";
    private static final String LAYER_ID = "weather-layer";
    private static final String SOURCE_ID = "weather-source";

    public void applyLayer(Style style, String layerType) {
        if (style == null) return;

        // Clean up previous layers
        if (style.getLayer(LAYER_ID) != null) style.removeLayer(LAYER_ID);
        if (style.getSource(SOURCE_ID) != null) style.removeSource(SOURCE_ID);

        // Define Tile URL (OpenWeatherMap)
        String url = "https://tile.openweathermap.org/map/" + layerType + "/{z}/{x}/{y}.png?appid=" + OWM_API_KEY;

        TileSet tileSet = new TileSet("2.1.0", url);
        RasterSource source = new RasterSource(SOURCE_ID, tileSet, 256);
        style.addSource(source);
        style.addLayer(new RasterLayer(LAYER_ID, SOURCE_ID));
    }
}
