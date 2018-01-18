package com.bki.cpmap.utils;

/**
 * Class provide some String utils
 */
public abstract class StringUtil {

    /**
     * Parse string to double with exceptions handling
     *
     * @param value string
     * @return double value from string (return 0d if error)
     */
    public static double parseToDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0d;
        }
    }

}
