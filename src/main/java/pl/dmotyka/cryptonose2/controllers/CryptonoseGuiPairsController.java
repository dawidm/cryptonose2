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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
import javafx.stage.Stage;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import pl.dmotyka.cryptonose2.dataobj.MarketAndVolume;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.pairdataprovider.PairDataProvider;
import pl.dmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

public class CryptonoseGuiPairsController implements Initializable {

    private static final Map<String, Double> DEF_MIN_VOLUMES = Map.of(
            "BTC", 200.0,
            "ETH", 1000.0,
            "BNB", 10000.0,
            "EUR", 10000000.0,
            "USD", 10000000.0,
            "USDT", 10000000.0,
            "DAI", 10000000.0,
            "USDC", 10000000.0,
            "BUSD", 10000000.0,
            "TUSD", 10000000.0
    );
    private static final double DEF_OTHER_MIN_VOL = 100.0;

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

    public static class PairListItem {
        private final PairSymbolConverter pairSymbolConverter;
        private final BooleanProperty selectedBooleanProperty=new SimpleBooleanProperty(false);
        private final CurrencyPair currencyPair;

        public PairListItem(boolean selected, CurrencyPair currencyPair, PairSymbolConverter pairSymbolConverter) {
            this.pairSymbolConverter = pairSymbolConverter;
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
            return pairSymbolConverter.toFormattedString(pairSymbolConverter.toApiSymbol(currencyPair));
        }
    }

    public static final Logger logger = Logger.getLogger(CryptonoseGuiPairsController.class.getName());


