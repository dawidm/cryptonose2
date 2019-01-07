package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.cryptonoseengine.PriceChanges;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

/**
 * Created by dawid on 8/9/17.
 */
public class PairPriceChanges {

    public static final int PERIOD1 = 1;
    public static final int PERIOD2 = 2;

    private final String pairName;
    private final String formattedPairName;
    private PriceChanges p1changes;
    private PriceChanges p2changes;

    public PairPriceChanges(ExchangeSpecs exchangeSpecs, String pairName) {
        this.pairName = pairName;
        this.formattedPairName=PairSymbolConverter.toFormattedString(exchangeSpecs, pairName);
    }

    public String getPairName() {
        return pairName;
    }

    public String getFormattedPairName() {
        return formattedPairName;
    }

    public Double getP1PercentPriceChange() {
        if (p1changes!=null) {
            return new Double(p1changes.getPercentChange());
        } else {
            return null;
        }
    }

    public Double getP1RelativePriceChange() {
        if (p1changes!=null) {
            if(p1changes.getRelativePriceChange()==null)
                return new Double(0.0);
            return new Double(p1changes.getRelativePriceChange());
        } else {
            return null;
        }
    }

    public Double getP2PercentPriceChange() {
        if (p2changes!=null) {
            return new Double(p2changes.getPercentChange());
        } else {
            return null;
        }
    }

    public Double getP2RelativePriceChange() {
        if (p2changes!=null) {
            if(p2changes.getRelativePriceChange()==null)
                return new Double(0.0);
            return new Double(p2changes.getRelativePriceChange());
        } else {
            return null;
        }
    }

    public Double getLastPrice() {
        if (p2changes!=null)
            return new Double(p2changes.getLastPrice());
        else if (p1changes!=null)
            return new Double(p1changes.getLastPrice());
        else
            return null;
    }

    public void setPriceChanges(PriceChanges priceChanges, int period) {
        switch(period) {
            case PERIOD1:
                p1changes=priceChanges;
                break;
            case PERIOD2:
                p2changes=priceChanges;
                break;
        }
    }

}
