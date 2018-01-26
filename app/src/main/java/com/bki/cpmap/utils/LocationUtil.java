package com.bki.cpmap.utils;

import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bki.cpmap.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.commons.models.Position;

/**
 * Class provide some location utils
 */
public abstract class LocationUtil {

    /**
     * Create a {@link Location} from lat & long
     *
     * @param latitude  input lat
     * @param longitude input long
     * @return a {@link Location}
     */
    public static Location getLocation(double latitude, double longitude) {
        Location location = new Location((String) null);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    /**
     * Prepare pickup marker on the center of the screen
     *
     * @param context activity context
     * @return view
     */
    public static View preparePickupPin(Context context) {
        ImageView pickupPin = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                SizeUtil.dp2px(context, 42),
                SizeUtil.dp2px(context, 72),
                Gravity.CENTER);
        pickupPin.setImageResource(R.drawable.ic_pin_picker);
        pickupPin.setScaleType(ImageView.ScaleType.FIT_END);
        pickupPin.setLayoutParams(params);
        return pickupPin;
    }

    public static MapboxGeocoding getReverseGeocodeClient(final LatLng point) {
        return new MapboxGeocoding.Builder()
                .setAccessToken(Mapbox.getAccessToken())
                .setCoordinates(Position.fromCoordinates(point.getLongitude(), point.getLatitude()))
                .setGeocodingType(GeocodingCriteria.TYPE_ADDRESS)
                .build();
    }

}
