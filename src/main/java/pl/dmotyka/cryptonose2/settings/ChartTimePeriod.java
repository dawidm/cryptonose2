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

public enum ChartTimePeriod {
    ANY(0),
    M5(300),
    M30(1800);

    public final long periodSec;

    ChartTimePeriod(long periodSec) {
        this.periodSec = periodSec;
    }

    public static ChartTimePeriod getForPeriodSec(long periodSec) {
        if (periodSec == ChartTimePeriod.M5.periodSec)
            return ChartTimePeriod.M5;
        if (periodSec == ChartTimePeriod.M30.periodSec)
            return ChartTimePeriod.M30;
        if (periodSec == ChartTimePeriod.ANY.periodSec)
            return ChartTimePeriod.ANY;
        throw new IllegalArgumentException("Wrong time period");
    }
}
