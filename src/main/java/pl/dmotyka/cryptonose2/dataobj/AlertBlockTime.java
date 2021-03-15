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

package pl.dmotyka.cryptonose2.dataobj;

public enum AlertBlockTime {

    TIME_1H(3600, "1 hour"),
    TIME_4H(3600 * 4, "4 hours"),
    TIME_12H(3600 * 12, "12 hours"),
    TIME_DAY(3600 * 24, "1 day"),
    BLOCK_PERMANENTLY(Long.MAX_VALUE, "permanent"),
    UNBLOCK(0, "unblock");

    private final long timeSeconds;
    private final String label;

    AlertBlockTime(long timeSeconds, String label) {
        this.timeSeconds = timeSeconds;
        this.label = label;
    }

    public long getTimeSeconds() {
        return timeSeconds;
    }

    public String getLabel() {
        return label;
    }
}
