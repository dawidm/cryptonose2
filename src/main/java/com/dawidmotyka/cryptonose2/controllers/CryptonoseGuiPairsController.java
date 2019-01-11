package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
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

    public class PairsListItem {
        private final BooleanProperty selectedBooleanProperty=new SimpleBooleanProperty(false);
        private CurrencyPair currencyPair;

        public PairsListItem(boolean selected, CurrencyPair currencyPair) {
            selectedBooleanProperty.setValue(selected);
            this.currencyPair=currencyPair;
        }

        public boolean isSelected() {
            return selectedBooleanProperty.get();
        }

        public void setSelected(boolean selectedBooleanProperty) {
            this.selectedBooleanProperty.set(selectedBooleanProperty);
        }

        public BooleanProperty selectedBooleanPropertyProperty() {
            return selectedBooleanProperty;
        }

        public CurrencyPair getCurrencyPair() {
            return currencyPair;
        }

        @Override
        public String toString() {
            return currencyPair.toString();
        }
    }

    public static final Logger logger = Logger.getLogger(CryptonoseGuiPairsController.class.getName());


    @FXML
    public HBox mainHBox;
    @FXML
    public GridPane loadingGridPane;
    @FXML
    public TableView minVolumeTableView;
    @FXML
    public VBox choosePairsVBox;
    @FXML
    public ListView<PairsListItem> currencyPairsListView;
    @FXML
    public TextField filterTextField;

    private ExchangeSpecs exchangeSpecs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainHBox.setVisible(false);
        loadingGridPane.setVisible(true);
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
                fillList(currencyPairList);
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
        activeTableColumn.setPrefWidth(10.0);
        activeTableColumn.setEditable(true);
        activeTableColumn.setCellValueFactory(tableItem->tableItem.getValue().getActive());
        activeTableColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
        activeTableColumn.setOnEditCommit(event -> event.getRowValue().setActive(event.getNewValue()));
        TableColumn<CounterCurrencyTableItem,String> marketTableColumn = new TableColumn("Market");
        marketTableColumn.setPrefWidth(20.0);
        marketTableColumn.setCellValueFactory(tableItem->tableItem.getValue().getName());
        marketTableColumn.setEditable(false);
        TableColumn<CounterCurrencyTableItem,String> minVolTableColumn = new TableColumn("Min 24h volume");
        minVolTableColumn.setPrefWidth(20.0);
        minVolTableColumn.setCellValueFactory(tableItem -> tableItem.getValue().getMinVolume().asString());
        minVolTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        minVolTableColumn.setOnEditCommit(value -> {
            try {
                double newVal = Double.parseDouble(value.getNewValue());
                if (newVal > 0) {
                    value.getRowValue().setMinVolume(newVal);
                } else
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                minVolumeTableView.refresh();
            }
        });
        minVolTableColumn.setEditable(true);
        minVolumeTableView.getColumns().addAll(activeTableColumn,marketTableColumn,minVolTableColumn);
        minVolumeTableView.setItems(tableItemObservableList);
        minVolumeTableView.setEditable(true);
    }

    public void fillList(List<CurrencyPair> currencyPairList) {
        ObservableList<PairsListItem> pairsObservableList = FXCollections.observableArrayList();
        FilteredList<PairsListItem> filteredPairsList = new FilteredList<>(pairsObservableList);
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> filteredPairsList.setPredicate(pairsListItem -> pairsListItem.toString().toUpperCase().contains(newValue.toUpperCase())));
        for(CurrencyPair currentCurrencyPair : currencyPairList) {
            pairsObservableList.add(new PairsListItem(false,currentCurrencyPair));
        }
        currencyPairsListView.setCellFactory(CheckBoxListCell.forListView(param -> param.selectedBooleanProperty));
        currencyPairsListView.setItems(filteredPairsList);
    }

    public void selectVisibleClick() {
        for(PairsListItem pairsListItem : currencyPairsListView.getItems()) {
            pairsListItem.setSelected(true);
        }
    }

    public void deselectAllClick() {
        for(PairsListItem pairsListItem : (ObservableList<PairsListItem>)((FilteredList)currencyPairsListView.getItems()).getSource()) {
            pairsListItem.setSelected(false);
        }
    }

}
