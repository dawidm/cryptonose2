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

/**
 *
 * @author dawid
 */
public class PairData {
    
    private String pairName;
    
    private int[] priceChangePeriods; //in seconds
    private Double[] priceChanges;
    private Double[] totalVolumes;
    private double[] relativePriceChanges;
    private double[] alertChangesRising;
    private double[] alertChangesDropping;
    private double[] averageHighLowDiffs;
    private double lastPrice;
    private double volume; //24h
    private String fullName;

    public PairData(String pairName, double lastPrice, double volume) {
        this.pairName=pairName;
        this.lastPrice=lastPrice;
        this.volume=volume;
    }
    
    /**
     * @return the priceChangePeriods
     */
    public int[] getPriceChangePeriods() {
        return priceChangePeriods;
    }

    /**
     * @param priceChangePeriods the priceChangePeriods to set
     */
    public void setPriceChangePeriods(int[] priceChangePeriods) {
        this.priceChangePeriods = priceChangePeriods;
    }

    /**
     * @return the priceChanges
     */
    public Double[] getPriceChanges() {
        return priceChanges;
    }

    /**
     * @param priceChanges the priceChanges to set
     */
    public void setPriceChanges(Double[] priceChanges) {
        this.priceChanges = priceChanges;
    }

    /**
     * @return the alertChangesRising
     */
    public double[] getAlertChangesRising() {
        return alertChangesRising;
    }

    /**
     * @param alertChangesRising the alertChangesRising to set
     */
    public void setAlertChangesRising(double[] alertChangesRising) {
        this.alertChangesRising = alertChangesRising;
    }

    /**
     * @return the alertChangesDropping
     */
    public double[] getAlertChangesDropping() {
        return alertChangesDropping;
    }

    /**
     * @param alertChangesDropping the alertChangesDropping to set
     */
    public void setAlertChangesDropping(double[] alertChangesDropping) {
        this.alertChangesDropping = alertChangesDropping;
    }

    /**
     * @return the averageHighLowDiffs
     */
    public double[] getAverageHighLowDiffs() {
        return averageHighLowDiffs;
    }

    /**
     * @param averageHighLowDiffs the averageHighLowDiffs to set
     */
    public void setAverageHighLowDiffs(double[] averageHighLowDiffs) {
        this.averageHighLowDiffs = averageHighLowDiffs;
    }

    /**
     * @return the lastPrice
     */
    public double getLastPrice() {
        return lastPrice;
    }

    /**
     * @param lastPrice the lastPrice to set
     */
    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    /**
     * @return the volume
     */
    public double getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(double volume) {
        this.volume = volume;
    }

    /**
     * @return the pairName
     */
    public String getPairName() {
        return pairName;
    }

    /**
     * @param pairName the pairName to set
     */
    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    /**
     * @return the relativePriceChanges
     */
    public double[] getRelativePriceChanges() {
        return relativePriceChanges;
    }

    /**
     * @param relativePriceChanges the relativePriceChanges to set
     */
    public void setRelativePriceChanges(double[] relativePriceChanges) {
        this.relativePriceChanges = relativePriceChanges;
    }

    /**
     * @return the buyToSellVolume
     */
    public Double[] getTotalVolumes() {
        return totalVolumes;
    }

    /**
     * @param totalVolumes the buyToSellVolume to set
     */
    public void setTotalVolumes(Double[] totalVolumes) {
        this.totalVolumes = totalVolumes;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
