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

package pl.dmotyka.cryptonose2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.cryptonose2.dataobj.PriceAlertThresholds;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.cryptonoseengine.PriceChanges;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

/**
 * Created by dawid on 7/24/17.
 */
public class CryptonoseGuiAlertChecker {

    private final ExchangeSpecs exchangeSpecs;
    private final PairSymbolConverter pairSymbolConverter;
    // the key is symbol,time_period, example: DOGE_BTC,300
    private final Map<String, PriceAlert> priceAlertsMap;
    private final Map<Long, PriceAlertThresholds> priceAlertThresholdsMap;
    private final long[] sortedTimePeriods;

    public CryptonoseGuiAlertChecker(ExchangeSpecs exchangeSpecs, Map<Long, PriceAlertThresholds> priceAlertThresholdsMap) {
        this.exchangeSpecs = exchangeSpecs;
        pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
        this.priceAlertThresholdsMap = priceAlertThresholdsMap;
        sortedTimePeriods = priceAlertThresholdsMap.keySet().stream().mapToLong(val -> val).sorted().toArray();
        priceAlertsMap = new HashMap<>();
    }

    public List<PriceAlert> checkAlerts(List<PriceChanges> changesList) {
        List<PriceAlert> priceAlertList = new LinkedList<>();
        for (PriceChanges currentPriceChanges : changesList) {
            if (currentPriceChanges.getChangeTimeSeconds() <= sortedTimePeriods[0] && currentPriceChanges.getTimePeriodSeconds() == sortedTimePeriods[1]) {
                continue;
            }
            PriceAlertThresholds priceAlertThresholds = priceAlertThresholdsMap.get(currentPriceChanges.getTimePeriodSeconds());
            if (currentPriceChanges.getRelativePriceChange() != null) {
                double relativeChangeValue = currentPriceChanges.getRelativePriceChange();
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
                            currentPriceChanges.getChangeTimeSeconds(),
                            currentPriceChanges.getFinalPriceTimestamp(),
                            currentPriceChanges.getReferencePriceTimestamp());
                    if(!checkPreviousAlerts(priceAlert))
                        priceAlertList.add(priceAlert);

                }
            }
        }
        return priceAlertList;
    }

    // check whether there were previous alerts that should block this alert
    private boolean checkPreviousAlerts(PriceAlert priceAlert) {
        String alertKey = priceAlert.getPair() + "," + priceAlert.getPeriodSeconds();
        if (!priceAlertsMap.containsKey(alertKey)) {
            priceAlertsMap.put(alertKey, priceAlert);
            return false; // allow the alert
        } else {
            PriceAlert oldPriceAlert = priceAlertsMap.get(alertKey);
            if (CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_ALLOW_SUBSEQUENT_2X_ALERTS) &&
                    Math.abs(priceAlert.getPriceChange()) >= 2 * Math.abs(oldPriceAlert.getPriceChange())) {
                priceAlertsMap.put(alertKey, priceAlert); // replace
                return false; // allow the alert
            }
            if (CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_BLOCK_SUBSEQUENT_ALERTS)) {
                if (priceAlert.getTimestamp() - oldPriceAlert.getTimestamp() > CryptonoseSettings.ALERTS_PAUSE_SECONDS) {
                    priceAlertsMap.put(alertKey, priceAlert); // replace
                    return false; // allow the alert
                }
            } else {
                if (priceAlert.getReferencePriceTimestamp() > oldPriceAlert.getFinalPriceTimestamp() && priceAlert.getPriceChange() * oldPriceAlert.getPriceChange() < 0) {
                    priceAlertsMap.put(alertKey, priceAlert); // replace
                    return false; // allow the alert
                }
            }
            return true; // block the alert
        }

    }

}
