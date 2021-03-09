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
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import pl.dmotyka.cryptonose2.CryptonoseGuiBrowser;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.minimalfxcharts.MinimalFxChart;

public class CryptonoseGuiPinnedNodeController {

    private static final Logger logger = Logger.getLogger(CryptonoseGuiPinnedNodeController.class.getName());

    private static final long MIN_UPDATE_PERIOD_MS = 1000;

    private ExchangeSpecs exchangeSpecs;

    private String pairName;
    private MinimalFxChart minimalFxChart;
    private double[] lastChartValues;

    private SimpleDoubleProperty priceProperty; // to keep reference
    private SimpleObjectProperty<ChartCandle[]> chartCandlesProperty; // to keep reference

    private long lastChartUpdateMs;

    @FXML
    public HBox mainHBox;
    @FXML
    public Label pairLabel;
    @FXML
    public Label priceLabel;
    @FXML
    public Pane chartPane;

    public CryptonoseGuiPinnedNodeController() {
        lastChartUpdateMs = milliTime();
    }

    public synchronized void init(ExchangeSpecs exchangeSpecs, String pairName, SimpleDoubleProperty priceProperty, SimpleObjectProperty<ChartCandle[]> chartCandlesProperty) {
        this.exchangeSpecs = exchangeSpecs;
        this.pairName = pairName;
        this.priceProperty = priceProperty;
        this.chartCandlesProperty = chartCandlesProperty;
        mainHBox.widthProperty().addListener((observable, oldValue, newValue) -> updateChart());
        pairLabel.setText(exchangeSpecs.getPairSymbolConverter().toFormattedString(pairName));
        pairLabel.getStyleClass().add(exchangeSpecs.getName().toLowerCase()+"-color");
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
        chartCandlesProperty.addListener(((observable, oldValue, newValue) -> {
            updateChartValues(newValue);
            Platform.runLater(() -> {
                if (!mainHBox.isVisible()) {
                    mainHBox.setVisible(true);
                    priceLabel.setText(DecimalFormatter.formatDecimalPrice(lastChartValues[lastChartValues.length-1]));
                }
                updateChart();
            });
        }));
        priceProperty.addListener((observable, oldValue, newValue) -> {
            if (!mainHBox.isVisible()) {
                Platform.runLater(() -> mainHBox.setVisible(true));
            }
            if (milliTime() - lastChartUpdateMs > MIN_UPDATE_PERIOD_MS) {
                Platform.runLater(() -> priceLabel.setText(DecimalFormatter.formatDecimalPrice(newValue.doubleValue())));
                updateChartLastVal(newValue.doubleValue());
                lastChartUpdateMs = milliTime();
            }
        });
        mainHBox.setOnMouseClicked(e -> CryptonoseGuiBrowser.runBrowser(pairName, exchangeSpecs));
        mainHBox.managedProperty().bind(mainHBox.visibleProperty());
        mainHBox.setVisible(false);
    }

    private synchronized void updateChartValues(ChartCandle[] chartCandles) {
        if (chartCandles == null)
            return;
        int numCandles = (int)(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC / CryptonoseSettings.MINI_CHART_TIME_PERIOD_SEC);
        if (chartCandles.length < numCandles)
            chartCandles = null;
        else
            chartCandles = Arrays.copyOfRange(chartCandles, chartCandles.length-numCandles, chartCandles.length);
        if (chartCandles != null) {
            lastChartValues = Arrays.stream(chartCandles).mapToDouble(ChartCandle::getClose).toArray();
        } else {
            logger.warning("chartCandles is null");
        }
    }

    private void updateChart() {
        if (mainHBox.getWidth() == 0 || lastChartValues == null)
            return;
        if (minimalFxChart != null) {
            Platform.runLater(() -> minimalFxChart.repaint(lastChartValues));
        } else {
            minimalFxChart = new MinimalFxChart(lastChartValues);
            minimalFxChart.setMarginsHorizontalPercent(0.01);
            minimalFxChart.setMarginsVerticalPercent(0.15);
            minimalFxChart.setChartPaint(priceLabel.getTextFill());
            Platform.runLater(() -> chartPane.getChildren().add(minimalFxChart));
        }
    }

    private synchronized void updateChartLastVal(double val) {
        if (minimalFxChart != null && lastChartValues.length > 0) {
            lastChartValues[lastChartValues.length-1] = val;
            Platform.runLater(() -> minimalFxChart.repaint(lastChartValues));
        } else {
            logger.warning("nothing to update");
        }
    }

    private long milliTime() {
        return System.nanoTime()/1000000;
    }

}
