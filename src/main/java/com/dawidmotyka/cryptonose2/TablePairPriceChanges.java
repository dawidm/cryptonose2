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

import com.dawidmotyka.cryptonoseengine.PriceChanges;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Created by dawid on 8/9/17.
 */
public class TablePairPriceChanges {

    public static final int PERIOD1 = 1;
    public static final int PERIOD2 = 2;

    private final String pairName;
    private final String formattedPairName;
    private SimpleDoubleProperty p1PercentChange;
    private SimpleDoubleProperty p2PercentChange;
    private SimpleDoubleProperty p1RelativeChange;
    private SimpleDoubleProperty p2RelativeChange;
    private SimpleDoubleProperty lastPrice;

    public TablePairPriceChanges(ExchangeSpecs exchangeSpecs, String pairName) {
        this.pairName = pairName;
        this.formattedPairName=PairSymbolConverter.toFormattedString(exchangeSpecs, pairName);
        p1PercentChange=new SimpleDoubleProperty(0.0);
        p1RelativeChange=new SimpleDoubleProperty(0.0);
        p2PercentChange=new SimpleDoubleProperty(0.0);
        p2RelativeChange=new SimpleDoubleProperty(0.0);
        lastPrice=new SimpleDoubleProperty(0.0);
    }

    public String getPairName() {
        return pairName;
    }

    public String getFormattedPairName() {
        return formattedPairName;
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

    public void setPriceChanges(PriceChanges priceChanges, int period) {
        switch(period) {
            case PERIOD1:
                p1PercentChange.setValue(priceChanges.getPercentChange());
                p1RelativeChange.set(priceChanges.getRelativePriceChange());
                break;
            case PERIOD2:
                p2PercentChange.setValue(priceChanges.getPercentChange());
                p2RelativeChange.set(priceChanges.getRelativePriceChange());
                break;
        }
        lastPrice.set(priceChanges.getLastPrice());
    }

}
