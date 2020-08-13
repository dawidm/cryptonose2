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
import java.util.ListIterator;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import pl.dmotyka.cryptonose2.PriceAlert;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;

/**
 * Created by dawid on 9/3/17.
 */
public class CryptonoseGuiPriceAlertsTabController implements Initializable{

    public VBox mainVBox;

    private int maxAlerts = 30;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addAlert(PriceAlert priceAlert, ChartCandle[] chartCandles) {
        javafx.application.Platform.runLater(() -> {
            if (mainVBox.getChildren().size() == 1) {
                mainVBox.getChildren().clear();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiPriceAlertPaneLabels.fxml"));
                try {
                    Node priceAlertPaneLabels = fxmlLoader.load();
                    mainVBox.getChildren().add(priceAlertPaneLabels);
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiPriceAlertPane.fxml"));
            try {
                Node alertPane = fxmlLoader.load();
                ((CryptonoseGuiPriceAlertNodeController) fxmlLoader.getController()).fillPane(priceAlert, chartCandles);
                mainVBox.getChildren().listIterator(1).add(alertPane);
                //iterator for outdated alerts
                if (mainVBox.getChildren().size() > maxAlerts + 1) {
                    ListIterator listIterator = mainVBox.getChildren().listIterator(maxAlerts + 1);
                    while (listIterator.hasNext()) {
                        listIterator.next();
                        listIterator.remove();
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
        });
    }

    public void setMaxAlerts(int maxAlerts) {
        this.maxAlerts=maxAlerts;
    }


}
