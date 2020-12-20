/*
 * Cryptonose
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.cryptonose2.dataobj.PriceAlertThresholds;
import pl.dmotyka.cryptonoseengine.PriceChanges;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

/**
 * Created by dawid on 7/24/17.
 */
public class CryptonoseGuiAlertChecker {

    private final ExchangeSpecs exchangeSpecs;
    private final PairSymbolConverter pairSymbolConverter;
    private Map<String, PriceAlert> priceAlertsMap;
    private Map<Long, PriceAlertThresholds> priceAlertThresholdsMap;
    private long maxTimePeriod;

    public CryptonoseGuiAlertChecker(ExchangeSpecs exchangeSpecs, Map<Long, PriceAlertThresholds> priceAlertThresholdsMap) {
        this.exchangeSpecs = exchangeSpecs;
        pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
        this.priceAlertThresholdsMap = priceAlertThresholdsMap;
        maxTimePeriod = priceAlertThresholdsMap.keySet().stream().max(Long::compareTo).get();
        priceAlertsMap = new HashMap<>();
    }

    public List<PriceAlert> checkAlerts(List<PriceChanges> changesList) {
        List<PriceAlert> priceAlertList = new LinkedList<>();
        for (PriceChanges currentPriceChanges : changesList) {
            PriceAlertThresholds priceAlertThresholds = priceAlertThresholdsMap.get(currentPriceChanges.getTimePeriodSeconds());
            if (currentPriceChanges.getRelativePriceChange() != null) {
                double relativeChangeValue = currentPriceChanges.getRelativePriceChange().doubleValue();
                if ((currentPriceChanges.getPercentChange() > priceAlertThresholds.getRequiredRisingValue()
                        && (relativeChangeValue >= priceAlertThresholds.getRequiredRelativeRisingValue() || relativeChangeValue==0.0)
                        || relativeChangeValue >= priceAlertThresholds.getSufficientRelativeRisingValue()) ||
                        (currentPriceChanges.getPercentChange() < -priceAlertThresholds.getRequiredFallingValue()
                                && (relativeChangeValue <= -priceAlertThresholds.getRequiredRelativeFallingValue() || relativeChangeValue==0.0)
                                || relativeChangeValue <= -priceAlertThresholds.getSufficientRelativeFallingValue()))
                {
                    PriceAlert priceAlert = new PriceAlert(
                            exchangeSpecs,
                            currentPriceChanges.getCurrencyPair(),
                            pairSymbolConverter.toFormattedString(currentPriceChanges.getCurrencyPair()),
                            currentPriceChanges.getTimePeriodSeconds(),
                            System.currentTimeMillis() / 1000,
                            currentPriceChanges.getPercentChange(),
                            currentPriceChanges.getRelativePriceChange(),
                            currentPriceChanges.getMinPrice(),
                            currentPriceChanges.getMaxPrice(),
                            currentPriceChanges.getChangeTimeSeconds());
                    if(!checkPreviousAlerts(priceAlert))
                        priceAlertList.add(priceAlert);

                }
            }
        }
        return priceAlertList;
    }

    //returns true if not outdated alert exist on recent alerts list
    private boolean checkPreviousAlerts(PriceAlert priceAlert) {
        if (priceAlertsMap.containsKey(priceAlert.getPair())) {
            PriceAlert oldPriceAlert = priceAlertsMap.get(priceAlert.getPair());
            long currentTimestamp = System.currentTimeMillis()/1000;
            if(currentTimestamp - oldPriceAlert.getTimestamp() > maxTimePeriod) {
                priceAlertsMap.put(priceAlert.getPair(), priceAlert); //replace
                return false;
            } else {
                if (Math.abs(priceAlert.getPriceChange()) > 2*Math.abs(oldPriceAlert.getPriceChange()) &&
                        priceAlert.getPriceChange()*oldPriceAlert.getPriceChange() > 0) {
                    priceAlertsMap.put(priceAlert.getPair(), priceAlert); //replace
                    return false;
                }
                return true;
            }
        } else {
            priceAlertsMap.put(priceAlert.getPair(),priceAlert);
            return false;
        }

    }

}
