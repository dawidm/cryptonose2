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

package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import com.dawidmotyka.exchangeutils.tools.TimeConverter;

/**
 *
 * @author dawid
 */
public class PriceAlert {
    private final String pair;
    private final String formattedPair;
    private final double priceChange;
    private final double relativePriceChange;
    private final double minPrice;
    private final double maxPrice;
    private final long changeTimeSeconds;
    private final long periodSeconds;
    private final long timestamp;
    private final ExchangeSpecs exchangeSpecs;

    public PriceAlert(ExchangeSpecs exchangeSpecs, String pair, long periodSeconds, long timestamp, double priceChange, double relativePriceChange, double minPrice, double maxPrice, long changeTimeSeconds) {
        this.exchangeSpecs=exchangeSpecs;
        this.pair = pair;
        this.formattedPair =PairSymbolConverter.toFormattedString(exchangeSpecs,pair);
        this.periodSeconds = periodSeconds;
        this.timestamp = timestamp;
        this.priceChange = priceChange;
        this.relativePriceChange=relativePriceChange;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.changeTimeSeconds = changeTimeSeconds;
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

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    /**
     * @return the relativePriceChange
     */
    public double getRelativePriceChange() {
        return relativePriceChange;
    }

    public double getFinalPrice() {
        return (priceChange>0)?maxPrice:minPrice;
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
}
