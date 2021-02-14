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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import pl.dmotyka.cryptonose2.CryptonoseGuiBrowser;
import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.minimalfxcharts.MinimalFxChart;

/**
 * Created by dawid on 9/3/17.
 */
public class CryptonoseGuiPriceAlertNodeController {
    @FXML
    public HBox mainHBox;
    @FXML
    public Label changeLabel;
    @FXML
    public Label pairNameLabel;
    @FXML
    public Label finalPriceLabel;
    @FXML
    public Label periodLabel;
    @FXML
    public Label exchangeLabel;
    @FXML
    public Label timeLabel;
    @FXML
    public StackPane chartPane;
    @FXML
    public HBox moreHBox;

    public void fillPane(PriceAlert priceAlert, ChartCandle[] chartCandles) {
        String arrowString;
        if(priceAlert.getPriceChange()<0) {
            arrowString = "\u2198";
            changeLabel.getStyleClass().add("price-falling");
        }
        else {
            arrowString = "\u2197";
            changeLabel.getStyleClass().add("price-rising");
        }
        if (chartCandles != null) {
            MinimalFxChart minimalFxChart = new MinimalFxChart(chartCandlesToClosePrices(chartCandles));
            minimalFxChart.setMarginsHorizontalPercent(0.01);
            minimalFxChart.setMarginsVerticalPercent(0.01);
            minimalFxChart.setChartPaint(Color.BLACK);
            finalPriceLabel.textFillProperty().addListener(observable -> minimalFxChart.setChartPaint(finalPriceLabel.getTextFill()));
            chartPane.getChildren().add(minimalFxChart);
        }
        pairNameLabel.setText(priceAlert.getFormattedPair());
        String changeText = String.format("%s %.2f%% (%.2f)",arrowString, Math.abs(priceAlert.getPriceChange()),Math.abs(priceAlert.getRelativePriceChange()));
        changeLabel.setText(changeText);
        finalPriceLabel.setText(DecimalFormatter.formatDecimalPrice(priceAlert.getFinalPrice()));
        periodLabel.setText(String.format("%s (%ds)",priceAlert.getFormattedTimePeriod(),priceAlert.getChangeTimeSeconds()));
        if(priceAlert.getExchangeSpecs()!=null) {
            exchangeLabel.setText(priceAlert.getExchangeSpecs().getName());
            pairNameLabel.getStyleClass().add(priceAlert.getExchangeSpecs().getName().toLowerCase()+"-color");
        }
        mainHBox.setOnMouseClicked((event) -> {
            CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs());
        });
        timeLabel.setText(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        String baseCurrency = priceAlert.getExchangeSpecs().getPairSymbolConverter().apiSymbolToXchangeCurrencyPair(priceAlert.getPair()).base.getCurrencyCode();
        PriceAlertPluginsButtons buttons = new PriceAlertPluginsButtons();
        buttons.install(moreHBox, baseCurrency);
    }

    private double[] chartCandlesToClosePrices(ChartCandle[] chartCandles) {
        return Arrays.stream(chartCandles).mapToDouble(candle -> candle.getClose()).toArray();
    }

}
