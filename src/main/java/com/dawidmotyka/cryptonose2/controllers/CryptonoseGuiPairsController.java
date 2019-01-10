package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptonoseGuiPairsController implements Initializable {

    public static final double DEFAULT_MIN_VOLUME = 200;

    public static class CounterCurrencyTableItem {

        private final SimpleBooleanProperty active = new SimpleBooleanProperty();
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleDoubleProperty minVolume = new SimpleDoubleProperty();

        public CounterCurrencyTableItem(boolean active, String name, double minVolume) {
            this.active.set(active);
            this.name.set(name);
            this.minVolume.set(minVolume);
        }

        public SimpleBooleanProperty getActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active.set(active);
        }

        public SimpleStringProperty getName() {
            return name;
        }

        public SimpleDoubleProperty getMinVolume() {
            return minVolume;
        }

        public void setMinVolume(double minVolume) {
            this.minVolume.set(minVolume);
        }
    }

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
            Platform.runLater(()->{
                mainHBox.setVisible(true);
                loadingGridPane.setVisible(false);
                fillTable(currencyPairList);
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE,"when loading pairs from exchange",e);
        }

    }

    public void fillTable(List<CurrencyPair> currencyPairsList) {
        Set<Currency> baseCurrencies = new HashSet<>();
        for(CurrencyPair currentCurrencyPair: currencyPairsList)
            baseCurrencies.add(currentCurrencyPair.counter);
        ObservableList<CounterCurrencyTableItem> tableItemObservableList = FXCollections.observableArrayList();
        for(Currency currency : baseCurrencies) {
            tableItemObservableList.add(new CounterCurrencyTableItem(false,currency.getSymbol(),DEFAULT_MIN_VOLUME));
        }
        minVolumeTableView.getColumns().clear();
        TableColumn<CounterCurrencyTableItem,Boolean>  activeTableColumn = new TableColumn("Active");
        activeTableColumn.setPrefWidth(10);
        activeTableColumn.setEditable(true);
        activeTableColumn.setCellValueFactory(tableItem->tableItem.getValue().getActive());
        activeTableColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
        activeTableColumn.setOnEditCommit(event -> event.getRowValue().setActive(event.getNewValue()));
        TableColumn<CounterCurrencyTableItem,String> marketTableColumn = new TableColumn("Market");
        marketTableColumn.setPrefWidth(20);
        marketTableColumn.setCellValueFactory(tableItem->tableItem.getValue().getName());
        marketTableColumn.setEditable(false);
        TableColumn<CounterCurrencyTableItem,String> minVolTableColumn = new TableColumn("Min volume");
        minVolTableColumn.setPrefWidth(20);
        minVolTableColumn.setCellValueFactory(tableItem -> tableItem.getValue().getMinVolume().asString());
        minVolTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        minVolTableColumn.setOnEditCommit(value -> {
            if (value.getNewValue().matches("\\d*")) {
                double newVal = Double.parseDouble(value.getNewValue());
                if(newVal>0)
                    value.getRowValue().setMinVolume(newVal);
            } minVolumeTableView.refresh();
        });
        minVolTableColumn.setEditable(true);
        minVolumeTableView.getColumns().addAll(activeTableColumn,marketTableColumn,minVolTableColumn);
        minVolumeTableView.setItems(tableItemObservableList);
        minVolumeTableView.setEditable(true);
    }

}
