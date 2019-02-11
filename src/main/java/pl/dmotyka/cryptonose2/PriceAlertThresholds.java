/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2;

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
