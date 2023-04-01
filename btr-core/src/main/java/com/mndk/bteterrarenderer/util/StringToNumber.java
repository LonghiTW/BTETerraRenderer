package com.mndk.bteterrarenderer.util;

public class StringToNumber {

	public static boolean validate(String s) {
		try {
			Double.parseDouble(s);
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static String formatNicely(double value, int maximumDecimalDigit) {
		return value == (long) value ?
				String.format("%d", (long) value) :
				String.format("%." + maximumDecimalDigit + "f", value);
	}

	public static String formatNicely(double value) {
		return value == (long) value ? String.format("%d", (long) value) : String.format("%f", value);
	}
}