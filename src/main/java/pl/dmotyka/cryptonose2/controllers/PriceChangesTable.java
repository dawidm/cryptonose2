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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import pl.dmotyka.cryptonose2.dataobj.CryptonosePairData;
import pl.dmotyka.cryptonose2.tools.CoinPluginConverter;
import pl.dmotyka.exchangeutils.tools.TimeConverter;

public class PriceChangesTable {

    private static final long TABLE_SORT_FREQUENCY_MILLIS = 2500;

    private final long[] timePeriods;
    private final TableView<CryptonosePairData> tableView;
    private final SortedList<CryptonosePairData> items;

    private boolean enableShowExchange = false;
    private boolean pinnedCheckboxes = false;
    private boolean buttonsFocusTraversable = true;
    private boolean autoSort = false;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    static class PriceChangesTableCell extends TableCell<CryptonosePairData,Number> {

        boolean firstUpdate = true;
        double lastVal = 0.0;

        @Override
        protected void updateItem(Number item, boolean empty) {
            super.updateItem(item, empty);
            if(empty) {
                setText(null);
            }
            if (firstUpdate) {
                getStyleClass().add("price-rising");
                firstUpdate = false;
            }
            if(item!=null) {
                if (lastVal*item.doubleValue() <= 0) {
                    getStyleClass().remove(getStyleClass().size()-1);
                    if (item.doubleValue() >= 0)
                        getStyleClass().add("price-rising");
                    else
                        getStyleClass().add("price-falling");
                }
                setText(String.format("%.2f", item));
                lastVal = item.doubleValue();
            } else {
                setText(null);
            }
        }
    }
    static class PriceTableCell extends TableCell<CryptonosePairData,Number> {
        @Override
        protected void updateItem(Number item, boolean empty) {
            super.updateItem(item, empty);
            if(empty)
                setText(null);
            if(item==null)
                setText(null);
            else {
                if (item.doubleValue() == 0) {
                    setText("no data");
                } else {
                    setText(DecimalFormatter.formatDecimalPrice(item.doubleValue()));
                }
            }
        }
    }

    public PriceChangesTable(TableView<CryptonosePairData> tableView, SortedList<CryptonosePairData> items, long[] timePeriods) {
        this.tableView = tableView;
        this.items = items;
        this.timePeriods = timePeriods;
    }

