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

package pl.dmotyka.cryptonose2.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

import pl.dmotyka.cryptonoseengine.PriceChanges;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

/**
 * Created by dawid on 8/9/17.
 */
public class CryptonosePairData {

    public static final int PERIOD1 = 1;
    public static final int PERIOD2 = 2;

    private final SimpleBooleanProperty pinnedProperty;
    private long pinnedTimestampSec = 0;
    private final ExchangeSpecs exchangeSpecs;
    private final String pairName;
    private final String formattedPairName;
    private final SimpleDoubleProperty p1PercentChange;
    private final SimpleDoubleProperty p2PercentChange;
    private final SimpleDoubleProperty p1RelativeChange;
    private final SimpleDoubleProperty p2RelativeChange;
    private final SimpleDoubleProperty lastPrice;
    private final SimpleObjectProperty<ChartCandle[]> chartCandlesProperty;

    public CryptonosePairData(ExchangeSpecs exchangeSpecs, String pairName, String formattedPairName) {
        this.exchangeSpecs = exchangeSpecs;
        this.pairName = pairName;
        this.formattedPairName=formattedPairName;
        pinnedProperty = new SimpleBooleanProperty(false);
        p1PercentChange=new SimpleDoubleProperty(0.0);
        p1RelativeChange=new SimpleDoubleProperty(0.0);
        p2PercentChange=new SimpleDoubleProperty(0.0);
        p2RelativeChange=new SimpleDoubleProperty(0.0);
        lastPrice=new SimpleDoubleProperty(0.0);
        chartCandlesProperty = new SimpleObjectProperty<>(null);
    }

    public String getPairName() {
        return pairName;
    }

    public String getFormattedPairName() {
        return formattedPairName;
    }

    public SimpleBooleanProperty pinnedProperty() {
        return pinnedProperty;
    }

    public long getPinnedTimestampMs() {
        return pinnedTimestampSec;
    }

    public SimpleDoubleProperty p1PercentChangeProperty() {
        return p1PercentChange;
    }

    public SimpleDoubleProperty p2PercentChangeProperty() {
        return p2PercentChange;
    }

    public SimpleDoubleProperty p1RelativeChangeProperty() {
        return p1RelativeChange;
    }

    public SimpleDoubleProperty p2RelativeChangeProperty() {
        return p2RelativeChange;
    }

    public SimpleDoubleProperty lastPriceProperty() {
        return lastPrice;
    }

    public void setPinned(boolean pinned) {
        pinnedProperty.set(pinned);
    }

    public void setPinnedTimestampMs(long pinnedTimestampSec) {
        this.pinnedTimestampSec = pinnedTimestampSec;
    }

    public void setPriceChanges(PriceChanges priceChanges, int period) {
        switch(period) {
            case PERIOD1:
                p1PercentChange.setValue(priceChanges.getPercentChange());
                p1RelativeChange.setValue(priceChanges.getRelativeLastPriceChange()!=null?priceChanges.getRelativePriceChange():0);
                break;
            case PERIOD2:
                p2PercentChange.setValue(priceChanges.getPercentChange());
                p2RelativeChange.setValue(priceChanges.getRelativeLastPriceChange()!=null?priceChanges.getRelativePriceChange():0);
                break;
        }
        lastPrice.set(priceChanges.getLastPrice());
    }

    public SimpleObjectProperty<ChartCandle[]> chartCandlesProperty() {
        return chartCandlesProperty;
    }

    public ExchangeSpecs getExchangeSpecs() {
        return exchangeSpecs;
    }
}
