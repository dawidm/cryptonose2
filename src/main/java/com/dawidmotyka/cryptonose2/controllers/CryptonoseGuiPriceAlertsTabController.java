package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.cryptonose2.PriceAlert;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ListIterator;
import java.util.ResourceBundle;

/**
 * Created by dawid on 9/3/17.
 */
public class CryptonoseGuiPriceAlertsTabController implements Initializable{

    public VBox mainVBox;

    private int maxAlerts = 30;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addAlert(PriceAlert priceAlert) {
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
                ((CryptonoseGuiPriceAlertNodeController) fxmlLoader.getController()).fillPane(priceAlert);

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
