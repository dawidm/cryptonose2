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

package pl.dmotyka.cryptonose2.dataobj;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
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
    public static final int DEF_THROTTLE_INTERVAL_MS = 200;
    public static final int MIN_THROTTLE_INTERVAL_MS = 200;

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
    private boolean p1WaitingForUpdate = false;
    private boolean p2WaitingForUpdate = false;
    private PriceChanges lastP1PriceChanges = null;
    private PriceChanges lastP2PriceChanges = null;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

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
        scheduledExecutorService.scheduleAtFixedRate(this::updateValues, DEF_THROTTLE_INTERVAL_MS, DEF_THROTTLE_INTERVAL_MS, TimeUnit.MILLISECONDS);
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

    public synchronized void setPriceChanges(PriceChanges priceChanges, int period) {
        switch (period) {
            case PERIOD1 -> {
                lastP1PriceChanges = priceChanges;
                p1WaitingForUpdate = true;
            }
            case PERIOD2 -> {
                lastP2PriceChanges = priceChanges;
                p2WaitingForUpdate = true;
            }
        }
    }

    public SimpleObjectProperty<ChartCandle[]> chartCandlesProperty() {
        return chartCandlesProperty;
    }

    public ExchangeSpecs getExchangeSpecs() {
        return exchangeSpecs;
    }

    // return true exchange and pair api symbol are equal
    public boolean isSamePair(CryptonosePairData other) {
        return exchangeSpecs.equals(other.getExchangeSpecs()) && pairName.equals(other.getPairName());
    }

    // should be >= MIN_THROTTLE_INTERVAL_MS
    public void setUpdateThrottleIntervalMs(long updateThrottleIntervalMs) {
        if (updateThrottleIntervalMs >= MIN_THROTTLE_INTERVAL_MS) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(this::updateValues, updateThrottleIntervalMs, updateThrottleIntervalMs, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalArgumentException("updateThrottleIntervalMs should be >= MIN_THROTTLE_INTERVAL_MS");
        }
    }

    private synchronized void updateValues() {
        if (p1WaitingForUpdate || p2WaitingForUpdate) {
            Platform.runLater(() -> {
                if (p1WaitingForUpdate) {
                    p1PercentChange.setValue(lastP1PriceChanges.getPercentChange());
                    p1RelativeChange.setValue(lastP1PriceChanges.getRelativePriceChange() != null ? lastP1PriceChanges.getRelativePriceChange() : 0);
                    lastPrice.set(lastP1PriceChanges.getLastPrice());
                    p1WaitingForUpdate = false;
                }
                if (p2WaitingForUpdate) {
                    p2PercentChange.setValue(lastP2PriceChanges.getPercentChange());
                    p2RelativeChange.setValue(lastP2PriceChanges.getRelativePriceChange() != null ? lastP2PriceChanges.getRelativePriceChange() : 0);
                    lastPrice.set(lastP2PriceChanges.getLastPrice());
                    p2WaitingForUpdate = false;
                }
            });
        }
    }

    private long vmTimeMillis() {
        return System.nanoTime() / 1000000;
    }


}
