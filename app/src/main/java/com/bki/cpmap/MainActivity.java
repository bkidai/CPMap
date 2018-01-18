package com.bki.cpmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bki.cpmap.utils.LocationUtil;
import com.bki.cpmap.utils.StringUtil;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.android.ui.geocoder.GeocoderAutoCompleteView;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, LocationEngineListener {

    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.autocomplete_widget)
    GeocoderAutoCompleteView autoCompleteWidget;

    @BindString(R.string.map_access_token)
    String mapAccessToken;
    @BindString(R.string.enable_gps_message)
    String enableGpsMessage;
    @BindString(R.string.default_map_latitude)
    String defaultLatitude;
    @BindString(R.string.default_map_longitude)
    String defaultLongitude;


    private Context context;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Marker locationMarker;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Mapbox.getInstance(context, mapAccessToken);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Set up autocomplete widget
        autoCompleteWidget.setAccessToken(Mapbox.getAccessToken());
        autoCompleteWidget.setType(GeocodingCriteria.TYPE_POI);
        autoCompleteWidget.setOnFeatureListener(feature -> {
            setPinPosition(LocationUtil.getLocation(feature.asPosition().getLatitude(),
                    feature.asPosition().getLongitude()));
        });
    }

    @Override
    public void onMapReady(MapboxMap map) {
        // store current map
        mapboxMap = map;
        // show user location
        setUserLocationMap();
    }


    /**
     * show user location
     */
    void setUserLocationMap() {
        if ( PermissionsManager.areLocationPermissionsGranted(context) ) setUserLocation();
        else {
            setDefaultLocation();
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions((Activity) context);
        }
    }


    @SuppressWarnings({"MissingPermission"})
    void setUserLocation() {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if ( locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Toast.makeText(context, enableGpsMessage, Toast.LENGTH_LONG).show();
            setDefaultLocation();
        }

        locationEngine = new LostLocationEngine(this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.addLocationEngineListener(this);
        locationEngine.activate();

        locationPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
        locationPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
        getLifecycle().addObserver(locationPlugin);
    }

    /**
     * Set a default location
     */
    void setDefaultLocation() {
        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(StringUtil.parseToDouble(defaultLatitude),
                        StringUtil.parseToDouble(defaultLongitude)))
                .zoom(10)
                .build());
    }

    private void setCameraPosition(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
    }

    private void setPinPosition(Location location) {
        Icon icon = IconFactory.getInstance(context).fromResource(R.drawable.mapbox_marker_icon_default);
        if ( locationMarker == null ) {
            // Add marker
            locationMarker = mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(icon));
        } else {
            // Move existing marker
            locationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        setCameraPosition(location);
    }


    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onConnected() {
        if ( locationEngine != null ) locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if ( locationMarker == null ) setPinPosition(location);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if ( granted ) setUserLocation();
        else Toast.makeText(context, R.string.reject_location_permission, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if ( mapView != null ) mapView.onSaveInstanceState(outState);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        if ( mapView != null ) mapView.onStart();
        if ( locationEngine != null ) {
            locationEngine.requestLocationUpdates();
            locationEngine.addLocationEngineListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( mapView != null ) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if ( mapView != null ) mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( mapView != null ) mapView.onStop();
        if ( locationEngine != null ) locationEngine.removeLocationUpdates();
        if ( locationPlugin != null ) locationPlugin.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( mapView != null ) mapView.onDestroy();
        if ( locationEngine != null ) locationEngine.deactivate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if ( mapView != null ) mapView.onLowMemory();
    }
}
