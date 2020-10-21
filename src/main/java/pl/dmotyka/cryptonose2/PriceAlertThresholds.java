/*
 * Cryptonose2
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2;

/**
 * Created by dawid on 7/25/17.
 */
public class PriceAlertThresholds {

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

}
