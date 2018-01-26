package com.bki.cpmap.utils;


import android.content.Context;
import android.util.DisplayMetrics;

public class SizeUtil {
    /**
     * dp to px
     *
     * @param dpValue dp
     * @return px
     */
    public static int dp2px(Context context, int dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dpValue * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * px to dp
     *
     * @param pxValue px
     * @return dp
     */
    public static int px2dp(Context context, final int pxValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return pxValue / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
