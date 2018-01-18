package com.bki.cpmap.utils;

import android.location.Location;

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

}
