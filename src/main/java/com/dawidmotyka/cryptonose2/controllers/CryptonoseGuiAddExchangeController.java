package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
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
        itemsObservableList.addAll(exchangeSpecs);
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
