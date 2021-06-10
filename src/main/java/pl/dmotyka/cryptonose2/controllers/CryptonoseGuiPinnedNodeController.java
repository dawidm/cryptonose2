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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.tools.TimeConverter;
import pl.dmotyka.minimalfxcharts.MinimalFxChart;

public class CryptonoseGuiPinnedNodeController {

    private static final Logger logger = Logger.getLogger(CryptonoseGuiPinnedNodeController.class.getName());

    private static final long MIN_UPDATE_PERIOD_MS = 1000;
    public static final long MINI_CHART_TIMEFRAME_SEC = CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC;
    public static final long MINI_CHART_TIME_PERIOD_SEC = CryptonoseSettings.MINI_CHART_TIME_PERIOD_SEC;

    private ExchangeSpecs exchangeSpecs;
    private String pairApiSymbol;
    private SimpleDoubleProperty priceProperty; // to keep reference
    private SimpleObjectProperty<ChartCandle[]> chartCandlesProperty; // to keep reference

    private MinimalFxChart minimalFxChart;
    private final AtomicReference<double[]> lastChartValues = new AtomicReference<>();

    private ChangeListener<? super ChartCandle[]> candlesListener;
    private ChangeListener<? super Number> priceListener;

    private AtomicLong lastChartUpdateMs = new AtomicLong();

    @FXML
    public HBox mainHBox;
    @FXML
    public Label pairLabel;
    @FXML
    public Label priceLabel;
    @FXML
    public Label changeLabel;
    @FXML
    public Pane chartPane;

    public CryptonoseGuiPinnedNodeController() {
        lastChartUpdateMs.set(milliTime());
    }

