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

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import pl.dmotyka.cryptonose2.PriceAlertPlugin;

public class PriceAlertPluginsButtons {

    public void install(HBox hbox, String baseCurrency, boolean focusTraversable) {
        hbox.setSpacing(2);
        PriceAlertPlugin geckoPlugin = new PriceAlertPluginGecko(baseCurrency);
        Button geckoButton = new Button(geckoPlugin.getButtonTitle());
        geckoButton.getStyleClass().add("button-small");
        geckoButton.getStyleClass().add(geckoPlugin.getButtonCssClass());
        geckoButton.setTooltip(new Tooltip(geckoPlugin.getDescription()));
        geckoButton.setOnAction(e -> geckoPlugin.show());
        geckoButton.setFocusTraversable(focusTraversable);
        geckoPlugin.setAnchor(geckoButton);
        hbox.getChildren().add(geckoButton);
        PriceAlertPlugin cpPlugin = new PriceAlertPluginCryptoPanic(baseCurrency);
        Button cpButton = new Button(cpPlugin.getButtonTitle());
        cpButton.getStyleClass().add("button-small");
        cpButton.getStyleClass().add(cpPlugin.getButtonCssClass());
        cpButton.setTooltip(new Tooltip(cpPlugin.getDescription()));
        cpButton.setOnAction(e -> cpPlugin.show());
        cpButton.setFocusTraversable(focusTraversable);
        cpPlugin.setAnchor(cpButton);
        hbox.getChildren().add(cpButton);
    }

}
