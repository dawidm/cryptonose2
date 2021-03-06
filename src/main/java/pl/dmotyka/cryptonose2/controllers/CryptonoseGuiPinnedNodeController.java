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

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.minimalfxcharts.MinimalFxChart;

public class CryptonoseGuiPinnedNodeController {

    private static final Logger logger = Logger.getLogger(CryptonoseGuiPinnedNodeController.class.getName());

    private String pairName;
    private MinimalFxChart minimalFxChart;
    private double[] chartValues;

    @FXML
    public Label pairLabel;
    @FXML
    public Label priceLabel;
    @FXML
    public Pane chartPane;


    public synchronized void fillPane(String pairName, SimpleDoubleProperty priceProperty, SimpleObjectProperty<ChartCandle[]> chartCandlesProperty) {
        this.pairName = pairName;
        pairLabel.setText(pairName);
        priceLabel.textProperty().bind(priceProperty.asString());
        ChartCandle[] chartCandles = chartCandlesProperty.get();
        updateChart(chartCandles);
        chartCandlesProperty.addListener(((observable, oldValue, newValue) -> updateChart(newValue)));
        priceProperty.addListener((observable, oldValue, newValue) -> updateChartLastVal(newValue.doubleValue()));

    }

    private synchronized void updateChart(ChartCandle[] chartCandles) {
        int numCandles = (int)(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC / CryptonoseSettings.MINI_CHART_TIME_PERIOD_SEC);
        if (chartCandles.length < numCandles)
            chartCandles = null;
        else
            chartCandles = Arrays.copyOfRange(chartCandles, chartCandles.length-numCandles, chartCandles.length);
        if (chartCandles != null) {
            chartValues = Arrays.stream(chartCandles).mapToDouble(ChartCandle::getClose).toArray();
            if (minimalFxChart != null) {
                minimalFxChart.repaint(chartValues);
            } else {
                minimalFxChart = new MinimalFxChart(chartValues);
                minimalFxChart.setMarginsHorizontalPercent(0.01);
                minimalFxChart.setMarginsVerticalPercent(0.01);
                minimalFxChart.setChartPaint(Color.BLACK);
                chartPane.getChildren().add(minimalFxChart);
            }
        } else {
            logger.warning("chartCandles is null");
        }
    }

    private synchronized void updateChartLastVal(double val) {
        if (minimalFxChart != null) {
            chartValues[chartValues.length-1] = val;
            minimalFxChart.repaint(chartValues);
        } else {
            logger.warning("nothing to update");
        }
    }

}
