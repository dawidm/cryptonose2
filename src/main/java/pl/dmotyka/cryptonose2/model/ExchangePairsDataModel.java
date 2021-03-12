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

package pl.dmotyka.cryptonose2.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import pl.dmotyka.cryptonose2.dataobj.CryptonosePairData;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.cryptonoseengine.PriceChanges;
import pl.dmotyka.exchangeutils.chartdataprovider.CurrencyPairTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

public class ExchangePairsDataModel {

    private final ExchangeSpecs exchangeSpecs;
    private final PairSymbolConverter pairSymbolConverter;
    private final long[] timePeriods;

    private Map<String, CryptonosePairData> cnPairDataMap = new HashMap<>();
    private ObservableList<CryptonosePairData> cnPairDataObservableList = FXCollections.observableArrayList();

    private Map<CurrencyPairTimePeriod, ChartCandle[]> candlesMap;

    public ExchangePairsDataModel(ExchangeSpecs exchangeSpecs, long[] timePeriods) {
        this.exchangeSpecs = exchangeSpecs;
        this.timePeriods = timePeriods;
        this.pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
    }

    public synchronized void updateData(List<PriceChanges> priceChangesList) {
        for (PriceChanges priceChanges : priceChangesList) {
            CryptonosePairData cnPairData = cnPairDataMap.get(priceChanges.getCurrencyPair());
            if (cnPairData == null) {
                cnPairData = new CryptonosePairData(exchangeSpecs, priceChanges.getCurrencyPair(), pairSymbolConverter.toFormattedString(priceChanges.getCurrencyPair()));
                ChartCandle[] candles = candlesMap.get(new CurrencyPairTimePeriod(priceChanges.getCurrencyPair(), CryptonoseSettings.TIME_PERIODS[0]));
                if (candles != null) {
                    cnPairData.chartCandlesProperty().set(candles);
                }
                cnPairDataMap.put(priceChanges.getCurrencyPair(), cnPairData);
                cnPairDataObservableList.add(cnPairData);
            }
            int period = (priceChanges.getTimePeriodSeconds() == timePeriods[0]) ? CryptonosePairData.PERIOD1 : CryptonosePairData.PERIOD2;
            cnPairData.setPriceChanges(priceChanges, period);
        }
    }

    public synchronized void updateChartData(Map<CurrencyPairTimePeriod, ChartCandle[]> candlesMap) {
        this.candlesMap = candlesMap;
        for (CryptonosePairData cnPairData : cnPairDataObservableList) {
            ChartCandle[] candles = candlesMap.get(new CurrencyPairTimePeriod(cnPairData.getPairName(), CryptonoseSettings.TIME_PERIODS[0]));
            if (candles != null) {
                cnPairData.chartCandlesProperty().set(candles);
            }
        }
    }

    // update pairs list, removing these that are not in provided list
    // pairs - list of api symbols of pairs
    public synchronized void removeOutdatedPairs(String[] pairs) {
        Set<String> newPairs = Set.of(pairs);
        Set<String> outdatedPairs = new HashSet<>(cnPairDataMap.keySet());
        outdatedPairs.removeAll(newPairs);
        for (String pair : outdatedPairs) {
            cnPairDataObservableList.remove(cnPairDataMap.get(pair));
            cnPairDataMap.remove(pair);
        }
    }

    // clears a table
    public synchronized void clear() {
        Platform.runLater(() -> cnPairDataObservableList.clear());
        cnPairDataMap.clear();
    }

    public synchronized ObservableList<CryptonosePairData> getReadonlyItems() {
        return FXCollections.unmodifiableObservableList(cnPairDataObservableList);
    }

    public synchronized ObservableList<CryptonosePairData> getItems() {
        return cnPairDataObservableList;
    }
}