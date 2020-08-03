/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CryptonoseGuiAddExchangeController implements Initializable {

    private static final Logger logger = Logger.getLogger(CryptonoseGuiAddExchangeController.class.getName());

    @FXML
    public javafx.scene.control.ListView<Object> exchangeListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void closeClick() {closeWindow();}

    public void init(CryptonoseGuiController cryptonoseGuiController, ExchangeSpecs[] exchangeSpecs) {
        if(exchangeSpecs.length==0) {
            exchangeListView.setItems(FXCollections.observableList(Collections.singletonList("(All exchanges are active)")));
            exchangeListView.setSelectionModel(null);
            exchangeListView.refresh();
            return;
        }
        ObservableList<Object> itemsObservableList = FXCollections.observableList(new ArrayList<>());
        exchangeListView.setItems(itemsObservableList);
        itemsObservableList.addAll(Arrays.asList(exchangeSpecs));
        exchangeListView.setOnMouseClicked(event -> {
            if(event.getClickCount()==1) {
                ExchangeSpecs selectedExchangeSpecs=(ExchangeSpecs)exchangeListView.getSelectionModel().getSelectedItem();
                itemsObservableList.remove(selectedExchangeSpecs);
                exchangeListView.getSelectionModel().select(null);
                cryptonoseGuiController.loadExchange(selectedExchangeSpecs,true);
                closeWindow();
            }
        });
    }

    private void closeWindow() {((Stage)exchangeListView.getScene().getWindow()).close();}

}