    // add columns and cell factories (including price alert plugins), add listener for double click
    public void init() {
        tableView.getColumns().clear();
        TableColumn<CryptonosePairData,String> pairNameCol = new TableColumn("Pair name");
        pairNameCol.setCellValueFactory(cellDataFeatures -> new SimpleStringProperty(cellDataFeatures.getValue().getFormattedPairName()));
        pairNameCol.setMaxWidth(Integer.MAX_VALUE * 0.2);
        TableColumn<CryptonosePairData,Number> lastPriceCol = new TableColumn("Last price");
        lastPriceCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().lastPriceProperty());
        lastPriceCol.setCellFactory(col -> new PriceTableCell());
        lastPriceCol.setMaxWidth(Integer.MAX_VALUE * 0.2);
        TableColumn<CryptonosePairData,Number> p1ChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(timePeriods[0]) +" % change");
        p1ChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p1PercentChangeProperty());
        p1ChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p1ChangeCol.setMaxWidth(Integer.MAX_VALUE * 0.12);
        TableColumn<CryptonosePairData,Number> p1RelativeChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(timePeriods[0])+" relative");
        p1RelativeChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p1RelativeChangeProperty());
        p1RelativeChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p1RelativeChangeCol.setMaxWidth(Integer.MAX_VALUE * 0.12);
        TableColumn<CryptonosePairData,Number> p2ChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(timePeriods[1])+" % change");
        p2ChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p2PercentChangeProperty());
        p2ChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p2ChangeCol.setMaxWidth(Integer.MAX_VALUE * 0.12);
        TableColumn<CryptonosePairData,Number> p2RelativeChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(timePeriods[1])+" relative");
        p2RelativeChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p2RelativeChangeProperty());
        p2RelativeChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p2RelativeChangeCol.setMaxWidth(Integer.MAX_VALUE * 0.12);
        TableColumn<CryptonosePairData, Void> buttonsCol = new TableColumn("More");
        buttonsCol.setMaxWidth(Integer.MAX_VALUE * 0.12);
        buttonsCol.setCellFactory(col -> {
            TableCell<CryptonosePairData, Void> tableCell = new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        this.setText(null);
                        CryptonosePairData cnPairData = tableView.getItems().get(getIndex());
                        HBox hbox = new HBox();
                        hbox.setAlignment(Pos.CENTER);
                        PriceAlertPluginsButtons buttons = new PriceAlertPluginsButtons();
                        buttons.install(hbox, CoinPluginConverter.apiSymbolToPluginSymbol(cnPairData.getPairName(), cnPairData.getExchangeSpecs()), buttonsFocusTraversable);
                        this.setGraphic(hbox);
                    } else {
                        this.setText(null);
                        this.setGraphic(null);
                    }
                }
            };
            return tableCell;
        });
        if (pinnedCheckboxes) {
            TableColumn<CryptonosePairData,Boolean> pinCol = new TableColumn("Pin");
            pinCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().pinnedProperty());
            pinCol.setCellFactory(tc -> new CheckBoxTableCell<>());
            pinCol.setMaxWidth(Integer.MAX_VALUE * 0.05);
            pinCol.setEditable(true);
            tableView.getColumns().add(pinCol);
        }
        if (enableShowExchange) {
            TableColumn<CryptonosePairData,String> exchangeCol = new TableColumn("Exchange");
            exchangeCol.setCellValueFactory(cellDataFeatures -> new SimpleStringProperty(cellDataFeatures.getValue().getExchangeSpecs().getName()));
            exchangeCol.setMaxWidth(Integer.MAX_VALUE * 0.2);
            tableView.getColumns().addAll(pairNameCol,exchangeCol,lastPriceCol,p1ChangeCol,p1RelativeChangeCol,p2ChangeCol,p2RelativeChangeCol, buttonsCol);
        } else
            tableView.getColumns().addAll(pairNameCol,lastPriceCol,p1ChangeCol,p1RelativeChangeCol,p2ChangeCol,p2RelativeChangeCol, buttonsCol);
        tableView.setItems(items);
        if (pinnedCheckboxes) {
            tableView.setEditable(true);
        }
        tableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Node node = ((Node) event.getTarget()).getParent();
                if (node instanceof TableRow || node.getParent() instanceof TableRow) {
                    CryptonosePairData cnPairData = tableView.getSelectionModel().getSelectedItem();
                    CryptonoseGuiBrowser.runBrowser(cnPairData.getPairName(), cnPairData.getExchangeSpecs());
                }
            }
        });
        var oldHandler = tableView.getOnKeyPressed();
        tableView.setOnKeyPressed(event -> {
            if (oldHandler != null)
                oldHandler.handle(event);
            if (event.getCode() == KeyCode.ENTER) {
                CryptonosePairData cnPairData = tableView.getSelectionModel().getSelectedItem();
                CryptonoseGuiBrowser.runBrowser(cnPairData.getPairName(), cnPairData.getExchangeSpecs());
            }
            if (event.getCode() == KeyCode.SPACE) {
                CryptonosePairData cnPairData = tableView.getSelectionModel().getSelectedItem();
                cnPairData.setPinned(!cnPairData.pinnedProperty().get());
            }
        });
        if (autoSort) {
            scheduledExecutorService.scheduleWithFixedDelay(() -> Platform.runLater(tableView::sort), TABLE_SORT_FREQUENCY_MILLIS, TABLE_SORT_FREQUENCY_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    // allow changing table order manually by using gui sorting functionality
    public void enableGuiSorting() {
        items.comparatorProperty().bind(tableView.comparatorProperty());
    }

    // show column with exchange name
    public void enableShowExchange() {
        enableShowExchange = true;
    }

    public void disablePluginButtonsFocusTraversable() {
        buttonsFocusTraversable = false;
    }

    public void enablePinnedCheckboxes() {
        pinnedCheckboxes = true;
    }

    // resort table every specified interval (TABLE_SORT_FREQUENCY_MILLIS)
    public void enableAutoSort() {
        autoSort = true;
    }

}
