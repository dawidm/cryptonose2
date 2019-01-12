package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.cryptonose2.*;
import com.dawidmotyka.cryptonoseengine.*;
import com.dawidmotyka.dmutils.TimeConverter;
import com.dawidmotyka.exchangeutils.exchangespecs.*;
import com.dawidmotyka.exchangeutils.pairdataprovider.PairSelectionCriteria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by dawid on 8/1/17.
 */
public class CryptonoseGuiExchangeController implements Initializable, EngineMessageReceiver, EngineTransactionHeartbeatReceiver, EngineChangesReceiver {

    private static final Logger logger = Logger.getLogger(CryptonoseGuiExchangeController.class.getName());

    public static final String DEFAULT_TIME_PERIODS_VALUE = "300,1800";
    public static final int DEFAULT_MIN_VOLUME_VALUE = 200;
    public static final int DEFAULT_AUTO_REFRESH_PAIR_DATA_VALUE = 1800;
    public static final int RELATIVE_CHANGE_NUM_CANDLES = 50;

    private static final boolean CURRENCIES_TABLE_VISIBLE = false;
    private static final long TABLE_UPDATE_FREQUENCY_MILLIS=1000;
    public static final long NO_TRADES_PERIOD_SECONDS_TO_SET_DISCONNECTED=60;
    public static final CryptonoseGuiNotification.NotificationLibrary NOTIFICATION_LIBRARY=CryptonoseGuiNotification.NotificationLibrary.CONTROLSFX;

    @FXML
    public VBox mainVBox;
    @FXML
    public TextArea consoleTextArea;
    @FXML
    public Label lastTradeLabel;
    @FXML
    public Label connectionStatusLabel;
    @FXML
    public Label tpsLabel;
    @FXML
    public CheckBox soundCheckBox;
    @FXML
    public CheckBox runBrowserCheckBox;
    @FXML
    public CheckBox notificationCheckBox;
    @FXML
    public TableView currenciesTableView;
    @FXML
    public TitledPane logTitledPane;
    @FXML
    public CheckBox showTableCheckBox;

    private Pane graphicsPane;
    private CryptonoseGuiPriceAlertsTabController priceAlertTabController;
    private ExchangeSpecs exchangeSpecs;
    private int[] timePeriods;
    private CryptonoseGuiAlertChecker cryptonoseGuiAlertChecker;
    private CryptonoseEngineBase engine;
    long lastTradeTimeMillis = 0;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<Long,PriceAlertThresholds> priceAlertThresholdsMap=Collections.synchronizedMap(new HashMap<>());
    private CryptonoseGuiSoundAlerts cryptonoseGuiSoundAlerts;
    private Preferences alertPreferences;
    private Preferences enginePreferences;
    private Preferences cryptonosePreferences;

    private Map<String, PairPriceChanges> pairPriceChangesMap;
    private ObservableList<PairPriceChanges> pairPriceChangesObservableList;
    private long lastTableUpdateMillis=0;
    private CryptonoseGuiConnectionStatus currentConnectionStatus=CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED;
    private long numTradesPerSecond=0;
    private Object numTradesPerSecondLock=new Object();

