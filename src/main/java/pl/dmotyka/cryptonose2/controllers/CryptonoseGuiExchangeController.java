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
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import pl.dmotyka.cryptonose2.model.CryptonoseGuiAlertChecker;
import pl.dmotyka.cryptonose2.tools.UILoader;
import pl.dmotyka.cryptonose2.dataobj.CryptonoseGuiConnectionStatus;
import pl.dmotyka.cryptonose2.dataobj.CryptonosePairData;
import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.cryptonose2.dataobj.PriceAlertThresholds;
import pl.dmotyka.cryptonose2.model.ExchangePairsDataModel;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.cryptonoseengine.CryptonoseGenericEngine;
import pl.dmotyka.cryptonoseengine.EngineChangesReceiver;
import pl.dmotyka.cryptonoseengine.EngineMessage;
import pl.dmotyka.cryptonoseengine.EngineMessageReceiver;
import pl.dmotyka.cryptonoseengine.EngineTransactionHeartbeatReceiver;
import pl.dmotyka.cryptonoseengine.PriceChanges;
import pl.dmotyka.exchangeutils.chartdataprovider.CurrencyPairTimePeriod;
import pl.dmotyka.exchangeutils.chartinfo.ChartCandle;
import pl.dmotyka.exchangeutils.chartutils.LiquidityFactor;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import pl.dmotyka.exchangeutils.tools.TimeConverter;

/**
 * Created by dawid on 8/1/17.
 */
public class CryptonoseGuiExchangeController implements Initializable, EngineMessageReceiver, EngineTransactionHeartbeatReceiver, EngineChangesReceiver {

    private static final Logger logger = Logger.getLogger(CryptonoseGuiExchangeController.class.getName());

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
    public TableView<CryptonosePairData> currenciesTableView;
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

    private ColorIndicatorBox indicatorBox;
    private CryptonoseGuiController cryptonoseGuiController;
    private CryptonoseGuiPriceAlertsTabController priceAlertTabController;
    private ExchangeSpecs exchangeSpecs;
    private CryptonoseGuiAlertChecker cryptonoseGuiAlertChecker;
    private CryptonoseGenericEngine engine;
    private final AtomicLong lastUpdateTimeMillis = new AtomicLong(0);
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> updatePreferencesScheduledFuture;
    private ScheduledFuture<?> reconnectScheduledFuture;
    private final Map<Long, PriceAlertThresholds> priceAlertThresholdsMap=Collections.synchronizedMap(new HashMap<>());
    private CryptonoseGuiSoundAlerts cryptonoseGuiSoundAlerts;
    private final LiquidityFactor liquidityFactorIndicator = new LiquidityFactor();

    private ExchangePairsDataModel pairsDataModel;
    private PriceChangesTable priceChangesTable;
    private final AtomicInteger numTradesPerSecondAtomicInteger = new AtomicInteger(0);
    private final AtomicReference<CryptonoseGuiConnectionStatus> connectionStatus = new AtomicReference<>(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED);
    private boolean noPairsAlertShown = false;

