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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.CryptonoseGuiAlertChecker;
import pl.dmotyka.cryptonose2.CryptonoseGuiBrowser;
import pl.dmotyka.cryptonose2.CryptonoseGuiConnectionStatus;
import pl.dmotyka.cryptonose2.CryptonoseGuiNotification;
import pl.dmotyka.cryptonose2.CryptonoseGuiSoundAlerts;
import pl.dmotyka.cryptonose2.PriceAlert;
import pl.dmotyka.cryptonose2.PriceAlertThresholds;
import pl.dmotyka.cryptonose2.TablePairPriceChanges;
import pl.dmotyka.cryptonoseengine.CryptonoseGenericEngine;
import pl.dmotyka.cryptonoseengine.EngineChangesReceiver;
import pl.dmotyka.cryptonoseengine.EngineMessage;
import pl.dmotyka.cryptonoseengine.EngineMessageReceiver;
import pl.dmotyka.cryptonoseengine.EngineTransactionHeartbeatReceiver;
import pl.dmotyka.cryptonoseengine.PriceChanges;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.tools.TimeConverter;

/**
 * Created by dawid on 8/1/17.
 */
public class CryptonoseGuiExchangeController implements Initializable, EngineMessageReceiver, EngineTransactionHeartbeatReceiver, EngineChangesReceiver {

    private static final Logger logger = Logger.getLogger(CryptonoseGuiExchangeController.class.getName());

    public static final long[] TIME_PERIODS = {300,1800};
    public static final int RELATIVE_CHANGE_NUM_CANDLES = 50;
    private static final boolean LOG_VISIBLE = false;
    private static final long TABLE_SORT_FREQUENCY_MILLIS = 2500;
    private static final CryptonoseGuiNotification.NotificationLibrary NOTIFICATION_LIBRARY=CryptonoseGuiNotification.NotificationLibrary.DORKBOX;

    @FXML
    public VBox mainVBox;
    @FXML
    public TextArea consoleTextArea;
    @FXML
    public Label lastTradeLabel;
    @FXML
    public Label connectionStatusLabel;
    @FXML
    public CheckBox soundCheckBox;
    @FXML
    public CheckBox runBrowserCheckBox;
    @FXML
    public CheckBox notificationCheckBox;
    @FXML
    public TableView currenciesTableView;
    @FXML
    public HBox tableDisabledHbox;
    @FXML
    public TitledPane logTitledPane;
    @FXML
    public CheckBox showLogCheckBox;
    @FXML
    public Button pairsButton;
    @FXML
    public Button alertSettingsButton;

    private Pane graphicsPane;
    private CryptonoseGuiController cryptonoseGuiController;
    private CryptonoseGuiPriceAlertsTabController priceAlertTabController;
    private ExchangeSpecs exchangeSpecs;
    private CryptonoseGuiAlertChecker cryptonoseGuiAlertChecker;
    private CryptonoseGenericEngine engine;
    long lastTradeTimeMillis = 0;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<Long, PriceAlertThresholds> priceAlertThresholdsMap=Collections.synchronizedMap(new HashMap<>());
    private CryptonoseGuiSoundAlerts cryptonoseGuiSoundAlerts;
    private Preferences alertPreferences;
    private Preferences cryptonosePreferences;
    private ScheduledFuture updatePreferencesScheduledFuture;

    private Map<String, TablePairPriceChanges> pairPriceChangesMap=new HashMap<>();
    private ObservableList<TablePairPriceChanges> tablePairPriceChangesObservableList;
    private long lastTableSortMillis =0;
    private AtomicInteger numTradesPerSecondAtomicInteger = new AtomicInteger(0);

    class PriceChangesTableCell extends TableCell<TablePairPriceChanges,Number> {
        @Override
        protected void updateItem(Number item, boolean empty) {
            super.updateItem(item, empty);
            if(empty)
                setText(null);
            if(item!=null) {
                if (item.doubleValue() >= 0)
                    setTextFill(Color.GREEN);
                else
                    setTextFill(Color.RED);
                setText(String.format("%.2f", item));
            } else {
                setText(null);
            }
        }
    }
    class PriceTableCell extends TableCell<TablePairPriceChanges,Number> {
        @Override
        protected void updateItem(Number item, boolean empty) {
            super.updateItem(item, empty);
            if(empty)
                setText(null);
            if(item==null)
                setText(null);
            else
                setText(DecimalFormatter.formatDecimalPrice(item.doubleValue()));
        }
    }

