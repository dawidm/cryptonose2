package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptonoseGuiPairsController implements Initializable {

    public static final Logger logger = Logger.getLogger(CryptonoseGuiPairsController.class.getName());


    @FXML
    public HBox mainHBox;
    @FXML
    public GridPane loadingGridPane;
    @FXML
    public RadioButton minVolumeRadioButton;
    @FXML
    public RadioButton choosePairsRadioButton;
    @FXML
    public TableView minVolumeTableView;
    @FXML
    public VBox choosePairsVBox;

    private ExchangeSpecs exchangeSpecs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainHBox.setVisible(false);
        loadingGridPane.setVisible(true);
        radioButtonClick();
    }

    public void radioButtonClick() {
        if(minVolumeRadioButton.isSelected()==true) {
            minVolumeTableView.setDisable(false);
            choosePairsVBox.setDisable(true);
        } else {
            minVolumeTableView.setDisable(true);
            choosePairsVBox.setDisable(false);
        }
    }

    public void setExchange(ExchangeSpecs exchange) {
        this.exchangeSpecs=exchange;
        new Thread(this::loadPairs).start();
    }

    public void loadPairs() {
        try {
            Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecs.getXchangeExchange());
            List<CurrencyPair> currencyPairList = exchange.getExchangeSymbols();
            Set<Currency> baseCurrencies = new HashSet<>();
            for(CurrencyPair currentCurrencyPair: currencyPairList)
                baseCurrencies.add(currentCurrencyPair.counter);
            Arrays.stream(baseCurrencies.toArray(new Currency[baseCurrencies.size()])).forEach(currency -> System.out.println(currency.getCurrencyCode()));
            Platform.runLater(()->{
                mainHBox.setVisible(true);
                loadingGridPane.setVisible(false);
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE,"when loading pairs from exchange",e);
        }

    }

}