    public void init(ExchangeSpecs exchangeSpecs,CryptonoseGuiPriceAlertsTabController cryptonoseGuiPriceAlertsTabController, CryptonoseGuiController cryptonoseGuiController, ColorIndicatorBox indicatorBox) {
        this.priceAlertTabController = cryptonoseGuiPriceAlertsTabController;
        this.cryptonoseGuiController = cryptonoseGuiController;
        this.indicatorBox = indicatorBox;
        this.exchangeSpecs = exchangeSpecs;
        pairsButton.setOnAction(event -> pairsClick());
        alertSettingsButton.setOnAction(event -> alertSettingsClick());
        currenciesTableView.managedProperty().bind(currenciesTableView.visibleProperty());
        tableDisabledHbox.managedProperty().bind(tableDisabledHbox.visibleProperty());
        logTitledPane.managedProperty().bind(logTitledPane.visibleProperty());
        tableDisabledHbox.visibleProperty().bind(currenciesTableView.visibleProperty().not());
        CryptonoseSettings.getPrefsNode(CryptonoseSettings.PreferenceCategory.CATEGORY_ALERTS_PREFS, exchangeSpecs).addPreferenceChangeListener(evt -> {
            if(evt.getNode().name().equals(exchangeSpecs.getName())) {
                if(updatePreferencesScheduledFuture==null || updatePreferencesScheduledFuture.isDone())
                updatePreferencesScheduledFuture=scheduledExecutorService.schedule(this::initPriceAlertThresholds,1,TimeUnit.SECONDS);
            }
        });
        cryptonoseGuiSoundAlerts = new CryptonoseGuiSoundAlerts();
        initPriceAlertThresholds();
        cryptonoseGuiAlertChecker = new CryptonoseGuiAlertChecker(exchangeSpecs,priceAlertThresholdsMap);
        pairsDataModel = new ExchangePairsDataModel(exchangeSpecs, CryptonoseSettings.TIME_PERIODS);
        priceChangesTable = new PriceChangesTable(currenciesTableView, pairsDataModel.getItems(), CryptonoseSettings.TIME_PERIODS);
        priceChangesTable.init();
        indicatorBox.switchColor(connectionStatus.get().getCssClass());
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
        pairsDataModel.clear();
        lastUpdateTimeMillis.set(0);
        String markets = CryptonoseSettings.getString(CryptonoseSettings.Pairs.MARKETS, exchangeSpecs);
        ArrayList<PairSelectionCriteria> pairSelectionCriteria = new ArrayList<>(10);
        if(!markets.equals("")) {
            Arrays.stream(markets.split(",")).forEach(market -> {
                double minVolume = CryptonoseSettings.getDouble(new CryptonoseSettings.MarketVolumePreference(market), exchangeSpecs);
                if(minVolume>=0)
                    pairSelectionCriteria.add(new PairSelectionCriteria(market,minVolume));
            });
        }
        String pairs = CryptonoseSettings.getString(CryptonoseSettings.Pairs.PAIRS_API_SYMBOLS, exchangeSpecs);
        String[] additionalPairs;
        if(pairs==null || pairs.equals(""))
            additionalPairs = new String[0];
        else
            additionalPairs=pairs.split(",");
        new Thread(() -> {
            if (engine!=null)
                engine.stop();
            engine = CryptonoseGenericEngine.withProvidedMarketsAndPairs(exchangeSpecs,
                    this,
                    CryptonoseSettings.TIME_PERIODS,
                    CryptonoseSettings.RELATIVE_CHANGE_NUM_CANDLES,
                    pairSelectionCriteria.toArray(PairSelectionCriteria[]::new),
                    additionalPairs);
            engine.enableInitEngineWithLowerPeriodChartData();
            engine.autoRefreshPairData(CryptonoseSettings.AUTO_REFRESH_INTERVAL_MINUTES);
            engine.setCheckChangesDelayMs(100);
            engine.setEngineMessageReceiver(this);
            engine.setEngineUpdateHeartbeatReceiver(this);
            engine.useMedianRelativeChanges();
            engine.subscribeChartData(map -> pairsDataModel.updateChartData(map));
            engine.start();
        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scheduledExecutorService = Executors.newScheduledThreadPool(4);
        startLastTransactionTimer();
        consoleTextArea.setOnKeyPressed(event -> consoleTextArea.getScene().getOnKeyPressed().handle(event));
        logTitledPane.visibleProperty().bind(showLogCheckBox.selectedProperty());
        soundCheckBox.setOnAction(e -> cryptonoseGuiController.updateCheckboxes());
        runBrowserCheckBox.setOnAction(e -> cryptonoseGuiController.updateCheckboxes());
        notificationCheckBox.setOnAction(e -> cryptonoseGuiController.updateCheckboxes());
    }

    public void pairsClick() {
        UILoader<CryptonoseGuiPairsController> uiLoader = new UILoader<>("cryptonoseGuiPairs.fxml");
        AtomicBoolean settingsChangedAtomic = new AtomicBoolean(false);
        uiLoader.getController().init(exchangeSpecs,()->settingsChangedAtomic.set(true));
        pairsButton.setDisable(true);
        uiLoader.stageShowAndWait("Pairs settings: " + exchangeSpecs.getName());
        pairsButton.setDisable(false);
        if(settingsChangedAtomic.get()) {
            startEngine();
        }
    }

    public void alertSettingsClick() {
        UILoader<CryptonoseGuiAlertSettingsController> uiLoader = new UILoader<>("cryptonoseGuiAlertSettings.fxml");
        uiLoader.getController().init(exchangeSpecs, CryptonoseSettings.TIME_PERIODS);
        alertSettingsButton.setDisable(true);
        uiLoader.stageShowAndWait("Alerts conditions: " + exchangeSpecs.getName());
        alertSettingsButton.setDisable(false);
    }

    private void setConnectionStatus(CryptonoseGuiConnectionStatus newConnectionStatus, boolean notify) {
        if (connectionStatus.get() != null && newConnectionStatus.equals(connectionStatus.get()))
            return;
        connectionStatus.set(newConnectionStatus);
        javafx.application.Platform.runLater(() -> {
            connectionStatusLabel.setText(newConnectionStatus.getText().toLowerCase());
            indicatorBox.switchColor(newConnectionStatus.getCssClass());
        });
        if(notify && CryptonoseSettings.getBool(CryptonoseSettings.General.CONNECTION_STATUS_NOTIFICATIONS))
            CryptonoseGuiNotification.notifyConnectionState(CryptonoseSettings.NOTIFICATION_LIBRARY,exchangeSpecs, newConnectionStatus);
    }

    @Override
    public void message(EngineMessage msg) {
        if(msg.getCode()==EngineMessage.Type.ERROR)
            consoleLog("Error: " + msg.getMessage());
        else
            consoleLog(msg.getMessage());
        switch(msg.getCode()) {
            case CONNECTED:
                lastUpdateTimeMillis.set(System.currentTimeMillis());
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTED, true);
                break;
            case CONNECTING:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTING, false);
                break;
            case RECONNECTING:
                break;
            case DISCONNECTED:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED, true);
                break;
            case NO_PAIRS:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_PAIRS, false);
                if (!noPairsAlertShown) {
                    noPairsAlertShown = true;
                    Platform.runLater(()-> {
                        CryptonoseAlert alert = new CryptonoseAlert(Alert.AlertType.CONFIRMATION, "Got 0 valid currency pairs for " + exchangeSpecs.getName() + ". Show currency pairs settings?", ButtonType.YES, ButtonType.NO);
                        alert.getDialogPane().setPrefWidth(500);
                        alert.setTitle("Pairs settings: " + exchangeSpecs.getName());
                        alert.showAndWait();
                        if (alert.getResult() == ButtonType.YES)
                            pairsClick();
                    });
                }
                if (reconnectScheduledFuture == null || reconnectScheduledFuture.isDone()) {
                    consoleLog(String.format("Reconnecting in %d minutes", CryptonoseSettings.NO_PAIRS_RECONNECT_MINUTES));
                    reconnectScheduledFuture = scheduledExecutorService.schedule(() -> {
                        if (connectionStatus.get() == CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_PAIRS)
                            reconnectEngine();
                    }, CryptonoseSettings.NO_PAIRS_RECONNECT_MINUTES, TimeUnit.MINUTES);
                }
                break;
            case AUTO_REFRESHING_DONE:
                lastUpdateTimeMillis.set(System.currentTimeMillis());
                pairsDataModel.removeOutdatedPairs(engine.getAllPairs());
        }
    }

    @Override
    public void receiveTransactionHeartbeat() {
        lastUpdateTimeMillis.set(System.currentTimeMillis());
        numTradesPerSecondAtomicInteger.getAndIncrement();
    }

    @Override
    public void receiveChanges(PriceChanges priceChanges) {
        //TODO refactor?
        List<PriceChanges> priceChangesList = new LinkedList<>();
        priceChangesList.add(priceChanges);
        receiveChanges(priceChangesList);
    }

    @Override
    public void receiveChanges(List<PriceChanges> priceChangesList) {
        List<PriceAlert> priceAlerts = cryptonoseGuiAlertChecker.checkAlerts(priceChangesList);
        Iterator<PriceAlert> it = priceAlerts.iterator();
        // check cn liquidity
        if (CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_MIN_CN_LIQUIDITY, exchangeSpecs)) {
            while (it.hasNext()) {
                PriceAlert priceAlert = it.next();
                ChartCandle[] candles = engine.getCandleData(new CurrencyPairTimePeriod(priceAlert.getPair(), priceAlert.getPeriodSeconds()));
                if (candles.length >= CryptonoseSettings.RELATIVE_CHANGE_NUM_CANDLES) {
                    double liqFactorVal = liquidityFactorIndicator.calcValue(candles, CryptonoseSettings.RELATIVE_CHANGE_NUM_CANDLES);
                    if (liqFactorVal < CryptonoseSettings.getDouble(CryptonoseSettings.Alert.MIN_CN_LIQUIDITY, exchangeSpecs)) {
                        logger.fine(String.format("Cn liquidity factor is too low, filtering out alert: %s %d", priceAlert.getPair(), priceAlert.getPeriodSeconds()));
                        it.remove();
                    }
                } else {
                    logger.warning(String.format("not enough candles %s %d (this shouldn't be happening)", priceAlert.getPair(), priceAlert.getPeriodSeconds()));
                }
            }
        }
        for(PriceAlert priceAlert : priceAlerts)
            handlePriceAlert(priceAlert);
        if(currenciesTableView.isVisible()) {
            Platform.runLater(() -> pairsDataModel.updateData(priceChangesList));
        }
    }

    private void handlePriceAlert(PriceAlert priceAlert) {
        CurrencyPairTimePeriod currencyPairTimePeriod = new CurrencyPairTimePeriod(priceAlert.getPair(),CryptonoseSettings.MINI_CHART_TIME_PERIOD_SEC);
        ChartCandle[] chartCandles = engine.requestCandlesGeneration(currencyPairTimePeriod);
        ChartCandle lastCandle = chartCandles[chartCandles.length-1];
        chartCandles[chartCandles.length-1] = new ChartCandle(lastCandle.getHigh(),
                lastCandle.getLow(),
                lastCandle.getOpen(),
                priceAlert.getFinalPrice(),
                lastCandle.getTimestampSeconds());
        int numCandles = (int)(CryptonoseSettings.MINI_CHART_TIMEFRAME_SEC / CryptonoseSettings.MINI_CHART_TIME_PERIOD_SEC);
        if (chartCandles.length < numCandles)
            chartCandles = null;
        else
            chartCandles = Arrays.copyOfRange(chartCandles, chartCandles.length-numCandles, chartCandles.length);
        priceAlertTabController.addAlert(priceAlert, chartCandles);
        String alertString = String.format("Price alert on %s, %s: change by %.2f (relative: %.2f), period: %s, final price: %s",
                priceAlert.getFormattedPair(),
                exchangeSpecs.getName(),
                priceAlert.getPriceChange(),
                priceAlert.getRelativePriceChange(),
                TimeConverter.secondsToFullMinutesHoursDays((int)priceAlert.getPeriodSeconds()),
                DecimalFormatter.formatDecimalPrice(priceAlert.getFinalPrice()));
        consoleLog(alertString);
        SimpleDateFormat preciseTimeDateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        String preciseTime = preciseTimeDateFormat.format(new Date(System.currentTimeMillis()));
        logger.info(preciseTime + ": " + alertString);
        if (runBrowserCheckBox.isSelected()) {
            CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs());
        }
        if(notificationCheckBox.isSelected()) {
            CryptonoseGuiNotification.notifyPriceAlert(CryptonoseSettings.NOTIFICATION_LIBRARY,priceAlert,()->CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs()));
        }
        if (soundCheckBox.isSelected()) {
            cryptonoseGuiSoundAlerts.soundAlert(priceAlert);
        }
    }

    private void initPriceAlertThresholds() {
        logger.info("updating alerts values for " + exchangeSpecs);
        for(long currentTimePeriod : CryptonoseSettings.TIME_PERIODS) {
            priceAlertThresholdsMap.put(currentTimePeriod,
                    CryptonoseSettings.getPriceAlertThresholds(exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(currentTimePeriod))
            );
        }
    }

    private void startLastTransactionTimer() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (lastUpdateTimeMillis.get() !=0) {
                long lastTradeSecondsAgo = (System.currentTimeMillis() - lastUpdateTimeMillis.get()) / 1000;
                javafx.application.Platform.runLater(() -> lastTradeLabel.setText(lastTradeSecondsAgo + " seconds ago"));
                if (lastTradeSecondsAgo > CryptonoseSettings.NO_TRADES_RECONNECT_SECONDS) {
                    consoleLog(String.format("No trades for %d seconds. Reconnecting...", CryptonoseSettings.NO_TRADES_RECONNECT_SECONDS));
                    setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_TRADES_RECONNECT, true);
                    setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTING, false);
                    reconnectEngine();
                } else if (lastTradeSecondsAgo > CryptonoseSettings.NO_TRADES_WARNING_SECONDS) {
                    if (!connectionStatus.get().equals(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_TRADES))
                        consoleLog(String.format("No trades for %d seconds.", CryptonoseSettings.NO_TRADES_WARNING_SECONDS));
                    setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_TRADES, false);
                } else if (connectionStatus.get().equals(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_TRADES))
                    setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTED, false);
            }
        },1,1, TimeUnit.SECONDS);
    }

    private void reconnectEngine() {
        lastUpdateTimeMillis.set(0);
        pairsDataModel.clear();
        javafx.application.Platform.runLater(() -> lastTradeLabel.setText("no updates yet"));
        new Thread(() -> engine.reconnect()).start();
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
        if(!enable && engine!=null)
            pairsDataModel.updateData(Arrays.asList(engine.requestAllPairsChanges()));
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

    public ObservableList<CryptonosePairData> getReadonlyPairsData() {
        return pairsDataModel.getReadonlyItems();
    }

    public ObservableList<CryptonosePairData> getPairsData() {
        return pairsDataModel.getItems();
    }

}