    @FXML
    public HBox mainHBox;
    @FXML
    public GridPane loadingGridPane;
    @FXML
    public TableView<MarketTableItem> minVolumeTableView;
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
    private Future<?> loadPairsFuture;
    private SettingsChangedListener settingsChangedListener;
    private PairSymbolConverter pairSymbolConverter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainHBox.setVisible(false);
        loadingGridPane.setVisible(true);
    }

    public void init(ExchangeSpecs exchange, SettingsChangedListener settingsChangedListener) {
        this.exchangeSpecs=exchange;
        this.settingsChangedListener = settingsChangedListener;
        loadPairsFuture=Executors.newSingleThreadExecutor().submit(this::loadPairs);
    }

    public void loadPairs() {
        try {
            PairDataProvider pairDataProvider = exchangeSpecs.getPairDataProvider();
            pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
            List<CurrencyPair> currencyPairList = new ArrayList<>();
            for (String apiSymbol : pairDataProvider.getPairsApiSymbols()) {
                try {
                    currencyPairList.add(pairSymbolConverter.apiSymbolToXchangeCurrencyPair(apiSymbol));
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "when loading currency pairs", e);
                }
            }

            if(Thread.interrupted())
                return;

            String markets = CryptonoseSettings.getString(CryptonoseSettings.Pairs.MARKETS, exchangeSpecs);
            MarketAndVolume[] marketsVolumes = Arrays.stream(markets.split(",")).map(market -> {
                double volume = CryptonoseSettings.getDouble(new CryptonoseSettings.MarketVolumePreference(market),exchangeSpecs);
                return new MarketAndVolume(market, volume);
            }).toArray(MarketAndVolume[]::new);
            String selectedApiSymbolsString = CryptonoseSettings.getString(CryptonoseSettings.Pairs.PAIRS_API_SYMBOLS, exchangeSpecs);
            String[] selectedApiSymbols;
            if (selectedApiSymbolsString.trim().isEmpty()) {
                selectedApiSymbols = new String[]{};
            } else {
                selectedApiSymbols = selectedApiSymbolsString.split(",");
            }

            Platform.runLater(()->{
                mainHBox.setVisible(true);
                loadingGridPane.setVisible(false);
                fillTable(currencyPairList);
                fillList(currencyPairList);
                fillPreferencesData(marketsVolumes, selectedApiSymbols);
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
            String currencyCode = currency.getCurrencyCode();
            double defMinVol = DEF_MIN_VOLUMES.getOrDefault(currencyCode, DEF_OTHER_MIN_VOL);
            marketsObservableList.add(new MarketTableItem(false, currencyCode, defMinVol));
        }
        marketsObservableList.sort(Comparator.comparing(MarketTableItem::getName));
        minVolumeTableView.getColumns().clear();
        TableColumn<MarketTableItem,Boolean> activeTableColumn = new TableColumn<>("Active");
        activeTableColumn.maxWidthProperty().bind(minVolumeTableView.widthProperty().multiply(0.2)); // because max width determines column sizes in CONSTRAINED_RESIZE_POLICY
        activeTableColumn.setEditable(true);
        activeTableColumn.setCellValueFactory(tableItem->tableItem.getValue().activeProperty());
        activeTableColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
        activeTableColumn.setOnEditCommit(event -> event.getRowValue().setActive(event.getNewValue()));
        TableColumn<MarketTableItem,String> marketTableColumn = new TableColumn<>("Market");
        marketTableColumn.maxWidthProperty().bind(minVolumeTableView.widthProperty().multiply(0.3));
        marketTableColumn.setCellValueFactory(tableItem->tableItem.getValue().nameProperty());
        marketTableColumn.setEditable(false);
        TableColumn<MarketTableItem,String> minVolTableColumn = new TableColumn<>("Min 24h volume");
        minVolTableColumn.maxWidthProperty().bind(minVolumeTableView.widthProperty().multiply(0.5));
        minVolTableColumn.setCellValueFactory(tableItem -> tableItem.getValue().minVolumeProperty().asString("%.2f"));
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
        minVolumeTableView.getFocusModel().focusedCellProperty().addListener((observable, oldPos, newPos) -> {
            if (newPos != null) {
                if (newPos.getTableColumn().isEditable()) {
                    Platform.runLater(() -> minVolumeTableView.edit(newPos.getRow(), minVolumeTableView.getColumns().get(newPos.getColumn())));
                }
            }
        });
    }

    public void fillList(List<CurrencyPair> currencyPairList) {
        pairsObservableList = FXCollections.observableArrayList();
        FilteredList<PairListItem> filteredPairsList = new FilteredList<>(pairsObservableList);
        SortedList<PairListItem> sortedPairsList = new SortedList<>(filteredPairsList, Comparator.comparing(PairListItem::toString));
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> filteredPairsList.setPredicate(pairListItem -> pairListItem.toString().toUpperCase().contains(newValue.toUpperCase())));
        for(CurrencyPair currentCurrencyPair : currencyPairList) {
            pairsObservableList.add(new PairListItem(false,currentCurrencyPair, pairSymbolConverter));
        }
        currencyPairsListView.setCellFactory(CheckBoxListCell.forListView(param -> param.selectedBooleanProperty));
        currencyPairsListView.setItems(sortedPairsList);
    }

    public void fillPreferencesData(MarketAndVolume[] marketAndVolumes, String[] selectedPairs) {
        PairSymbolConverter pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
        for (MarketAndVolume marketAndVolume : marketAndVolumes) {
            Optional<MarketTableItem> optionalMarketTableItem = marketsObservableList.stream().filter(marketTableItem -> marketTableItem.getName().equals(marketAndVolume.getMarketSymbol())).findAny();
            if(optionalMarketTableItem.isPresent()) {
                MarketTableItem marketTableItem = optionalMarketTableItem.get();
                marketTableItem.setActive(true);
                marketTableItem.setMinVolume(marketAndVolume.getMarketVolume());
            }
        }
        Set<String> selectedApiSymbolsSet = Set.of(selectedPairs);
        for(PairListItem pairListItem : pairsObservableList) {
            if(selectedApiSymbolsSet.contains(pairSymbolConverter.toApiSymbol(pairListItem.getCurrencyPair())))
                pairListItem.setSelected(true);
        }
    }

    public void savePreferences() {
        final PairSymbolConverter pairSymbolConverter = exchangeSpecs.getPairSymbolConverter();
        String markets = marketsObservableList.stream().
                filter(MarketTableItem::isActive).
                map(MarketTableItem::getName).
                collect(Collectors.joining(","));
        CryptonoseSettings.putString(CryptonoseSettings.Pairs.MARKETS, markets, exchangeSpecs);

        marketsObservableList.stream().
                filter(MarketTableItem::isActive).
                forEach(marketTableItem -> {
                    CryptonoseSettings.putDouble(new CryptonoseSettings.MarketVolumePreference(marketTableItem.getName()), marketTableItem.getMinVolume(), exchangeSpecs);
                });
        String apiSymbols = pairsObservableList.stream().
                filter(PairListItem::isSelected).
                map(pairListItem -> pairSymbolConverter.toApiSymbol(pairListItem.getCurrencyPair())).
                collect(Collectors.joining(","));
        CryptonoseSettings.putString(CryptonoseSettings.Pairs.PAIRS_API_SYMBOLS, apiSymbols, exchangeSpecs);
    }

    public void selectVisibleClick() {
        for(PairListItem pairListItem : currencyPairsListView.getItems()) {
            pairListItem.setSelected(true);
        }
    }

    public void deselectAllClick() {
        for(PairListItem pairListItem : pairsObservableList) {
            pairListItem.setSelected(false);
        }
    }

    public void saveClick() {
        savePreferences();
        settingsChangedListener.notifySettingsChanged();
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
