/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2.settings;

import pl.dmotyka.cryptonose2.dataobj.PriceAlertThresholds;

public class ExampleAlertThresholds {

    public enum ThresholdValuesType {
        LOW, MEDIUM, HIGH
    }

    public static PriceAlertThresholds getThresholds(ThresholdValuesType type, CryptonoseSettings.TimePeriod period) {
        switch (period) {
            case M5 -> {
                switch (type) {
                    case LOW -> {
                        return new PriceAlertThresholds(
                                3,
                                3,
                                3,
                                3,
                                6,
                                6);
                    }
                    case MEDIUM -> {
                        return new PriceAlertThresholds(
                                4,
                                4,
                                3.5,
                                3.5,
                                8,
                                8);
                    }
                    case HIGH -> {
                        return new PriceAlertThresholds(
                                5,
                                5,
                                4,
                                4,
                                10,
                                10);
                    }
                }
            }
            case M30 -> {
                switch (type) {
                    case LOW -> {
                        return new PriceAlertThresholds(
                                6,
                                6,
                                3,
                                3,
                                6,
                                6);
                    }
                    case MEDIUM -> {
                        return new PriceAlertThresholds(
                                8,
                                8,
                                3.5,
                                3.5,
                                8,
                                8);
                    }
                    case HIGH -> {
                        return new PriceAlertThresholds(
                                10,
                                10,
                                4,
                                4,
                                10,
                                10);
                    }
                }
            }
        }
        throw new IllegalArgumentException("wrong type/period");
    }

}
