/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.dmutils.TimeConverter;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

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
        return TimeConverter.secondsToMinutesHoursDays((int)periodSeconds);
    }

    public String getFormattedPair() {
        return formattedPair;
    }

    public long getChangeTimeSeconds() {
        return changeTimeSeconds;
    }
}