    public void init(ExchangeSpecs exchangeSpecs,CryptonoseGuiPriceAlertsTabController cryptonoseGuiPriceAlertsTabController, CryptonoseGuiController cryptonoseGuiController, Pane graphicsPane) {
        this.priceAlertTabController = cryptonoseGuiPriceAlertsTabController;
        this.cryptonoseGuiController = cryptonoseGuiController;
        this.graphicsPane = graphicsPane;
        this.exchangeSpecs=exchangeSpecs;
        pairsButton.setOnMouseClicked(event -> pairsClick());
        alertSettingsButton.setOnMouseClicked(event -> alertSettingsClick());
        currenciesTableView.managedProperty().bind(currenciesTableView.visibleProperty());
        tableDisabledHbox.managedProperty().bind(tableDisabledHbox.visibleProperty());
        logTitledPane.managedProperty().bind(logTitledPane.visibleProperty());
        tableDisabledHbox.visibleProperty().bind(currenciesTableView.visibleProperty().not());
        cryptonosePreferences=Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("cryptonosePreferences");
        alertPreferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("alertPreferences").node(exchangeSpecs.getName());
        alertPreferences.addPreferenceChangeListener(evt -> {
            if(evt.getNode().name().equals(exchangeSpecs.getName())) {
                if(updatePreferencesScheduledFuture==null || updatePreferencesScheduledFuture.isDone())
                updatePreferencesScheduledFuture=scheduledExecutorService.schedule(()->initPriceAlertThresholds(),1,TimeUnit.SECONDS);
            }
        });
        cryptonoseGuiSoundAlerts = new CryptonoseGuiSoundAlerts(cryptonosePreferences);
        initPriceAlertThresholds();
        cryptonoseGuiAlertChecker = new CryptonoseGuiAlertChecker(exchangeSpecs,priceAlertThresholdsMap);
        initTable();
        startEngine();
    }

    public void close() {
        if(engine!=null) {
            engine.stop();
            engine = null;
        }
        if(scheduledExecutorService!=null) {
            scheduledExecutorService.shutdown();
        }
    }

