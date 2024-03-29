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

/**
 * Created by dawid on 8/1/17.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.dataobj.CryptonosePairData;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.cryptonose2.tools.ObservableListAggregate;
import pl.dmotyka.cryptonose2.tools.UILoader;
import pl.dmotyka.cryptonose2.updatechecker.GetVersionException;
import pl.dmotyka.cryptonose2.updatechecker.UpdateChecker;
import pl.dmotyka.cryptonose2.updatechecker.VersionInfo;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.exchangespecs.NoSuchExchangeException;

public class CryptonoseGuiController extends Application {

    public static final Logger logger = Logger.getLogger(CryptonoseGuiController.class.getName());

    public static final double MAIN_WINDOW_WIDTH_DEF_MULTIPLIER = 0.5;
    public static final double MAIN_WINDOW_HEIGHT_DEF_MULTIPLIER = 0.7;

    @FXML
    public VBox mainVbox;
    @FXML
    public TabPane mainTabPane;
    @FXML
    public CheckBox soundCheckBox;
    @FXML
    public CheckBox runBrowserCheckBox;
    @FXML
    public CheckBox notificationCheckBox;
    @FXML
    public MenuButton addExchangeMenuButton;
    @FXML
    public Button settingsButton;
    @FXML
    public Button helpButton;
    @FXML
    public CheckBox powerSaveCheckBox;
    @FXML
    public HBox newVersionHBox;
    @FXML
    public Label newVersionShowLabel;
    @FXML
    public Label newVersionHideLabel;
    @FXML
    public TitledPane findTitledPane;
    @FXML
    public TableView<CryptonosePairData> findTableView;
    @FXML
    public TextField findTextField;
    @FXML
    public HBox pinnedHBox;

    private PinnedTickersHBox pinnedTickersHBox;

    private final Map<ExchangeSpecs, CryptonoseGuiExchangeController> activeExchangesControllersMap = new HashMap<>();
    private final ObservableListAggregate<CryptonosePairData> cnPairDataAggregate = new ObservableListAggregate<>();

    private CryptonoseGuiNotification cryptonoseGuiNotification;

    private CryptonoseGuiPriceAlertsTabController priceAlertsTabController;

    private Scene mainScene;

    private Stage primaryStage;

    public static ExchangeSpecs[] exchangeSpecss;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        exchangeSpecss = ExchangeSpecs.getAll();
        logger.fine("Javafx output scale X: " + Screen.getScreens().get(0).getOutputScaleX());
        Locale.setDefault(Locale.US);
        checkVersion();
        this.primaryStage=primaryStage;
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        UILoader<CryptonoseGuiController> uiLoader = new UILoader<>("cryptonoseGui.fxml", this);
        Node cryptonoseGuiFxNode = uiLoader.getRoot();
        primaryStage.setTitle("Cryptonose");

        String activeExchanges = CryptonoseSettings.getString(CryptonoseSettings.GuiState.ACTIVE_EXCHANGES);
        String[] loadedExchanges = activeExchanges.split(",");
        List<ExchangeSpecs> loadedExchangesList = new ArrayList<>();
        for(String currentLoadedExchange : loadedExchanges) {
            try {
                ExchangeSpecs exchangeSpecs = ExchangeSpecs.fromStringName(currentLoadedExchange);
                loadedExchangesList.add(exchangeSpecs);
            } catch (NoSuchExchangeException e) {
                logger.warning(e.getLocalizedMessage());
            }
        }

        primaryStage.getIcons().addAll(
                new Image(getClass().getClassLoader().getResourceAsStream("icon_new_16.png")),
                new Image(getClass().getClassLoader().getResourceAsStream("icon_new_32.png")),
                new Image(getClass().getClassLoader().getResourceAsStream("icon_new_64.png")),
                new Image(getClass().getClassLoader().getResourceAsStream("icon_new_128.png")),
                new Image(getClass().getClassLoader().getResourceAsStream("icon_new_256.png")));

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        if(CryptonoseSettings.getBool(CryptonoseSettings.GuiState.MAIN_IS_MAXIMIZED)) {
            primaryStage.setMaximized(true);
        } else {
            double mainWidth, mainHeight, mainX, mainY;
            mainWidth = CryptonoseSettings.getDouble(CryptonoseSettings.GuiState.MAIN_WIDTH);
            if (mainWidth == 0.0)
                mainWidth = primaryScreenBounds.getWidth() * MAIN_WINDOW_WIDTH_DEF_MULTIPLIER;
            mainHeight = CryptonoseSettings.getDouble(CryptonoseSettings.GuiState.MAIN_HEIGHT);
            if (mainHeight == 0.0)
                mainHeight = primaryScreenBounds.getHeight() * MAIN_WINDOW_HEIGHT_DEF_MULTIPLIER;
            primaryStage.setWidth(mainWidth);
            primaryStage.setHeight(mainHeight);
            mainX = CryptonoseSettings.getDouble(CryptonoseSettings.GuiState.MAIN_X);
            mainY = CryptonoseSettings.getDouble(CryptonoseSettings.GuiState.MAIN_Y);
            if (mainX < 0.0 || mainY < 0.0)
                primaryStage.centerOnScreen();
            else {
                primaryStage.setX(mainX);
                primaryStage.setY(mainY);
            }
        }

        mainScene = UILoader.createScene((VBox) cryptonoseGuiFxNode);

        mainScene.setOnKeyPressed(event -> {
            if(mainTabPane.getSelectionModel().getSelectedItem().getContent().getOnKeyPressed()!=null)
                mainTabPane.getSelectionModel().getSelectedItem().getContent().getOnKeyPressed().handle(event);
            if (event.getCode() == KeyCode.E) {
                if (mainTabPane.getSelectionModel().getSelectedIndex() == mainTabPane.getTabs().size() - 1)
                    mainTabPane.getSelectionModel().selectFirst();
                else
                    mainTabPane.getSelectionModel().selectNext();
            }
        });

        primaryStage.setScene(mainScene);
        primaryStage.setOnCloseRequest(handler -> {
            saveSceneSizePosition();
            saveExchangesList();
            saveOtherSettings();
            System.exit(0);
        });

        cryptonoseGuiNotification = new CryptonoseGuiNotification(CryptonoseSettings.NOTIFICATION_LIBRARY);
        cryptonoseGuiNotification.setAnchorHelperWindow(mainScene.getWindow());

        UILoader<CryptonoseGuiPriceAlertsTabController> uiLoaderAlerts = new UILoader<>("cryptonoseGuiPriceAlertsTab.fxml");
        Node priceAlertsPane = uiLoaderAlerts.getRoot();
        priceAlertsTabController = uiLoaderAlerts.getController();
        priceAlertsTabController.setAlertBlockListener(alertBlock -> activeExchangesControllersMap.get(alertBlock.getExchangeSpecs()).blockAlert(alertBlock));
        priceAlertsTabController.setBlocksSettingsListener(exchangeSpecs -> activeExchangesControllersMap.get(exchangeSpecs).showAlertBlocksDialog());
        Tab priceAlertsTab = new Tab("Price alerts",priceAlertsPane);
        priceAlertsTab.setClosable(false);
        mainTabPane.getTabs().add(priceAlertsTab);

        soundCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.GuiState.ALERT_SOUND));
        runBrowserCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.GuiState.ALERT_BROWSER));
        notificationCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.GuiState.ALERT_NOTIFICATION));

        for (ExchangeSpecs exchangeSpecs : loadedExchangesList) {
            loadExchange(exchangeSpecs,false);
        }
        checkIfAllExchangesLoaded();

        soundCheckBox.setOnAction(event -> {
            globalEnableSound(soundCheckBox.isSelected());
        });
        runBrowserCheckBox.setOnAction(event -> {
            globalEnableBrowser(runBrowserCheckBox.isSelected());
        });
        notificationCheckBox.setOnAction(event -> {
            globalEnableNotif(notificationCheckBox.isSelected());
        });
        powerSaveCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.GuiState.POWER_SAVE));
        for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
            cryptonoseGuiExchangeController.enablePowerSave(powerSaveCheckBox.isSelected());
        }
        powerSaveCheckBox.setOnAction(event -> {
            enablePowerSave(powerSaveCheckBox.isSelected());
        });
        addExchangeMenuButton.setOnShowing(event -> addExchangeClick());
        addExchangeMenuButton.setOnAction(event -> removeExchangesButtonShadow());
        settingsButton.setOnAction(event -> settingsClick());
        helpButton.setOnAction(event -> helpClick());

        findTitledPane.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded)
                Platform.runLater(this::showFindPane);
        });

        primaryStage.show();
        initShortcuts();

        pinnedHBox.visibleProperty().bind(powerSaveCheckBox.selectedProperty().not());
        pinnedHBox.managedProperty().bind(powerSaveCheckBox.selectedProperty().not());

        pinnedTickersHBox = new PinnedTickersHBox(pinnedHBox, cnPairDataAggregate);

    }

    private void checkVersion() {
        Platform.runLater(() -> {
            newVersionHBox.setVisible(false);
            newVersionHBox.setManaged(false);
        });
        Executors.newCachedThreadPool().submit(() -> {
            VersionInfo versionInfo = null;
            try {
                versionInfo = UpdateChecker.getNewVersionURLOrNull();
                if (versionInfo==null)
                    return;
                String hiddenNewVersion = CryptonoseSettings.getString(CryptonoseSettings.GuiState.HIDDEN_NEW_VERSION);
                if (hiddenNewVersion == null || !hiddenNewVersion.equals(versionInfo.getVersionString())) {
                    VersionInfo finalVersionInfo = versionInfo;
                    Platform.runLater(() -> {
                        newVersionShowLabel.setOnMouseClicked(e -> {
                            versionWindow(finalVersionInfo);
                            CryptonoseSettings.putString(CryptonoseSettings.GuiState.HIDDEN_NEW_VERSION, finalVersionInfo.getVersionString());
                            newVersionHBox.setVisible(false);
                            newVersionHBox.setManaged(false);
                        });
                        newVersionHideLabel.setOnMouseClicked(e -> {
                            CryptonoseSettings.putString(CryptonoseSettings.GuiState.HIDDEN_NEW_VERSION, finalVersionInfo.getVersionString());
                            newVersionHBox.setVisible(false);
                            newVersionHBox.setManaged(false);
                        });
                        newVersionHBox.setVisible(true);
                        newVersionHBox.setManaged(true);
                });
                }
            } catch (IOException | GetVersionException e) {
                logger.log(Level.WARNING,"when checking version",e);
            }
        });
    }

    private void versionWindow(VersionInfo versionInfo) {
        UILoader<CryptonoseGuiVersionWindowController> uiLoader = new UILoader<>("cryptonoseGuiVersionWindow.fxml");
        CryptonoseGuiVersionWindowController cryptonoseGuiVersionWindowController = uiLoader.getController();
        cryptonoseGuiVersionWindowController.init(versionInfo);
        uiLoader.stageShowAndWait("New version");
    }

    private void initShortcuts() {
        mainVbox.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.S) {
                    if (soundCheckBox.isIndeterminate())
                        soundCheckBox.setSelected(true);
                    else
                        soundCheckBox.setSelected(!soundCheckBox.isSelected());
                    globalEnableSound(soundCheckBox.isSelected());
                    return;
                }
                if (event.getCode() == KeyCode.B) {
                    if (runBrowserCheckBox.isIndeterminate())
                        runBrowserCheckBox.setSelected(true);
                    else
                        runBrowserCheckBox.setSelected(!runBrowserCheckBox.isSelected());
                    globalEnableBrowser(runBrowserCheckBox.isSelected());
                    return;
                }
                if (event.getCode() == KeyCode.N) {
                    if (notificationCheckBox.isIndeterminate())
                        notificationCheckBox.setSelected(true);
                    else
                        notificationCheckBox.setSelected(!notificationCheckBox.isSelected());
                    globalEnableNotif(notificationCheckBox.isSelected());
                    return;
                }
                if (event.getCode() == KeyCode.P) {
                    powerSaveCheckBox.setSelected(!powerSaveCheckBox.isSelected());
                    enablePowerSave(powerSaveCheckBox.isSelected());
                    return;
                }
            }
            if (event.getCode() == KeyCode.F) {
                event.consume();
                if (findTitledPane.isExpanded()) {
                    Platform.runLater(() -> findTextField.requestFocus());
                } else {
                    findTitledPane.setExpanded(true);
                }
            }
        });
    }

    private void removeExchangesButtonShadow() {
        addExchangeMenuButton.setEffect(null);
    }

    public void loadExchange(ExchangeSpecs exchangeSpecs, boolean activate) {
        logger.info("loading "+ exchangeSpecs.getName());
        if(activeExchangesControllersMap.keySet().contains(exchangeSpecs))
            return;
        UILoader<CryptonoseGuiExchangeController> exchangeLoader = new UILoader<>("cryptonoseGuiExchange.fxml");
        Node cryptonoseGuiNode = exchangeLoader.getRoot();
        Tab tab = new Tab(exchangeSpecs.getName());
        ColorIndicatorBox indicatorBox = new ColorIndicatorBox(1);
        tab.setGraphic(indicatorBox);
        tab.setContent(cryptonoseGuiNode);
        mainTabPane.getTabs().add(tab);
        mainTabPane.getTabs().sort((o1, o2) -> {
            if (!o1.isClosable()) {
                return -1;
            }
            if (!o2.isClosable()) {
                return 1;
            }
            return o1.getText().compareTo(o2.getText());
        });
        if(activate)
            mainTabPane.getSelectionModel().select(tab);
        CryptonoseGuiExchangeController cryptonoseGuiExchangeController = exchangeLoader.getController();
        cryptonoseGuiExchangeController.init(exchangeSpecs,priceAlertsTabController,this, indicatorBox, cryptonoseGuiNotification);
        final ObservableList<CryptonosePairData> readOnlyPairsData = cryptonoseGuiExchangeController.getPairsData();
        cnPairDataAggregate.addList(readOnlyPairsData);
        tab.setOnCloseRequest((event) -> {
            logger.info("closing tab and disconnecting: " + exchangeSpecs.getName());
            cnPairDataAggregate.removeList(readOnlyPairsData);
            new Thread(cryptonoseGuiExchangeController::close).start();
            activeExchangesControllersMap.remove(exchangeSpecs);
            checkIfAllExchangesLoaded();
        });
        cryptonoseGuiExchangeController.soundCheckBox.setSelected(soundCheckBox.isSelected());
        cryptonoseGuiExchangeController.runBrowserCheckBox.setSelected(runBrowserCheckBox.isSelected());
        cryptonoseGuiExchangeController.notificationCheckBox.setSelected(notificationCheckBox.isSelected());
        cryptonoseGuiExchangeController.enablePowerSave(powerSaveCheckBox.isSelected());
        activeExchangesControllersMap.put(exchangeSpecs, cryptonoseGuiExchangeController);
        removeExchangesButtonShadow();
    }

    void checkIfAllExchangesLoaded() {
        if (activeExchangesControllersMap.size() == exchangeSpecss.length) {
            addExchangeMenuButton.setDisable(true);
        } else {
            addExchangeMenuButton.setDisable(false);
        }
    }

    private void showFindPane() {
        ObservableList<CryptonosePairData> allPairsObservableList = cnPairDataAggregate.getObservableAggregate();
        FilteredList<CryptonosePairData> filteredPairsList = new FilteredList<>(allPairsObservableList);
        SortedList<CryptonosePairData> sortedPairsList = new SortedList<>(filteredPairsList, Comparator.comparing(item -> {
            if (item.pinnedProperty().get()) {
                return "a" + item.getFormattedPairName() + item.getExchangeSpecs().getName();
            } else {
                return "b" + item.getFormattedPairName() + item.getExchangeSpecs().getName();
            }
        }));
        findTextField.textProperty().addListener((observable, oldValue, newValue) -> filteredPairsList.setPredicate(item -> item.getFormattedPairName().toLowerCase().contains(newValue.toLowerCase())));
        findTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE)
                findTitledPane.setExpanded(false);
            if (event.getCode() == KeyCode.DOWN) {
                if (!findTableView.getItems().isEmpty()) {
                    findTableView.getSelectionModel().select(0, findTableView.getColumns().get(0));
                    findTableView.scrollTo(0);
                    findTableView.requestFocus();
                }
            }
            if (event.getCode() == KeyCode.ENTER) {
                if (!findTableView.getItems().isEmpty()) {
                    findTableView.getSelectionModel().select(0);
                    findTableView.scrollTo(0);
                    findTableView.requestFocus();
                    findTableView.getOnKeyPressed().handle(event);
                    Platform.runLater(() -> findTitledPane.setExpanded(false));
                }
            }
        });
        findTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE)
                findTitledPane.setExpanded(false);
            if (event.getCode() == KeyCode.BACK_SPACE) {
                findTextField.requestFocus();
                findTextField.getOnKeyPressed().handle(event);
            }
            if (event.getCode() == KeyCode.UP) {
                if (findTableView.getSelectionModel().getSelectedIndex() == 0 || findTableView.getItems().isEmpty())
                    findTextField.requestFocus();
            }
        });
        findTextField.requestFocus();
        findTextField.setText("");
        PriceChangesTable priceChangesTable = new PriceChangesTable(findTableView, sortedPairsList, CryptonoseSettings.TIME_PERIODS);
        priceChangesTable.enableShowExchange();
        priceChangesTable.enablePinnedCheckboxes();
        priceChangesTable.disablePluginButtonsFocusTraversable();
        priceChangesTable.init();
    }

    private void saveSceneSizePosition() {
        if(mainScene!=null) {
            CryptonoseSettings.putBool(CryptonoseSettings.GuiState.MAIN_IS_MAXIMIZED, primaryStage.isMaximized());
            CryptonoseSettings.putDouble(CryptonoseSettings.GuiState.MAIN_WIDTH, primaryStage.getWidth());
            CryptonoseSettings.putDouble(CryptonoseSettings.GuiState.MAIN_HEIGHT, primaryStage.getHeight());
            CryptonoseSettings.putDouble(CryptonoseSettings.GuiState.MAIN_X, primaryStage.getX());
            CryptonoseSettings.putDouble(CryptonoseSettings.GuiState.MAIN_Y, primaryStage.getY());
        }
    }

    private void saveExchangesList() {
        String activeExchangesString= activeExchangesControllersMap.keySet().stream().map(activeExchange -> activeExchange.getName()).collect(Collectors.joining(","));
        CryptonoseSettings.putString(CryptonoseSettings.GuiState.ACTIVE_EXCHANGES, activeExchangesString);
    }

    private void saveOtherSettings() {
        CryptonoseSettings.putBool(CryptonoseSettings.GuiState.POWER_SAVE, powerSaveCheckBox.isSelected());
        CryptonoseSettings.putBool(CryptonoseSettings.GuiState.ALERT_SOUND, soundCheckBox.isSelected());
        CryptonoseSettings.putBool(CryptonoseSettings.GuiState.ALERT_BROWSER, runBrowserCheckBox.isSelected());
        CryptonoseSettings.putBool(CryptonoseSettings.GuiState.ALERT_NOTIFICATION, notificationCheckBox.isSelected());
    }

    public void addExchangeClick() {
        Set<ExchangeSpecs> allExchanges = new HashSet<>(Arrays.asList(exchangeSpecss));
        allExchanges.removeAll(activeExchangesControllersMap.keySet());
        addExchangeMenuButton.getItems().clear();
        ExchangeSpecs[] allExchangesSorted = Arrays.stream(allExchanges.toArray()).
                sorted(Comparator.comparing(Object::toString)).toArray(ExchangeSpecs[]::new);
        for (var exchange: allExchangesSorted) {
            MenuItem item = new MenuItem(exchange.getName());
            item.setOnAction(e -> {
                loadExchange(exchange, true);
                checkIfAllExchangesLoaded();
            });
            addExchangeMenuButton.getItems().add(item);
        }
    }

    public void settingsClick() {
        UILoader<CryptonoseGuiSettingsController> uiLoader = new UILoader<>("cryptonoseGuiSettings.fxml");
        settingsButton.setDisable(true);
        uiLoader.stageShowAndWait("Cryptonose2 settings");
        settingsButton.setDisable(false);
    }

    public void helpClick() {
        UILoader<CryptonoseGuiHelpWindowController> uiLoader = new UILoader<>("cryptonoseGuiHelpWindow.fxml");
        uiLoader.getController().init();
        helpButton.setDisable(true);
        uiLoader.stageShowAndWait("Help");
        helpButton.setDisable(false);
    }

    public void updateCheckboxes() {
        soundCheckBox.setAllowIndeterminate(true);
        soundCheckBox.setIndeterminate(false);
        runBrowserCheckBox.setAllowIndeterminate(true);
        runBrowserCheckBox.setIndeterminate(false);
        notificationCheckBox.setAllowIndeterminate(true);
        notificationCheckBox.setIndeterminate(false);
        int soundSum = 0;
        int browserSum = 0;
        int notifSum = 0;
        int numExchanges = activeExchangesControllersMap.size();
        for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
            if (cryptonoseGuiExchangeController.getIsSoundEnabled())
                soundSum++;
            if (cryptonoseGuiExchangeController.getIsBrowserEnabled())
                browserSum++;
            if (cryptonoseGuiExchangeController.getIsNotifEnabled())
                notifSum++;
        }
        Map<CheckBox, Integer> checkboxSumMap = Map.of(soundCheckBox, soundSum, runBrowserCheckBox, browserSum, notificationCheckBox, notifSum);
        for (var entry: checkboxSumMap.entrySet()) {
            if (entry.getValue() == numExchanges) {
                entry.getKey().setSelected(true);
            }
            else if (entry.getValue() == 0) {
                entry.getKey().setSelected(false);
            }
            else
                entry.getKey().setIndeterminate(true);
        }
    }

    private void globalEnableSound(boolean enable) {
        soundCheckBox.setIndeterminate(false);
        soundCheckBox.setAllowIndeterminate(false);
        for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
            cryptonoseGuiExchangeController.enablePlaySound(enable);
        }
    }
    private void globalEnableBrowser(boolean enable) {
        runBrowserCheckBox.setIndeterminate(false);
        runBrowserCheckBox.setAllowIndeterminate(false);
        for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
            cryptonoseGuiExchangeController.enableRunBrowser(enable);
        }
    }
    private void globalEnableNotif(boolean enable) {
        notificationCheckBox.setIndeterminate(false);
        notificationCheckBox.setAllowIndeterminate(false);
        for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
            cryptonoseGuiExchangeController.enableNotification(enable);
        }
    }

    private void enablePowerSave(boolean selected) {
        for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
            cryptonoseGuiExchangeController.enablePowerSave(powerSaveCheckBox.isSelected());
        }
    }
}
