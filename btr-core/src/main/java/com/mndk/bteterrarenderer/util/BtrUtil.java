package com.mndk.bteterrarenderer.util;

public class BtrUtil {

    public static int notNegative(int value, String name) {
        checkArgument(value >= 0, name + " must not be negative");
        return value;
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static <T> T validateNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static boolean validateDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object object) {
        return (T) object;
    }
}