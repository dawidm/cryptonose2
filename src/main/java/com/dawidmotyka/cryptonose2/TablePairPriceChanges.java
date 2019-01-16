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
