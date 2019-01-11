package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class CryptonoseGuiPairsController implements Initializable {

    public static final double DEFAULT_MIN_VOLUME = 200;

    public static class MarketTableItem {

        private final SimpleBooleanProperty active = new SimpleBooleanProperty();
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleDoubleProperty minVolume = new SimpleDoubleProperty();

        public MarketTableItem(boolean active, String name, double minVolume) {
            this.active.set(active);
            this.name.set(name);
            this.minVolume.set(minVolume);
        }

        public boolean isActive() {
            return active.get();
        }

        public SimpleBooleanProperty activeProperty() {
            return active;
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public double getMinVolume() {
            return minVolume.get();
        }

        public SimpleDoubleProperty minVolumeProperty() {
            return minVolume;
        }

        public void setActive(boolean active) {
            this.active.set(active);
        }

        public void setMinVolume(double minVolume) {
            this.minVolume.set(minVolume);
        }
    }

    public class PairListItem {
        private final BooleanProperty selectedBooleanProperty=new SimpleBooleanProperty(false);
        private CurrencyPair currencyPair;

        public PairListItem(boolean selected, CurrencyPair currencyPair) {
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
    public ListView<PairListItem> currencyPairsListView;
    @FXML
    public TextField filterTextField;
    @FXML
    public Button saveButton;

    private ExchangeSpecs exchangeSpecs;
    private ObservableList<MarketTableItem> marketsObservableList;
    private ObservableList<PairListItem> pairsObservableList;
    private Future loadPairsFuture;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainHBox.setVisible(false);
        loadingGridPane.setVisible(true);
    }

    public void setExchange(ExchangeSpecs exchange) {
        this.exchangeSpecs=exchange;
        loadPairsFuture=Executors.newSingleThreadExecutor().submit(this::loadPairs);
    }

    public void loadPairs() {
        try {
            Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecs.getXchangeExchange());
            List<CurrencyPair> currencyPairList = exchange.getExchangeSymbols();
            if(Thread.interrupted())
                return;
            Platform.runLater(()->{
                mainHBox.setVisible(true);
                loadingGridPane.setVisible(false);
                fillTable(currencyPairList);
                fillList(currencyPairList);
                loadPreferences();
                saveButton.setDisable(false);
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE,"when loading pairs from exchange",e);
        }

    }

    public void fillTable(List<CurrencyPair> currencyPairsList) {
        Set<Currency> baseCurrencies = new HashSet<>();
        for(CurrencyPair currentCurrencyPair: currencyPairsList)
            baseCurrencies.add(currentCurrencyPair.counter);
        marketsObservableList = FXCollections.observableArrayList();
        for(Currency currency : baseCurrencies) {
            marketsObservableList.add(new MarketTableItem(false,currency.getSymbol(),DEFAULT_MIN_VOLUME));
        }
        minVolumeTableView.getColumns().clear();
        TableColumn<MarketTableItem,Boolean>  activeTableColumn = new TableColumn("Active");
        activeTableColumn.setPrefWidth(10.0);
        activeTableColumn.setEditable(true);
        activeTableColumn.setCellValueFactory(tableItem->tableItem.getValue().activeProperty());
        activeTableColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
        activeTableColumn.setOnEditCommit(event -> event.getRowValue().setActive(event.getNewValue()));
        TableColumn<MarketTableItem,String> marketTableColumn = new TableColumn("Market");
        marketTableColumn.setPrefWidth(20.0);
        marketTableColumn.setCellValueFactory(tableItem->tableItem.getValue().nameProperty());
        marketTableColumn.setEditable(false);
        TableColumn<MarketTableItem,String> minVolTableColumn = new TableColumn("Min 24h volume");
        minVolTableColumn.setPrefWidth(20.0);
        minVolTableColumn.setCellValueFactory(tableItem -> tableItem.getValue().minVolumeProperty().asString());
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
        minVolumeTableView.setItems(marketsObservableList);
        minVolumeTableView.setEditable(true);
    }

    public void fillList(List<CurrencyPair> currencyPairList) {
        pairsObservableList = FXCollections.observableArrayList();
        FilteredList<PairListItem> filteredPairsList = new FilteredList<>(pairsObservableList);
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> filteredPairsList.setPredicate(pairListItem -> pairListItem.toString().toUpperCase().contains(newValue.toUpperCase())));
        for(CurrencyPair currentCurrencyPair : currencyPairList) {
            pairsObservableList.add(new PairListItem(false,currentCurrencyPair));
        }
        currencyPairsListView.setCellFactory(CheckBoxListCell.forListView(param -> param.selectedBooleanProperty));
        currencyPairsListView.setItems(filteredPairsList);
    }

    public void loadPreferences() {
        Preferences preferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("pairsPreferences").node(exchangeSpecs.getName());
        String markets = preferences.get("markets","");
        if(!markets.equals("")) {
            Arrays.stream(markets.split(",")).forEach(market -> {
                Optional<MarketTableItem> optionalMarketTableItem = marketsObservableList.stream().filter(marketTableItem -> marketTableItem.getName().equals(market)).findAny();
                if(optionalMarketTableItem.isPresent()) {
                    MarketTableItem marketTableItem = optionalMarketTableItem.get();
                    marketTableItem.setActive(true);
                    marketTableItem.setMinVolume(preferences.getDouble(market,DEFAULT_MIN_VOLUME));
                }
            });
        }
        Set<String> apiSymbolsSet = new HashSet<>(Arrays.asList(preferences.get("pairsApiSymbols","").split(",")));
        for(PairListItem pairListItem : pairsObservableList) {
            if(apiSymbolsSet.contains(PairSymbolConverter.toApiSymbol(exchangeSpecs,pairListItem.getCurrencyPair())))
                pairListItem.setSelected(true);
        }
    }

    public void savePreferences() {
        Preferences preferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("pairsPreferences").node(exchangeSpecs.getName());
        preferences.put("markets",
                marketsObservableList.stream().
                        filter(marketTableItem -> marketTableItem.isActive()).
                        map(marketTableItem -> marketTableItem.getName()).
                        collect(Collectors.joining(","))
        );
        marketsObservableList.stream().
                filter(marketTableItem -> marketTableItem.isActive()).
                forEach(marketTableItem -> preferences.putDouble(marketTableItem.getName(),marketTableItem.getMinVolume()));
        preferences.put("pairsApiSymbols",
                pairsObservableList.stream().
                        filter(pairListItem -> pairListItem.isSelected()).
                        map(pairListItem -> PairSymbolConverter.toApiSymbol(exchangeSpecs, pairListItem.getCurrencyPair())).
                        collect(Collectors.joining(","))
        );
    }

    public void selectVisibleClick() {
        for(PairListItem pairListItem : currencyPairsListView.getItems()) {
            pairListItem.setSelected(true);
        }
    }

    public void deselectAllClick() {
        for(PairListItem pairListItem : (ObservableList<PairListItem>)((FilteredList)currencyPairsListView.getItems()).getSource()) {
            pairListItem.setSelected(false);
        }
    }

    public void saveClick() {
        savePreferences();
        closeStage();
    }

    public void cancelClick() {
        if(loadPairsFuture!=null)
            loadPairsFuture.cancel(true);
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage)mainHBox.getScene().getWindow();
        stage.close();
    }

}