    // listeners are set on priceProperty and chartCandlesProperty, if these properties will exist longer than this object use removeListeners()
    public synchronized void init(ExchangeSpecs exchangeSpecs, String pairApiSymbol, SimpleDoubleProperty priceProperty, SimpleObjectProperty<ChartCandle[]> chartCandlesProperty) {
        this.exchangeSpecs = exchangeSpecs;
        this.pairApiSymbol = pairApiSymbol;
        this.priceProperty = priceProperty;
        this.chartCandlesProperty = chartCandlesProperty;
        mainHBox.widthProperty().addListener((observable, oldValue, newValue) -> updateChart());
        pairLabel.setText(exchangeSpecs.getPairSymbolConverter().toFormattedString(pairApiSymbol));
        pairLabel.getStyleClass().add(exchangeSpecs.getName().toLowerCase()+"-color");
        Tooltip.install(chartPane, new Tooltip(String.format("Chart time frame: %s", TimeConverter.secondsToFullMinutesHoursDays(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC))));
        Tooltip.install(pairLabel, new Tooltip("%s on %s".formatted(exchangeSpecs.getPairSymbolConverter().toFormattedString(pairApiSymbol), exchangeSpecs.getName())));
        final ChartCandle[] finalChartCandles = chartCandlesProperty.get();
        if (finalChartCandles != null) {
            final double[] finalLastChartValues = createChartValues(finalChartCandles);
            if(finalLastChartValues != null) {
                Platform.runLater(() -> {
                    if (!mainHBox.isVisible()) {
                        mainHBox.setVisible(true);
                    }
                    priceLabel.setText(DecimalFormatter.formatDecimalPrice(finalLastChartValues[finalLastChartValues.length - 1]));
                    updateChart(finalLastChartValues);
                });
            }
        }
        candlesListener = ((observable, oldValue, newValue) -> {
            final double[] finalLastChartValues = createChartValues(newValue);
            if(finalLastChartValues != null) {
                Platform.runLater(() -> {
                    if (!mainHBox.isVisible()) {
                        mainHBox.setVisible(true);
                        priceLabel.setText(DecimalFormatter.formatDecimalPrice(finalLastChartValues[finalLastChartValues.length - 1]));
                    }
                    updateChart(finalLastChartValues);
                });
            }
        });
        chartCandlesProperty.addListener(candlesListener);
        priceListener = (observable, oldValue, newValue) -> {
            if (!mainHBox.isVisible()) {
                Platform.runLater(() -> mainHBox.setVisible(true));
            }
            if (milliTime() - lastChartUpdateMs.get() > MIN_UPDATE_PERIOD_MS) {
                Platform.runLater(() -> priceLabel.setText(DecimalFormatter.formatDecimalPrice(newValue.doubleValue())));
                updateChartLastVal(newValue.doubleValue());
                lastChartUpdateMs.set(milliTime());
            }
        };
        priceProperty.addListener(priceListener);
        mainHBox.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                CryptonoseGuiBrowser.runBrowser(pairApiSymbol, exchangeSpecs);
            }
        });
        mainHBox.managedProperty().bind(mainHBox.visibleProperty());
        mainHBox.setVisible(false);
    }

    // remove listeners on priceProperty and chartCandlesProperty, set on init()
    public synchronized void removeListeners() {
        if (priceProperty != null) {
            logger.fine("removing price listener %s %s".formatted(exchangeSpecs.getName(), pairApiSymbol));
            priceProperty.removeListener(priceListener);
        }
        if (chartCandlesProperty != null) {
            logger.fine("removing candles listener %s %s".formatted(exchangeSpecs.getName(), pairApiSymbol));
            chartCandlesProperty.removeListener(candlesListener);
        }
    }

    // returns close price values for the time specified by MINI_CHART_TIMEFRAME_SEC using the most recent values of chartCandles
    //  chartCandles should be for the period MINI_CHART_TIME_PERIOD_SEC
    private double[] createChartValues(ChartCandle[] chartCandles) {
        if (chartCandles == null) {
            logger.warning("%s %s, chartCandles is null".formatted(exchangeSpecs.getName(), pairApiSymbol));
            return null;
        }
        int numCandles = (int)(MINI_CHART_TIMEFRAME_SEC / MINI_CHART_TIME_PERIOD_SEC);
        ChartCandle[] newChartCandles;
        if (chartCandles.length < numCandles) {
            logger.warning("%s %s, provided number of candles: %d, expected>%s".formatted(exchangeSpecs.getName(), pairApiSymbol, chartCandles.length, numCandles));
            return null;
        } else {
            newChartCandles = Arrays.copyOfRange(chartCandles, chartCandles.length - numCandles, chartCandles.length);
            lastChartValues.set(Arrays.stream(newChartCandles).mapToDouble(ChartCandle::getClose).toArray());
            return lastChartValues.get();
        }
    }

    // paint data saved in lastChartValues on minimalFxChart
    private void updateChart() {
        updateChart(lastChartValues.get());
    }

    // paint provided data on minimalFxChart
    private void updateChart(double[] values) {
        if (mainHBox.getWidth() == 0 || values == null)
            return;
        updateChangeLabel(values);
        if (minimalFxChart != null) {
            Platform.runLater(() -> minimalFxChart.repaint(values));
        } else {
            minimalFxChart = new MinimalFxChart(values, priceLabel.getTextFill(), null);
            minimalFxChart.setMarginsHorizontalPercent(1);
            minimalFxChart.setMarginsVerticalPercent(15);
            Platform.runLater(() -> {
                chartPane.getChildren().forEach(c -> c.setVisible(false));
                chartPane.getChildren().add(minimalFxChart);
            });
        }
    }

    // update lastChartValues data with new last value, update chart and change label
    private void updateChartLastVal(double val) {
        final double[] finalLastChartValues = lastChartValues.get();
        if (minimalFxChart != null && finalLastChartValues != null && finalLastChartValues.length > 0) {
            finalLastChartValues[finalLastChartValues.length-1] = val;
            updateChart(finalLastChartValues);
        } else {
            logger.warning("nothing to update");
        }
    }

    // updates change label with value calculated using values
    private void updateChangeLabel(double[] values) {
        double change = 100 * (values[values.length-1] - values[0]) / values[0];
        changeLabel.setText(String.format("%.1f%% (%s)", change, TimeConverter.secondsToFullMinutesHoursDays(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC)));
    }

    private long milliTime() {
        return System.nanoTime()/1000000;
    }

}
