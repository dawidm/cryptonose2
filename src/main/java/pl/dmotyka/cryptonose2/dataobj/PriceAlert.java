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

import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.tools.TimeConverter;

/**
 *
 * @author dawid
 */
public class PriceAlert {
    private final String pair;
    private final String formattedPair;
    private final double priceChange;
    private final double relativePriceChange;
    private final double referencePrice;
    private final double finalPrice;
    private final long changeTimeSeconds;
    private final long periodSeconds;
    private final long timestamp;
    private final long finalPriceTimestamp;
    private final long referencePriceTimestamp;
    private final ExchangeSpecs exchangeSpecs;

    public PriceAlert(ExchangeSpecs exchangeSpecs, String pair, String formattedPair, long periodSeconds, long timestamp, double priceChange, double relativePriceChange, double referencePrice, double finalPrice, long changeTimeSeconds, long finalPriceTimestamp, long referencePriceTimestamp) {
        this.exchangeSpecs=exchangeSpecs;
        this.pair = pair;
        this.formattedPair = formattedPair;
        this.periodSeconds = periodSeconds;
        this.timestamp = timestamp;
        this.priceChange = priceChange;
        this.relativePriceChange=relativePriceChange;
        this.referencePrice = referencePrice;
        this.finalPrice = finalPrice;
        this.changeTimeSeconds = changeTimeSeconds;
        this.finalPriceTimestamp = finalPriceTimestamp;
        this.referencePriceTimestamp = referencePriceTimestamp;
    }

    /**
     * @return the pair
     */
    public String getPair() {
        return pair;
    }

    /**
     * @return the periodSeconds
     */
    public long getPeriodSeconds() {
        return periodSeconds;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the priceChange
     */
    public double getPriceChange() {
        return priceChange;
    }

    public double getReferencePrice() {
        return referencePrice;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    /**
     * @return the relativePriceChange
     */
    public double getRelativePriceChange() {
        return relativePriceChange;
    }

    public ExchangeSpecs getExchangeSpecs() {
        return exchangeSpecs;
    }

    public String getFormattedTimePeriod() {
        return TimeConverter.secondsToFullMinutesHoursDays((int)periodSeconds);
    }

    public String getFormattedPair() {
        return formattedPair;
    }

    public long getChangeTimeSeconds() {
        return changeTimeSeconds;
    }

    public long getFinalPriceTimestamp() {
        return finalPriceTimestamp;
    }

    public long getReferencePriceTimestamp() {
        return referencePriceTimestamp;
    }
}
