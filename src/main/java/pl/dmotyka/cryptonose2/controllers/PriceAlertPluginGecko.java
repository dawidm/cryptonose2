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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import pl.dmotyka.cryptonose2.PriceAlertPlugin;
import pl.dmotyka.cryptonose2.UILoader;
import pl.dmotyka.cryptonose2.coingecko.GeckoApi;
import pl.dmotyka.cryptonose2.coingecko.GeckoCurrencyInfo;
import pl.dmotyka.cryptonose2.coingecko.GeckoNoSuchSymbolException;

public class PriceAlertPluginGecko extends PriceAlertPlugin implements Initializable {

    public static final Logger logger = Logger.getLogger(PriceAlertPluginGecko.class.getName());

    @FXML
    public VBox dataContainer;
    @FXML
    public AnchorPane errorContainer;
    @FXML
    public Label errorLabel;
    @FXML
    public Label nameLabel;
    @FXML
    public Label symbolLabel;
    @FXML
    public Label titleLabel;
    @FXML
    public Label dailyChangeLabel;
    @FXML
    public Label weeklyChangeLabel;
    @FXML
    public Label marketCapLabel;
    @FXML
    public Label rankLabel;
    @FXML
    public Label volumeLabel;

    private static final String NAME = "CoinGecko";
    private static final String TITLE = "...";
    private final AtomicBoolean isShowingAtomicBoolean = new AtomicBoolean(false);

    public PriceAlertPluginGecko(String currencySymbol) {
        super(NAME, TITLE, currencySymbol);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataContainer.visibleProperty().bind(errorContainer.visibleProperty().not());
    }

    @Override
    public void show() {
        if (anchor == null)
            throw new IllegalStateException("Anchor should be set before showing, use setAnchor()");
        try {
            UILoader<PriceAlertPluginGecko> uiLoader = new UILoader<>("priceAlertPluginGecko.fxml", this);
            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.setAutoFix(true);
            popup.getContent().add(uiLoader.getRoot());
            popup.showingProperty().addListener((observable, oldValue, newValue) -> {
                isShowingAtomicBoolean.set(newValue);
            });
            titleLabel.setText(currencySymbol + titleLabel.getText());
            Point2D location = anchor.localToScreen(0,0);
            popup.show(anchor, location.getX(), location.getY());
            new Thread(() -> {
                try {
                    GeckoCurrencyInfo info = GeckoApi.getInfoBySymbol(currencySymbol);
                    Platform.runLater(() -> {
                        nameLabel.setText(info.getName());
                        symbolLabel.setText(info.getSymbol().toUpperCase());
                        dailyChangeLabel.setText(String.format("%.2f%%", info.getDayChange()));
                        weeklyChangeLabel.setText(String.format("%.2f%%", info.getWeekChange()));
                        marketCapLabel.setText(String.format("$%,d", (int)info.getMarketCapUSD()));
                        rankLabel.setText(""+info.getGeckoMarketCapRank());
                        volumeLabel.setText(String.format("$%,d",(int)info.getDayVolume()));
                    });
                } catch (GeckoNoSuchSymbolException | IOException e) {
                    logger.log(Level.WARNING, "when getting gecko data for " + currencySymbol, e);
                    Platform.runLater(() -> {
                        errorContainer.setVisible(true);
                    });
                }
            }).start();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load fxml");
        }
    }

    @Override
    public boolean isShowing() {
        return false;
    }
}
