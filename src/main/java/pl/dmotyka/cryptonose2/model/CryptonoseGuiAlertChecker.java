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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import pl.dmotyka.cryptonose2.controllers.AlertBlock;
import pl.dmotyka.cryptonose2.dataobj.AlertBlockTime;
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

    private static final Logger logger = Logger.getLogger(CryptonoseGuiAlertChecker.class.getName());

    private final ExchangeSpecs exchangeSpecs;
    private final PairSymbolConverter pairSymbolConverter;
    // the key is symbol,time_period, example: DOGE_BTC,300
    private final Map<String, PriceAlert> priceAlertsMap;
    private final Map<Long, PriceAlertThresholds> priceAlertThresholdsMap;
    private final long[] sortedTimePeriods;
    // value - milliseconds time (from System.nanoTime()) when alert ends
    private final ObservableList<AlertBlock> alertBlocksList = FXCollections.observableList(new LinkedList<>());
    private final AtomicBoolean p1AlertsEnabled = new AtomicBoolean();
    private final AtomicBoolean p2AlertsEnabled = new AtomicBoolean();

    public CryptonoseGuiAlertChecker(ExchangeSpecs exchangeSpecs, Map<Long, PriceAlertThresholds> priceAlertThresholdsMap) {
        this.exchangeSpecs = exchangeSpecs;
        pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
        this.priceAlertThresholdsMap = priceAlertThresholdsMap;
        sortedTimePeriods = priceAlertThresholdsMap.keySet().stream().mapToLong(val -> val).sorted().toArray();
        priceAlertsMap = new HashMap<>();
        alertBlocksList.addAll(Arrays.asList(CryptonoseSettings.getPermanentAlertBlocks(exchangeSpecs)));
        p1AlertsEnabled.set(CryptonoseSettings.getBool(CryptonoseSettings.Alert.M5_ALERTS_ENABLED, exchangeSpecs));
        p2AlertsEnabled.set(CryptonoseSettings.getBool(CryptonoseSettings.Alert.M30_ALERTS_ENABLED, exchangeSpecs));
        CryptonoseSettings.runOnPreferenceChange(exchangeSpecs, CryptonoseSettings.Alert.M5_ALERTS_ENABLED, () -> {
            logger.fine("%s m5 alerts enabled changed".formatted(exchangeSpecs.getName()));
            p1AlertsEnabled.set(CryptonoseSettings.getBool(CryptonoseSettings.Alert.M5_ALERTS_ENABLED, exchangeSpecs));
        });
        CryptonoseSettings.runOnPreferenceChange(exchangeSpecs, CryptonoseSettings.Alert.M30_ALERTS_ENABLED, () -> {
            logger.fine("%s m30 alerts enabled changed".formatted(exchangeSpecs.getName()));
            p2AlertsEnabled.set(CryptonoseSettings.getBool(CryptonoseSettings.Alert.M30_ALERTS_ENABLED, exchangeSpecs));
        });
    }

    // returns list of alerts or null if no alerts
    public List<PriceAlert> checkAlerts(List<PriceChanges> changesList) {
        List<PriceAlert> priceAlertList = null;
        for (PriceChanges currentPriceChanges : changesList) {
            if (currentPriceChanges.getTimePeriodSeconds() == sortedTimePeriods[0] && !p1AlertsEnabled.get()) {
                continue;
            }
            if (currentPriceChanges.getTimePeriodSeconds() == sortedTimePeriods[1] && !p2AlertsEnabled.get()) {
                continue;
            }
            if (currentPriceChanges.getChangeTimeSeconds() <= sortedTimePeriods[0] && currentPriceChanges.getTimePeriodSeconds() == sortedTimePeriods[1] && p1AlertsEnabled.get()) {
                continue;
            }
            PriceAlertThresholds priceAlertThresholds = priceAlertThresholdsMap.get(currentPriceChanges.getTimePeriodSeconds());
            if (currentPriceChanges.getRelativeLastPriceChange() != null) {
                double relativeChangeValue = currentPriceChanges.getRelativeLastPriceChange();
                double percentChangeValue = currentPriceChanges.getLastPercentChange();
                if ((percentChangeValue > priceAlertThresholds.getRequiredRisingValue()
                        && (relativeChangeValue >= priceAlertThresholds.getRequiredRelativeRisingValue())
                        || relativeChangeValue >= priceAlertThresholds.getSufficientRelativeRisingValue()) ||
                        (percentChangeValue < -priceAlertThresholds.getRequiredFallingValue()
                                && (relativeChangeValue <= -priceAlertThresholds.getRequiredRelativeFallingValue())
                                || relativeChangeValue <= -priceAlertThresholds.getSufficientRelativeFallingValue()))
                {
                    PriceAlert priceAlert = new PriceAlert(
                            exchangeSpecs,
                            currentPriceChanges.getCurrencyPair(),
                            pairSymbolConverter.toFormattedString(currentPriceChanges.getCurrencyPair()),
                            currentPriceChanges.getTimePeriodSeconds(),
                            currentPriceChanges.getFinalPriceTimestampSec(),
                            currentPriceChanges.getLastPercentChange(),
                            relativeChangeValue,
                            currentPriceChanges.getMinPrice(),
                            currentPriceChanges.getMaxPrice(),
                            currentPriceChanges.getChangeTimeSeconds(),
                            currentPriceChanges.getLastPriceTimestampSec(),
                            currentPriceChanges.getReferenceToLastPriceTimestampSec());
                    if(!checkPreviousAlerts(priceAlert)) {
                        if (!checkIsBlocked(priceAlert)) {
                            if (priceAlertList == null) {
                                priceAlertList = new LinkedList<>();
                            }
                            priceAlertList.add(priceAlert);
                        }
                    }

                }
            }
        }
        return priceAlertList;
    }

    public synchronized void blockAlerts(AlertBlock alertBlock) {
        logger.fine("alertBlock(..) called for %s, %d blocks active before processing new block".formatted(exchangeSpecs.getName(), alertBlocksList.size()));
        if (alertBlock.getBlockTime() == AlertBlockTime.BLOCK_PERMANENTLY) {
            logger.fine("blocking alerts for %s permanently".formatted(alertBlock.getPairApiSymbol()));
            CryptonoseSettings.addPermanentAlertBlock(exchangeSpecs, alertBlock.getPairApiSymbol());
            alertBlocksList.removeIf(b -> b.isSamePair(alertBlock));
            alertBlocksList.add(alertBlock);
        } else if (alertBlock.getBlockTime() == AlertBlockTime.UNBLOCK) {
            logger.fine("unblocking alerts for %s".formatted(alertBlock.getPairApiSymbol()));
            CryptonoseSettings.removePermanentAlertBlock(exchangeSpecs, alertBlock.getPairApiSymbol());
            alertBlocksList.removeIf(b -> b.isSamePair(alertBlock));
        } else {
            if (alertBlock.getBlockTime().getTimeSeconds() < 1) {
                throw new IllegalArgumentException("expected block time greater than 0");
            }
            boolean longerBlockExists = false;
            long newAlertEndTimeMillis = millisTime() + alertBlock.getBlockTime().getTimeSeconds()*1000;
            var alertBlockIt = alertBlocksList.iterator();
            while (alertBlockIt.hasNext()) {
                var currentBlock = alertBlockIt.next();
                if (currentBlock.isSamePair(alertBlock)) {
                    if (newAlertEndTimeMillis < currentBlock.getEndingJvmTimestampMs() || currentBlock.getBlockTime() == AlertBlockTime.BLOCK_PERMANENTLY) {
                        logger.fine("longer alert block for %s already exists".formatted(alertBlock.getPairApiSymbol()));
                        longerBlockExists = true;
                    } else {
                        logger.fine("removing block with earlier ending time");
                        alertBlockIt.remove();
                    }
                }
            }
            if (!longerBlockExists) {
                logger.fine("adding new alert block %s %s".formatted(alertBlock.getPairApiSymbol(), alertBlock.getBlockTime().getLabel()));
                alertBlocksList.add(alertBlock);
            }
        }
    }

    public ObservableList<AlertBlock> getBlocksObservableList() {
        return FXCollections.unmodifiableObservableList(alertBlocksList);
    }

    // check whether there were previous alerts that should block this alert
    private boolean checkPreviousAlerts(PriceAlert priceAlert) {
        String alertKey = priceAlert.getPair() + "," + priceAlert.getPeriodSeconds();
        if (!priceAlertsMap.containsKey(alertKey)) {
            priceAlertsMap.put(alertKey, priceAlert);
            return false; // allow the alert
        } else {
            PriceAlert oldPriceAlert = priceAlertsMap.get(alertKey);
            if (CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_ALLOW_SUBSEQUENT_2X_ALERTS, exchangeSpecs) &&
                    Math.abs(priceAlert.getPriceChange()) >= 2 * Math.abs(oldPriceAlert.getPriceChange())) {
                priceAlertsMap.put(alertKey, priceAlert); // replace
                return false; // allow the alert
            }
            if (CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_BLOCK_SUBSEQUENT_ALERTS, exchangeSpecs)) {
                if (priceAlert.getTimestamp() - oldPriceAlert.getTimestamp() > CryptonoseSettings.ALERTS_PAUSE_SECONDS) {
                    priceAlertsMap.put(alertKey, priceAlert); // replace
                    return false; // allow the alert
                }
            } else {
                if (priceAlert.getReferencePriceTimestamp() >= oldPriceAlert.getFinalPriceTimestamp()
                        && priceAlert.getChangeTimeSeconds() != 0
                        && priceAlert.getPriceChange() * oldPriceAlert.getPriceChange() < 0) {
                    priceAlertsMap.put(alertKey, priceAlert); // replace
                    return false; // allow the alert
                }
            }
            return true; // block the alert
        }
    }

    private boolean checkIsBlocked(PriceAlert priceAlert) {
        for (var iterator = alertBlocksList.iterator(); iterator.hasNext(); ) {
            AlertBlock alertBlock = iterator.next();
            if (alertBlock.isForAlert(priceAlert)) {
                if (millisTime() < alertBlock.getEndingJvmTimestampMs()) {
                    logger.fine("%s alert blocked, block ends in: %.1f hours".formatted(priceAlert.getPair(), msToHours(alertBlock.getEndingJvmTimestampMs() - millisTime())));
                    return true;
                } else {
                    logger.fine("removing outdated alert for %s, end time: %s".formatted(priceAlert.getPair(), msToHours(alertBlock.getEndingJvmTimestampMs() - millisTime())));
                    iterator.remove();
                    return false;
                }
            }
        }
        return false;
    }

    private long millisTime() {
        return System.nanoTime() / (1000 * 1000);
    }

    private double msToHours(long millis) {
        return (double)millis / (1000*3600);
    }

}
