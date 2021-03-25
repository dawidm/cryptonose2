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

    private ExchangeSpecs exchangeSpecs;
    private String pairApiSymbol;
    private SimpleDoubleProperty priceProperty; // to keep reference
    private SimpleObjectProperty<ChartCandle[]> chartCandlesProperty; // to keep reference

    private MinimalFxChart minimalFxChart;
    private double[] lastChartValues;

    private ChangeListener<? super ChartCandle[]> candlesListener;
    private ChangeListener<? super Number> priceListener;

    private long lastChartUpdateMs;

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
        lastChartUpdateMs = milliTime();
    }

    // listeners are set on priceProperty and chartCandlesProperty, if these properties will exist longer than this object use removeListeners()
    public synchronized void init(ExchangeSpecs exchangeSpecs, String pairName, SimpleDoubleProperty priceProperty, SimpleObjectProperty<ChartCandle[]> chartCandlesProperty) {
        this.exchangeSpecs = exchangeSpecs;
        this.pairApiSymbol = pairName;
        this.priceProperty = priceProperty;
        this.chartCandlesProperty = chartCandlesProperty;
        mainHBox.widthProperty().addListener((observable, oldValue, newValue) -> updateChart());
        pairLabel.setText(exchangeSpecs.getPairSymbolConverter().toFormattedString(pairName));
        pairLabel.getStyleClass().add(exchangeSpecs.getName().toLowerCase()+"-color");
        Tooltip.install(chartPane, new Tooltip(String.format("Chart time frame: %s", TimeConverter.secondsToFullMinutesHoursDays(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC))));
        if (chartCandlesProperty.get() != null) {
            updateChartValues(chartCandlesProperty.get());
            Platform.runLater(() -> {
                if (!mainHBox.isVisible()) {
                    mainHBox.setVisible(true);
                }
                priceLabel.setText(DecimalFormatter.formatDecimalPrice(lastChartValues[lastChartValues.length - 1]));
                updateChart();
            });
        }
        candlesListener = ((observable, oldValue, newValue) -> {
            updateChartValues(newValue);
            Platform.runLater(() -> {
                if (!mainHBox.isVisible()) {
                    mainHBox.setVisible(true);
                    priceLabel.setText(DecimalFormatter.formatDecimalPrice(lastChartValues[lastChartValues.length-1]));
                }
                updateChart();
            });
        });
        chartCandlesProperty.addListener(candlesListener);
        priceListener = (observable, oldValue, newValue) -> {
            if (!mainHBox.isVisible()) {
                Platform.runLater(() -> mainHBox.setVisible(true));
            }
            if (milliTime() - lastChartUpdateMs > MIN_UPDATE_PERIOD_MS) {
                Platform.runLater(() -> priceLabel.setText(DecimalFormatter.formatDecimalPrice(newValue.doubleValue())));
                updateChartLastVal(newValue.doubleValue());
                lastChartUpdateMs = milliTime();
            }
        };
        priceProperty.addListener(priceListener);
        mainHBox.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                CryptonoseGuiBrowser.runBrowser(pairName, exchangeSpecs);
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

    private synchronized void updateChartValues(ChartCandle[] chartCandles) {
        if (chartCandles == null)
            return;
        int numCandles = (int)(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC / CryptonoseSettings.MINI_CHART_TIME_PERIOD_SEC);
        ChartCandle[] newChartCandles;
        if (chartCandles.length < numCandles)
            newChartCandles = null;
        else
            newChartCandles = Arrays.copyOfRange(chartCandles, chartCandles.length-numCandles, chartCandles.length);
        if (newChartCandles != null) {
            lastChartValues = Arrays.stream(newChartCandles).mapToDouble(ChartCandle::getClose).toArray();
        } else {
            logger.warning("chartCandles is null %s %s, provided number of candles: %d".formatted(exchangeSpecs.getName(), pairApiSymbol, chartCandles.length));
        }
    }

    private void updateChart() {
        if (mainHBox.getWidth() == 0 || lastChartValues == null)
            return;
        updateChangeLabel();
        if (minimalFxChart != null) {
            Platform.runLater(() -> minimalFxChart.repaint(lastChartValues));
        } else {
            minimalFxChart = new MinimalFxChart(lastChartValues);
            minimalFxChart.setMarginsHorizontalPercent(0.01);
            minimalFxChart.setMarginsVerticalPercent(0.15);
            minimalFxChart.setChartPaint(priceLabel.getTextFill());
            Platform.runLater(() -> {
                chartPane.getChildren().forEach(c -> c.setVisible(false));
                chartPane.getChildren().add(minimalFxChart);
            });
        }
    }

    private synchronized void updateChartLastVal(double val) {
        if (minimalFxChart != null && lastChartValues.length > 0) {
            lastChartValues[lastChartValues.length-1] = val;
            Platform.runLater(() -> {
                updateChangeLabel();
                minimalFxChart.repaint(lastChartValues);
            });
        } else {
            logger.warning("nothing to update");
        }
    }

    private void updateChangeLabel() {
        double change = 100 * (lastChartValues[lastChartValues.length-1] - lastChartValues[0]) / lastChartValues[0];
        changeLabel.setText(String.format("%.1f%% (%s)", change, TimeConverter.secondsToFullMinutesHoursDays(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC)));
    }

    private long milliTime() {
        return System.nanoTime()/1000000;
    }

}
