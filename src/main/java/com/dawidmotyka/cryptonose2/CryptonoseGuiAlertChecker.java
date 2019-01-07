package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.cryptonoseengine.PriceChanges;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by dawid on 7/24/17.
 */
public class CryptonoseGuiAlertChecker {

    private final ExchangeSpecs exchangeSpecs;
    private Map<String, PriceAlert> priceAlertsMap;
    private Map<Long,PriceAlertThresholds> priceAlertThresholdsMap;
    private long maxTimePeriod;

    public CryptonoseGuiAlertChecker(ExchangeSpecs exchangeSpecs, Map<Long, PriceAlertThresholds> priceAlertThresholdsMap) {
        this.exchangeSpecs=exchangeSpecs;
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
