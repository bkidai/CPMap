package com.bki.cpmap.utils;

import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bki.cpmap.R;

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

    public static View preparePickupPin(Context context) {
        ImageView pickupPin = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                SizeUtil.dp2px(context, 36),
                SizeUtil.dp2px(context, 64),
                Gravity.CENTER);
        pickupPin.setImageResource(R.drawable.pin_picker);
        pickupPin.setScaleType(ImageView.ScaleType.FIT_END);
        pickupPin.setLayoutParams(params);
        return pickupPin;
    }

}
