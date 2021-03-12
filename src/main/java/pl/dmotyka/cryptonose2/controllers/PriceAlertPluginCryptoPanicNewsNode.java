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

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import pl.dmotyka.cryptonose2.cryptopanic.CryptoPanicNews;

public class PriceAlertPluginCryptoPanicNewsNode implements Initializable {

    public static final Logger logger = Logger.getLogger(PriceAlertPluginCryptoPanicNewsNode.class.getName());

    @FXML
    public Hyperlink titleHyperlink;
    @FXML
    public Label timeLabel;
    @FXML
    public Label reactionsLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void init(CryptoPanicNews news) {
        String title;
        if (news.getTitle().length() > 100) {
            Tooltip tooltip = new Tooltip(news.getTitle());
            tooltip.setWrapText(true);
            titleHyperlink.setTooltip(tooltip);
            title = news.getTitle().substring(0, 100) + "...";
        }
        else
            title = news.getTitle();
        titleHyperlink.setText(title);
        titleHyperlink.setOnMouseClicked(e -> CryptonoseGuiBrowser.runBrowser(news.getLink()));
        timeLabel.setText(formatTimeAgo(news.getPublished()) + " ago");
        reactionsLabel.setText(reactionsLabel.getText() + news.getNumReactions());
    }

    public static String formatTimeAgo(Instant instant) {
        Instant currentInstant = Instant.now();
        long secsPassed = currentInstant.getEpochSecond() - instant.getEpochSecond();
        if (secsPassed > 60 * 60 * 24) // more than a day
            return String.format("%dd",secsPassed / (60 * 60 * 24));
        if (secsPassed > 60 * 60) // more than a hour
            return String.format("%dh",secsPassed / (60 * 60));
        if (secsPassed > 60) // more than a minute
            return String.format("%dm",secsPassed / (60));
        return String.format("%ds",secsPassed);
    }
}
