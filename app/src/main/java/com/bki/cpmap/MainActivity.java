package com.bki.cpmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.bki.cpmap.domain.Place;
import com.bki.cpmap.utils.LocationUtil;
import com.bki.cpmap.utils.SharedPreferencesUtil;
import com.bki.cpmap.utils.StringUtil;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, LocationEngineListener {

    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.select_location_btn)
    View pickerBtn;
    @BindView(R.id.autocomplete_widget)
    GeocoderAutoCompleteView autoCompleteWidget;
    @BindView(R.id.drawer_list_view)
    ListView drawerListView;


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
    private View pickupPin;

    ArrayAdapter<? extends String> adapter;

    private final String locationDbKey = "StoredLocations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.drawer_view);
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
                    storeLocation(feature);
                }
        );

        // add pickup view
        pickupPin = LocationUtil.preparePickupPin(context);

        // Button for user to drop marker or to pick marker back up.
        pickerBtn.setOnClickListener(view -> handleLocationPicker());

        prepareDrawerLocationsList();
    }

    void storeLocation(CarmenFeature feature) {
        int locationMaxSize = 15;
        List<Object> selectedLocations = SharedPreferencesUtil.getListObject(
                locationDbKey, Place.class, context);
        selectedLocations.add(0, new Place().parse(feature));
        if ( selectedLocations.size() > locationMaxSize )
            selectedLocations.remove(selectedLocations.size() - 1);
        SharedPreferencesUtil.putListObject(locationDbKey, selectedLocations, context);
        /*
        if ( adapter != null ) {
            drawerListView.invalidate();
            adapter.notifyDataSetChanged();
        }
        */
    }

    void prepareDrawerLocationsList() {
        List<Object> selectedLocations = SharedPreferencesUtil.getListObject(
                locationDbKey, Place.class, context);
        String[] listItems = new String[selectedLocations.size()];
        for (int i = 0; i < selectedLocations.size(); i++) {
            Place carmenFeature = (Place) selectedLocations.get(i);
            listItems[i] = carmenFeature.getAddressName();
        }
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, listItems);
        drawerListView.setAdapter(adapter);
    }

    void handleLocationPicker() {
        if ( autoCompleteWidget != null ) autoCompleteWidget.setText("");
        // add the view if the first click on pick location
        if ( pickupPin.getParent() == null ) {
            mapView.addView(pickupPin);
            // remove the pin if existed
            if ( locationMarker != null ) mapboxMap.removeMarker(locationMarker);
            locationMarker = null;
            return;
        }
        // no location picked before
        if ( locationMarker == null ) {
            // We first find where the hovering marker position is relative to the mapboxMap
            float coordinateX = pickupPin.getLeft() + (pickupPin.getWidth() / 2);
            float coordinateY = pickupPin.getBottom();
            final LatLng latLng = mapboxMap.getProjection().fromScreenLocation(
                    new PointF(coordinateX, coordinateY));
            // hide pickup pin
            pickupPin.setVisibility(View.GONE);
            Icon icon = IconFactory.getInstance(context).fromResource(R.drawable.mapbox_marker_icon_default);
            locationMarker = mapboxMap.addMarker(new MarkerOptions().position(latLng).icon(icon));
            // get the geoCoding information
            reverseGeocode(latLng);
        } else {
            mapboxMap.removeMarker(locationMarker);
            locationMarker = null;
            pickupPin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(MapboxMap map) {
        // store current map
        mapboxMap = map;
        // show user location
        setUserLocationMap();
    }

    /**
     * This method is used to reverse geocode where the user has dropped the marker
     */
    private void reverseGeocode(final LatLng point) {
        LocationUtil.getReverseGeocodeClient(point).enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call,
                                   @NonNull Response<GeocodingResponse> response) {
                CarmenFeature feature = null;
                if ( response.isSuccessful() ) {
                    GeocodingResponse geoResponse = response.body();
                    List<CarmenFeature> results = geoResponse != null ? geoResponse.getFeatures() : null;
                    if ( results != null && results.size() > 0 ) feature = results.get(0);
                }
                if ( locationMarker != null ) {
                    if ( feature != null ) {
                        autoCompleteWidget.setText(feature.getPlaceName());
                        autoCompleteWidget.clearFocus();
                        storeLocation(feature);
                    } else autoCompleteWidget.setText("");
                    mapboxMap.selectMarker(locationMarker);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
            }
        });
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

    /**
     * Set user location
     */
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
        setCameraPosition(LocationUtil.getLocation(
                StringUtil.parseToDouble(defaultLatitude),
                StringUtil.parseToDouble(defaultLongitude)), 10);
    }

    private void setCameraPosition(Location location, int zoom) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                location.getLatitude(),
                location.getLongitude()), zoom));
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

        hideKeyboard();
        setCameraPosition(location, 12);
    }

    private void hideKeyboard() {
        if ( getCurrentFocus() != null ) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if ( imm != null ) imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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
