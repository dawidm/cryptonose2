/*
 * Cryptonose2
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

package pl.dmotyka.cryptonose2.controllers;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import pl.dmotyka.cryptonose2.CryptonoseGuiBrowser;
import pl.dmotyka.cryptonose2.PriceAlert;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by dawid on 9/3/17.
 */
public class CryptonoseGuiPriceAlertNodeController {
    public HBox mainHBox;
    public Label changeLabel;
    public Label pairNameLabel;
    public Label finalPriceLabel;
    public Label periodLabel;
    public Label exchangeLabel;
    public Label timeLabel;

    public void fillPane(PriceAlert priceAlert) {
        String arrowString;
        if(priceAlert.getPriceChange()<0) {
            arrowString = "\u2198";
            changeLabel.setStyle(changeLabel.getStyle() + "-fx-text-fill: red");
        }
        else {
            arrowString = "\u2197";
            changeLabel.setStyle(changeLabel.getStyle() + "-fx-text-fill: green");
        }
        pairNameLabel.setText(priceAlert.getFormattedPair());
        changeLabel.setText(String.format("%.2f (%.2f) %s",priceAlert.getPriceChange(),priceAlert.getRelativePriceChange(),arrowString));
        finalPriceLabel.setText(String.format("%.8f",priceAlert.getFinalPrice()));
        periodLabel.setText(String.format("%s (%ds)",priceAlert.getFormattedTimePeriod(),priceAlert.getChangeTimeSeconds()));
        if(priceAlert.getExchangeSpecs()!=null) {
            exchangeLabel.setText(priceAlert.getExchangeSpecs().getName());
            pairNameLabel.setStyle(pairNameLabel.getStyle() + "-fx-text-fill: #" + priceAlert.getExchangeSpecs().getColorHash());
        }
        mainHBox.setOnMouseClicked((event) -> {
            CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs());
        });
        timeLabel.setText(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}
