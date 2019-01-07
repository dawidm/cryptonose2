package com.dawidmotyka.cryptonose2;

import java.util.prefs.Preferences;

/**
 * Created by dawid on 7/25/17.
 */
public class PriceAlertThresholds {

    private static final Double DEFAULT_REQUIRED_RISING_VALUE = 3.0;
    private static final Double DEFAULT_REQUIRED_FALLING_VALUE = 3.0;
    private static final Double DEFAULT_REQUIRED_RELATIVE_RISING_VALUE = 4.0;
    private static final Double DEFAULT_REQUIRED_RELATIVE_FALLING_VALUE = 4.0;
    private static final Double DEFAULT_SUFFICIENT_RELATIVE_RISING_VALUE = 8.0;
    private static final Double DEFAULT_SUFFICIENT_RELATIVE_FALLING_VALUE = 8.0;

    private double requiredRisingValue;
    private double requiredFallingValue;
    private double requiredRelativeRisingValue;
    private double requiredRelativeFallingValue;
    private double sufficientRelativeRisingValue;
    private double sufficientRelativeFallingValue;

    public PriceAlertThresholds(double requiredRisingValue, double requiredFallingValue, double requiredRelativeRisingValue, double requiredRelativeFallingValue, double sufficientRelativeRisingValue, double sufficientRelativeFallingValue) {
        this.requiredRisingValue = requiredRisingValue;
        this.requiredFallingValue = requiredFallingValue;
        this.requiredRelativeRisingValue = requiredRelativeRisingValue;
        this.requiredRelativeFallingValue = requiredRelativeFallingValue;
        this.sufficientRelativeRisingValue = sufficientRelativeRisingValue;
        this.sufficientRelativeFallingValue = sufficientRelativeFallingValue;
    }

    public double getRequiredRisingValue() {
        return requiredRisingValue;
    }

    public double getRequiredFallingValue() {
        return requiredFallingValue;
    }

    public double getRequiredRelativeRisingValue() {
        return requiredRelativeRisingValue;
    }

    public double getRequiredRelativeFallingValue() {
        return requiredRelativeFallingValue;
    }

    public double getSufficientRelativeRisingValue() {
        return sufficientRelativeRisingValue;
    }

    public double getSufficientRelativeFallingValue() {
        return sufficientRelativeFallingValue;
    }

    public void setRequiredRisingValue(double requiredRisingValue) {
        this.requiredRisingValue = requiredRisingValue;
    }

    public void setRequiredFallingValue(double requiredFallingValue) {
        this.requiredFallingValue = requiredFallingValue;
    }

    public void setRequiredRelativeRisingValue(double requiredRelativeRisingValue) {
        this.requiredRelativeRisingValue = requiredRelativeRisingValue;
    }

    public void setRequiredRelativeFallingValue(double requiredRelativeFallingValue) {
        this.requiredRelativeFallingValue = requiredRelativeFallingValue;
    }

    public void setSufficientRelativeRisingValue(double sufficientRelativeRisingValue) {
        this.sufficientRelativeRisingValue = sufficientRelativeRisingValue;
    }

    public void setSufficientRelativeFallingValue(double sufficientRelativeFallingValue) {
        this.sufficientRelativeFallingValue = sufficientRelativeFallingValue;
    }

    public static PriceAlertThresholds fromPreferences(Preferences preferences, String suffix) {
        return new PriceAlertThresholds(
                preferences.getDouble("requiredRisingValue"+suffix, DEFAULT_REQUIRED_RISING_VALUE),
                preferences.getDouble("requiredFallingValue"+suffix, DEFAULT_REQUIRED_FALLING_VALUE),
                preferences.getDouble("requiredRelativeRisingValue"+suffix, DEFAULT_REQUIRED_RELATIVE_RISING_VALUE),
                preferences.getDouble("requiredRelativeFallingValue"+suffix, DEFAULT_REQUIRED_RELATIVE_FALLING_VALUE),
                preferences.getDouble("sufficientRelativeRisingValue"+suffix, DEFAULT_SUFFICIENT_RELATIVE_RISING_VALUE),
                preferences.getDouble("sufficientRelativeFallingValue"+suffix, DEFAULT_SUFFICIENT_RELATIVE_FALLING_VALUE)
        );
    }

    public void toPreferences(Preferences preferences, String suffix) {
        preferences.putDouble("requiredRisingValue"+suffix, requiredRisingValue);
        preferences.putDouble("requiredFallingValue"+suffix, requiredFallingValue);
        preferences.putDouble("requiredRelativeRisingValue"+suffix, requiredRelativeRisingValue);
        preferences.putDouble("requiredRelativeFallingValue"+suffix, requiredRelativeFallingValue);
        preferences.putDouble("sufficientRelativeRisingValue"+suffix, sufficientRelativeRisingValue);
        preferences.putDouble("sufficientRelativeFallingValue"+suffix, sufficientRelativeFallingValue);
    }

}