    private void startEngine() {
        Preferences pairsPreferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("pairsPreferences").node(exchangeSpecs.getName());
        String markets = pairsPreferences.get("markets","");
        ArrayList<PairSelectionCriteria> pairSelectionCriteria = new ArrayList<>(10);
        if(!markets.equals("")) {
            Arrays.stream(markets.split(",")).forEach(market -> {
                Double minVolume = pairsPreferences.getDouble(market,-1.0);
                if(minVolume>=0)
                    pairSelectionCriteria.add(new PairSelectionCriteria(market,minVolume));
            });
        }
        String pairs = pairsPreferences.get("pairsApiSymbols",null);
        String[] additionalPairs;
        if(pairs==null || pairs.equals(""))
            additionalPairs = new String[0];
        else
            additionalPairs=pairs.split(",");
        engine = CryptonoseGenericEngine.withProvidedMarketsAndPairs(exchangeSpecs,
                this,
                TIME_PERIODS,
                RELATIVE_CHANGE_NUM_CANDLES,
                pairSelectionCriteria.toArray(new PairSelectionCriteria[pairSelectionCriteria.size()]),
                additionalPairs);
        engine.enableInitEngineWithLowerPeriodChartData();
        engine.setEngineMessageReceiver(this);
        engine.setEngineUpdateHeartbeatReceiver(this);
        new Thread(()->engine.start()).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
        startLastTransactionTimer();
        pairPriceChangesMap = new HashMap<>();
        tablePairPriceChangesObservableList = FXCollections.observableArrayList();
        consoleTextArea.setOnKeyPressed(event -> consoleTextArea.getScene().getOnKeyPressed().handle(event));
        showLogCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            logTitledPane.setVisible(newValue);
        });
        soundCheckBox.setOnMouseClicked(e -> cryptonoseGuiController.updateCheckboxes());
        runBrowserCheckBox.setOnMouseClicked(e -> cryptonoseGuiController.updateCheckboxes());
        notificationCheckBox.setOnMouseClicked(e -> cryptonoseGuiController.updateCheckboxes());
    }

    private void initTable() {
        TableColumn<TablePairPriceChanges,String> pairNameCol = new TableColumn("Pair name");
        pairNameCol.setCellValueFactory(cellDataFeatures -> new SimpleStringProperty(cellDataFeatures.getValue().getFormattedPairName()));
        pairNameCol.setPrefWidth(150);
        TableColumn<TablePairPriceChanges,Number> lastPriceCol = new TableColumn("Last price");
        lastPriceCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().lastPriceProperty());
        lastPriceCol.setCellFactory(col -> new PriceTableCell());
        lastPriceCol.setPrefWidth(150);
        TableColumn<TablePairPriceChanges,Number> p1ChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(TIME_PERIODS[0]) +" % change");
        p1ChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p1PercentChangeProperty());
        p1ChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p1ChangeCol.setPrefWidth(100);
        TableColumn<TablePairPriceChanges,Number> p1RelativeChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(TIME_PERIODS[0])+" relative");
        p1RelativeChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p1RelativeChangeProperty());
        p1RelativeChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p1RelativeChangeCol.setPrefWidth(100);
        TableColumn<TablePairPriceChanges,Number> p2ChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(TIME_PERIODS[1])+" % change");
        p2ChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p2PercentChangeProperty());
        p2ChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p2ChangeCol.setPrefWidth(100);
        TableColumn<TablePairPriceChanges,Number> p2RelativeChangeCol = new TableColumn(TimeConverter.secondsToFullMinutesHoursDays(TIME_PERIODS[1])+" relative");
        p2RelativeChangeCol.setCellValueFactory(cellDataFeatures -> cellDataFeatures.getValue().p2RelativeChangeProperty());
        p2RelativeChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p2RelativeChangeCol.setPrefWidth(100);
        currenciesTableView.setItems(tablePairPriceChangesObservableList);
        currenciesTableView.getColumns().addAll(pairNameCol,lastPriceCol,p1ChangeCol,p1RelativeChangeCol,p2ChangeCol,p2RelativeChangeCol);
        logTitledPane.setVisible(LOG_VISIBLE);
        showLogCheckBox.setSelected(LOG_VISIBLE);
        currenciesTableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Node node = ((Node) event.getTarget()).getParent();
                if (node instanceof TableRow || node.getParent() instanceof TableRow) {
                    CryptonoseGuiBrowser.runBrowser(((TablePairPriceChanges)currenciesTableView.getSelectionModel().getSelectedItem()).getPairName(),exchangeSpecs);
                }
            }
        });
    }

    public void pairsClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiPairs.fxml"));
            Parent root = fxmlLoader.load();
            AtomicBoolean settingsChangedAtomic = new AtomicBoolean(false);
            ((CryptonoseGuiPairsController)fxmlLoader.getController()).init(exchangeSpecs,()->settingsChangedAtomic.set(true));
            Stage stage = new Stage();
            stage.setTitle("Pairs settings: " + exchangeSpecs.getName());
            stage.setScene(new Scene(root));
            pairsButton.setDisable(true);
            stage.showAndWait();
            pairsButton.setDisable(false);
            if(settingsChangedAtomic.get()) {
                if(engine!=null)
                    engine.stop();
                startEngine();
            }
        } catch(IOException e) {
            throw new Error(e);
        }
    }

    public void alertSettingsClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiAlertSettings.fxml"));
            Parent root = fxmlLoader.load();
            ((CryptonoseGuiAlertSettingsController)fxmlLoader.getController()).init(exchangeSpecs,TIME_PERIODS);
            Stage stage = new Stage();
            stage.setTitle("Alerts conditions: " + exchangeSpecs.getName());
            stage.setScene(new Scene(root));
            alertSettingsButton.setDisable(true);
            stage.showAndWait();
            alertSettingsButton.setDisable(false);
        } catch(IOException e) {
            throw new Error(e);
        }
    }

    private void setConnectionStatus(CryptonoseGuiConnectionStatus cryptonoseGuiConnectionStatus) {
        javafx.application.Platform.runLater(() -> {
            connectionStatusLabel.setText(cryptonoseGuiConnectionStatus.getText());
            graphicsPane.setStyle("-fx-background-color: " + cryptonoseGuiConnectionStatus.getColor());
        });
        if(!cryptonoseGuiConnectionStatus.getText().equals("connecting"))
            CryptonoseGuiNotification.notifyConnectionState(NOTIFICATION_LIBRARY,exchangeSpecs,cryptonoseGuiConnectionStatus);
    }

    @Override
    public void message(EngineMessage msg) {
        if(msg.getCode()==EngineMessage.Type.ERROR)
            consoleLog("Error: " + msg.getMessage());
        else
            consoleLog(msg.getMessage());
        switch(msg.getCode()) {
            case CONNECTED:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTED);
                break;
            case CONNECTING:
            case RECONNECTING:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTING);
                break;
            case DISCONNECTED:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED);
                break;
            case NO_PAIRS:
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Got 0 valid currency pairs for " + exchangeSpecs.getName() + ". Show currency pairs settings?", ButtonType.YES, ButtonType.NO);
                    alert.getDialogPane().setPrefWidth(500);
                    alert.setTitle("Pairs settings: " + exchangeSpecs.getName());
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES)
                        pairsClick();
                    if(alert.getResult() == ButtonType.NO)
                        close();
                });
        }
    }

    @Override
    public void receiveTransactionHeartbeat() {
        lastTradeTimeMillis=System.currentTimeMillis();
        numTradesPerSecondAtomicInteger.getAndIncrement();
    }

    @Override
    public void receiveChanges(PriceChanges priceChanges) {
        List<PriceChanges> priceChangesList = new LinkedList<>();
        priceChangesList.add(priceChanges);
        receiveChanges(priceChangesList);
    }

    @Override
    public void receiveChanges(List<PriceChanges> priceChangesList) {
        List<PriceAlert> priceAlerts = cryptonoseGuiAlertChecker.checkAlerts(priceChangesList);
        for(PriceAlert priceAlert : priceAlerts)
            handlePriceAlert(priceAlert);
        if(currenciesTableView.isVisible())
            updateTable(priceChangesList);
    }

    private synchronized void updateTable(List<PriceChanges> priceChangesList) {
        for (PriceChanges priceChanges : priceChangesList) {
            TablePairPriceChanges tablePairPriceChanges =pairPriceChangesMap.get(priceChanges.getCurrencyPair());
            if(tablePairPriceChanges ==null) {
                tablePairPriceChanges = new TablePairPriceChanges(exchangeSpecs, priceChanges.getCurrencyPair());
                pairPriceChangesMap.put(priceChanges.getCurrencyPair(), tablePairPriceChanges);
                tablePairPriceChangesObservableList.add(tablePairPriceChanges);
            }
            int period = (priceChanges.getTimePeriodSeconds() == TIME_PERIODS[0]) ? TablePairPriceChanges.PERIOD1 : TablePairPriceChanges.PERIOD2;
            tablePairPriceChanges.setPriceChanges(priceChanges,period);
        }
        if(System.currentTimeMillis()- lastTableSortMillis > TABLE_SORT_FREQUENCY_MILLIS) {
            currenciesTableView.sort();
            lastTableSortMillis=System.currentTimeMillis();
        }
    }

    private void handlePriceAlert(PriceAlert priceAlert) {
        priceAlertTabController.addAlert(priceAlert);
        String alertString = String.format("Price alert on %s, %s: change by %.2f (relative: %.2f), period: %s, final price: %s",
                priceAlert.getFormattedPair(),
                exchangeSpecs.getName(),
                priceAlert.getPriceChange(),
                priceAlert.getRelativePriceChange(),
                TimeConverter.secondsToFullMinutesHoursDays((int)priceAlert.getPeriodSeconds()),
                DecimalFormatter.formatDecimalPrice(priceAlert.getFinalPrice()));
        consoleLog(alertString);
        if (runBrowserCheckBox.isSelected()) {
            CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs());
        }
        if(notificationCheckBox.isSelected()) {
            CryptonoseGuiNotification.notifyPriceAlert(NOTIFICATION_LIBRARY,priceAlert,()->CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs()));
        }
        if (soundCheckBox.isSelected()) {
            cryptonoseGuiSoundAlerts.soundAlert(priceAlert);
        }
    }

    private void initPriceAlertThresholds() {
        logger.info("updating alerts values for " + exchangeSpecs);
        for(long currentTimePeriod : TIME_PERIODS) {
            priceAlertThresholdsMap.put(currentTimePeriod,
                    PriceAlertThresholds.fromPreferences(
                            alertPreferences,
                            ""+currentTimePeriod
                    )
            );
        };
    }

    private void startLastTransactionTimer() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (lastTradeTimeMillis!=0) {
                long lastTradeSecondsAgo = (System.currentTimeMillis() - lastTradeTimeMillis) / 1000;
                javafx.application.Platform.runLater(() -> {
                    lastTradeLabel.setText(lastTradeSecondsAgo + " seconds ago");
                });
            }
        },1,1, TimeUnit.SECONDS);
    }

    private void consoleLog(String text) {
        javafx.application.Platform.runLater(() -> {
            if(consoleTextArea!=null)
                consoleTextArea.appendText(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss: ")) + text + "\n");
        });
    }

    public void enablePlaySound(boolean enable) {
        soundCheckBox.setSelected(enable);
    }

    public void enableRunBrowser(boolean enable) {
        runBrowserCheckBox.setSelected(enable);
    }

    public void enableNotification(boolean enable) {
        notificationCheckBox.setSelected(enable);
    }

    public void enablePowerSave(boolean enable) {
        currenciesTableView.setVisible(!enable);
        if(!enable)
            updateTable(Arrays.asList(engine.requestAllPairsChanges()));
    }

    public boolean getIsSoundEnabled() {
        return soundCheckBox.isSelected();
    }
    public boolean getIsBrowserEnabled() {
        return runBrowserCheckBox.isSelected();
    }
    public boolean getIsNotifEnabled() {
        return notificationCheckBox.isSelected();
    }
}