    class PriceChangesTableCell extends TableCell<PairPriceChanges,Double> {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if(empty)
                setText(null);
            if(item!=null) {
                if (item >= 0)
                    setTextFill(Color.GREEN);
                else
                    setTextFill(Color.RED);
                setText(String.format("%.2f", item));
            } else {
                setText(null);
            }
        }
    }
    class PriceTableCell extends TableCell<PairPriceChanges,Double> {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if(empty)
                setText(null);
            if(item==null)
                setText(null);
            else
                setText(new DecimalFormat("#.########").format(item));
        }
    }

    public void init(ExchangeSpecs exchangeSpecs,CryptonoseGuiPriceAlertsTabController cryptonoseGuiPriceAlertsTabController, Pane graphicsPane) {
        this.priceAlertTabController = cryptonoseGuiPriceAlertsTabController;
        this.graphicsPane = graphicsPane;
        this.exchangeSpecs=exchangeSpecs;
        cryptonosePreferences=Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("cryptonosePreferences");
        alertPreferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("alertPreferences").node(exchangeSpecs.getName());
        cryptonoseGuiSoundAlerts = new CryptonoseGuiSoundAlerts(cryptonosePreferences);
        enginePreferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("enginePreferences").node(exchangeSpecs.getName());
        enginePreferences.addPreferenceChangeListener(evt -> {updateEnginePreferences();});
        String[] stringTimePeriods = enginePreferences.get("timePeriods",DEFAULT_TIME_PERIODS_VALUE).split(",");
        try {
            timePeriods = new int[]{Integer.parseInt(stringTimePeriods[0]), Integer.parseInt(stringTimePeriods[1])};
            initPriceAlertThresholds();
            cryptonoseGuiAlertChecker = new CryptonoseGuiAlertChecker(exchangeSpecs,priceAlertThresholdsMap);
            initTable();
            startEngine();
        } catch (Exception e) {
            throw new Error("error reading time period settings for "+exchangeSpecs.getName(),e);
        }
    }

    public void close() {
        if(engine!=null) {
            engine.stop();
            engine = null;
        }
    }

    private void startEngine() {
        if(exchangeSpecs.getClass().equals(PoloniexExchangeSpecs.class)
                || exchangeSpecs.getClass().equals(BittrexExchangeSpecs.class)
                || exchangeSpecs.getClass().equals(BinanceExchangeSpecs.class)) {
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
                    timePeriods,
                    RELATIVE_CHANGE_NUM_CANDLES,
                    pairSelectionCriteria.toArray(new PairSelectionCriteria[pairSelectionCriteria.size()]),
                    additionalPairs);
            engine.autoRefreshPairData(enginePreferences.getInt("autoRefreshDataMinutes", DEFAULT_AUTO_REFRESH_PAIR_DATA_VALUE));
        } else if (exchangeSpecs.getClass().equals(XtbExchangeSpecs.class)) {
            Properties properties = new Properties();
            String propertiesFileName=exchangeSpecs.getClass().getSimpleName()+".settings";
            try {
                properties.load(new FileInputStream(propertiesFileName));
                engine = CryptonoseGenericEngine.withProvidedCurrencyPairs(exchangeSpecs,this,timePeriods,RELATIVE_CHANGE_NUM_CANDLES,properties.getProperty("pairs").split(","));
            } catch (IOException e) {
                consoleLog("error reading pairs from " + propertiesFileName);
                return;
            }
        }
        engine.setEngineMessageReceiver(this);
        engine.setEngineUpdateHeartbeatReceiver(this);
        new Thread(()->engine.start()).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
        startLastTransactionTimer();
        startChangesPerSecondCounter();
        pairPriceChangesMap = new HashMap<>();
        pairPriceChangesObservableList = FXCollections.observableArrayList();
        consoleTextArea.setOnKeyPressed(event -> consoleTextArea.getScene().getOnKeyPressed().handle(event));
        showTableCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> currenciesTableView.setVisible(newValue));
        initShortcuts();
    }

    private void updateEnginePreferences() {
        //engine.stop();
        //startEngine();
    }

    private void startChangesPerSecondCounter() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            long currentTps;
            synchronized (numTradesPerSecondLock) {
                currentTps=numTradesPerSecond;
                numTradesPerSecond=0;
            }
            javafx.application.Platform.runLater(() -> tpsLabel.setText(""+currentTps));
        },1,1,TimeUnit.SECONDS);
    }

    private void initTable() {
        TableColumn pairNameCol = new TableColumn("Pair name");
        pairNameCol.setCellValueFactory(new PropertyValueFactory<>("formattedPairName"));
        pairNameCol.setPrefWidth(150);

        TableColumn lastPriceCol = new TableColumn("Last price");
        lastPriceCol.setCellValueFactory(new PropertyValueFactory<>("lastPrice"));
        lastPriceCol.setCellFactory(col -> new PriceTableCell());
        lastPriceCol.setPrefWidth(150);

        TableColumn p1ChangeCol = new TableColumn(TimeConverter.secondsToMinutesHoursDays(timePeriods[0]) +" change");
        p1ChangeCol.setCellValueFactory(new PropertyValueFactory<>("P1PercentPriceChange"));
        p1ChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p1ChangeCol.setPrefWidth(100);

        TableColumn p1RelativeChangeCol = new TableColumn(TimeConverter.secondsToMinutesHoursDays(timePeriods[0])+" relative");
        p1RelativeChangeCol.setCellValueFactory(new PropertyValueFactory<>("P1RelativePriceChange"));
        p1RelativeChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p1RelativeChangeCol.setPrefWidth(100);

        TableColumn p2ChangeCol = new TableColumn(TimeConverter.secondsToMinutesHoursDays(timePeriods[1])+" change");
        p2ChangeCol.setCellValueFactory(new PropertyValueFactory<>("P2PercentPriceChange"));
        p2ChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p2ChangeCol.setPrefWidth(100);

        TableColumn p2RelativeChangeCol = new TableColumn(TimeConverter.secondsToMinutesHoursDays(timePeriods[1])+" relative");
        p2RelativeChangeCol.setCellValueFactory(new PropertyValueFactory<>("P2RelativePriceChange"));
        p2RelativeChangeCol.setCellFactory(col -> new PriceChangesTableCell());
        p2RelativeChangeCol.setPrefWidth(100);

        currenciesTableView.setItems(pairPriceChangesObservableList);
        currenciesTableView.getColumns().addAll(pairNameCol,lastPriceCol,p1ChangeCol,p1RelativeChangeCol,p2ChangeCol,p2RelativeChangeCol);

        currenciesTableView.managedProperty().bind(currenciesTableView.visibleProperty());
        currenciesTableView.setVisible(CURRENCIES_TABLE_VISIBLE);
        showTableCheckBox.setSelected(CURRENCIES_TABLE_VISIBLE);

        currenciesTableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Node node = ((Node) event.getTarget()).getParent();
                if (node instanceof TableRow || node.getParent() instanceof TableRow) {
                    CryptonoseGuiBrowser.runBrowser(((PairPriceChanges)currenciesTableView.getSelectionModel().getSelectedItem()).getPairName(),exchangeSpecs);
                }
            }
        });
    }

    private void initShortcuts() {
        mainVBox.setOnKeyPressed(event -> {
            if(event.getCode()==KeyCode.S) {
                soundCheckBox.setSelected(!soundCheckBox.isSelected());
                return;
            }
            if(event.getCode()==KeyCode.B) {
                runBrowserCheckBox.setSelected(!runBrowserCheckBox.isSelected());
                return;
            }
            if(event.getCode()==KeyCode.N) {
                notificationCheckBox.setSelected(!notificationCheckBox.isSelected());
                return;
            }
            if(event.getCode()==KeyCode.T) {
                showTableCheckBox.setSelected(!showTableCheckBox.isSelected());
                return;
            }
        });
    }

    @FXML
    public void pairsClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiPairs.fxml"));
            Parent root = fxmlLoader.load();
            ((CryptonoseGuiPairsController)fxmlLoader.getController()).setExchange(exchangeSpecs);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch(IOException e) {
            throw new Error(e);
        }
    }

    @FXML
    public void alertSettingsClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiAlertSettings.fxml"));
            Parent root = fxmlLoader.load();
            ((CryptonoseGuiAlertSettingsController)fxmlLoader.getController()).init(exchangeSpecs,timePeriods);
            Stage stage = new Stage();
            stage.setTitle("Alerts settings: " + exchangeSpecs.getName());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            initPriceAlertThresholds();
        } catch(IOException e) {
            throw new Error(e);
        }
    }

    private void setConnectionStatus(CryptonoseGuiConnectionStatus cryptonoseGuiConnectionStatus) {
        javafx.application.Platform.runLater(() -> {
            currentConnectionStatus=cryptonoseGuiConnectionStatus;
            connectionStatusLabel.setText(cryptonoseGuiConnectionStatus.getText());
            graphicsPane.setStyle("-fx-background-color: " + cryptonoseGuiConnectionStatus.getColor());
        });
        CryptonoseGuiNotification.notifyConnectionState(NOTIFICATION_LIBRARY,exchangeSpecs,cryptonoseGuiConnectionStatus);
    }

    @Override
    public void message(EngineMessage msg) {
        if(msg.getCode()==EngineMessage.ERROR)
            consoleLog("Error: " + msg.getMessage());
        else
            consoleLog(msg.getMessage());
        switch(msg.getCode()) {
            case EngineMessage.CONNECTED:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTED);
                break;
            case EngineMessage.DISCONNECTED:
                setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED);
                break;
        }
    }

    @Override
    public void receiveTransactionHeartbeat() {
        lastTradeTimeMillis=System.currentTimeMillis();
        synchronized (numTradesPerSecondLock) {
            numTradesPerSecond++;
        }

    }

    @Override
    public void receiveChanges(List<PriceChanges> priceChangesList) {
        List<PriceAlert> priceAlerts = cryptonoseGuiAlertChecker.checkAlerts(priceChangesList);
        for(PriceAlert priceAlert : priceAlerts)
            handlePriceAlert(priceAlert);
        if(showTableCheckBox.isSelected()) {
            for (PriceChanges priceChanges : priceChangesList) {
                Iterator<PairPriceChanges> pairPriceChangesIterator = pairPriceChangesObservableList.iterator();
                PairPriceChanges pairPriceChanges = pairPriceChangesIterator.hasNext()?pairPriceChangesIterator.next():null;
                while(pairPriceChangesIterator.hasNext() && !pairPriceChanges.getPairName().equals(priceChanges.getCurrencyPair()))
                    pairPriceChanges = pairPriceChangesIterator.next();
                if(pairPriceChanges==null || !pairPriceChanges.getPairName().equals(priceChanges.getCurrencyPair())) {//not found
                    PairPriceChanges newPairPriceChanges = new PairPriceChanges(exchangeSpecs, priceChanges.getCurrencyPair());
                    pairPriceChangesObservableList.add(newPairPriceChanges);
                    pairPriceChanges = newPairPriceChanges;
                }
                int period = (priceChanges.getTimePeriodSeconds() == timePeriods[0]) ? PairPriceChanges.PERIOD1 : PairPriceChanges.PERIOD2;
                pairPriceChanges.setPriceChanges(priceChanges,period);
                if(System.currentTimeMillis()-lastTableUpdateMillis>TABLE_UPDATE_FREQUENCY_MILLIS) {
                    pairPriceChangesObservableList.set(pairPriceChangesObservableList.indexOf(pairPriceChanges), pairPriceChanges);
                    currenciesTableView.sort();
                    lastTableUpdateMillis=System.currentTimeMillis();
                }
            }
        }
    }

    //TODO ugly, refactor
    @Override
    public void receiveChanges(PriceChanges priceChanges) {
        //priceChanges.setRelativePriceChange(new Double(0.0));
        List<PriceChanges> priceChangesList = new LinkedList<>();
        priceChangesList.add(priceChanges);
        receiveChanges(priceChangesList);
    }

    private void handlePriceAlert(PriceAlert priceAlert) {
        priceAlertTabController.addAlert(priceAlert);
        String alertString = String.format("Price alert on %s, %s: change by %.2f (relative: %.2f) during last %d minutes, final price: %.8f",
                priceAlert.getFormattedPair(),
                exchangeSpecs.getName(),
                priceAlert.getPriceChange(),
                priceAlert.getRelativePriceChange(),
                priceAlert.getPeriodSeconds()/60,
                priceAlert.getFinalPrice());
        consoleLog(alertString);
        if (runBrowserCheckBox.isSelected()) {
            CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs());
        }
        if(notificationCheckBox.isSelected()) {
            CryptonoseGuiNotification.notifyPriceAlert(NOTIFICATION_LIBRARY,priceAlert,()->CryptonoseGuiBrowser.runBrowser(priceAlert.getPair(),priceAlert.getExchangeSpecs()));
            //showNotification(priceAlert);
        }
        if (soundCheckBox.isSelected()) {
            cryptonoseGuiSoundAlerts.soundAlert(priceAlert);
        }
    }

    private void initPriceAlertThresholds() {
        logger.info("updating alerts values...");
        for(long currentTimePeriod : timePeriods) {
            priceAlertThresholdsMap.put(new Long(currentTimePeriod),
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
                if(lastTradeSecondsAgo>NO_TRADES_PERIOD_SECONDS_TO_SET_DISCONNECTED) {
                    if (!currentConnectionStatus.equals(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED))
                        setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED);
                } else if(currentConnectionStatus.equals(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED))
                    setConnectionStatus(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_CONNECTED);
                javafx.application.Platform.runLater(() -> {
                    lastTradeLabel.setText(String.valueOf(lastTradeSecondsAgo) + " seconds ago");
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
}
