package com.dawidmotyka.cryptonose2.controllers;

/**
 * Created by dawid on 8/1/17.
 */

import com.dawidmotyka.cryptonose2.CryptonoseGuiConnectionStatus;
import com.dawidmotyka.exchangeutils.exchangespecs.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class CryptonoseGuiController extends Application {

    public static final Logger logger = Logger.getLogger(CryptonoseGuiController.class.getName());

    public static final double MAIN_WINDOW_WIDTH_DEF_MULTIPLIER = 0.5;
    public static final double MAIN_WINDOW_HEIGHT_DEF_MULTIPLIER = 0.7;
    public static final ExchangeSpecs[] EXCHANGE_SPECSS = new ExchangeSpecs[]{new PoloniexExchangeSpecs(), new BittrexExchangeSpecs(), new BinanceExchangeSpecs(),new XtbExchangeSpecs(), new BitfinexExchangeSpecs()};

    @FXML
    public TabPane mainTabPane;
    @FXML
    public CheckBox soundCheckBox;
    @FXML
    public CheckBox runBrowserCheckBox;
    @FXML
    public CheckBox notificationCheckBox;
    @FXML
    public Button addExchangeButton;
    @FXML
    public Button settingsButton;

    private final Map<ExchangeSpecs, CryptonoseGuiExchangeController> activeExchangesControllersMap = new HashMap<>();

    private CryptonoseGuiPriceAlertsTabController priceAlertsTabController;

    private Scene mainScene;

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage=primaryStage;
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        //StyleManager.getInstance().addUserAgentStylesheet("FlatBee.css");
        //StyleManager.getInstance().addUserAgentStylesheet("JMetroLightTheme.css");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGui.fxml"));
        fxmlLoader.setController(this);
        Node cryptonoseGuiFxNode = fxmlLoader.load();
        primaryStage.setTitle("Cryptonose2");
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        String activeExchanges=preferences.get("activeExchanges","");
        String[] loadedExchanges=activeExchanges.split(",");
        List<ExchangeSpecs> loadedExchangesList =new ArrayList<>(loadedExchanges.length);
        for(String currentLoadedExchange : loadedExchanges) {
            try {
                ExchangeSpecs exchangeSpecs = ExchangeSpecs.fromStringName(currentLoadedExchange);
                loadedExchangesList.add(exchangeSpecs);
            } catch (NoSuchExchangeException e) {
                logger.warning(e.getLocalizedMessage());
            }
        }
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon24.png")));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        if(preferences.getBoolean("mainIsMaximized",false)) {
            primaryStage.setMaximized(true);
        } else {
            double mainWidth, mainHeight, mainX, mainY;
            mainWidth = preferences.getDouble("mainWidth", primaryScreenBounds.getWidth() * MAIN_WINDOW_WIDTH_DEF_MULTIPLIER);
            mainHeight = preferences.getDouble("mainHeight", primaryScreenBounds.getHeight() * MAIN_WINDOW_HEIGHT_DEF_MULTIPLIER);
            primaryStage.setWidth(mainWidth);
            primaryStage.setHeight(mainHeight);
            mainX = preferences.getDouble("mainX", -1.0);
            mainY = preferences.getDouble("mainY", -1.0);
            if (mainX < 0.0 || mainY < 0.0)
                primaryStage.centerOnScreen();
            else {
                primaryStage.setX(mainX);
                primaryStage.setY(mainY);
            }
        }
        mainScene = new Scene((VBox) cryptonoseGuiFxNode);
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
            System.exit(0);
        });
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiPriceAlertsTab.fxml"));
        Node priceAlertsPane = fxmlLoader.load();
        priceAlertsTabController = fxmlLoader.getController();
        Tab priceAlertsTab = new Tab("Price alerts",priceAlertsPane);
        priceAlertsTab.setClosable(false);
        mainTabPane.getTabs().add(priceAlertsTab);
        for (ExchangeSpecs exchangeSpecs : loadedExchangesList) {
            loadExchange(exchangeSpecs,false);
        }
        soundCheckBox.setOnMouseClicked(event -> {
            for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
                cryptonoseGuiExchangeController.enablePlaySound(soundCheckBox.isSelected());
            }
        });
        runBrowserCheckBox.setOnMouseClicked(event -> {
            for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
                cryptonoseGuiExchangeController.enableRunBrowser(runBrowserCheckBox.isSelected());
            }
        });
        notificationCheckBox.setOnMouseClicked(event -> {
            for(CryptonoseGuiExchangeController cryptonoseGuiExchangeController : activeExchangesControllersMap.values()) {
                cryptonoseGuiExchangeController.enableNotification(notificationCheckBox.isSelected());
            }
        });
        addExchangeButton.setOnMouseClicked(event -> addExchangeClick());
        settingsButton.setOnMouseClicked(event -> settingsClick());
        primaryStage.show();
    }

    public void loadExchange(ExchangeSpecs exchangeSpecs, boolean activate) {
        try {
            logger.info("loading "+exchangeSpecs.getName());
            if(activeExchangesControllersMap.keySet().contains(exchangeSpecs))
                return;
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiExchange.fxml"));
            Node cryptonoseGuiNode = fxmlLoader.load();
            Tab tab = new Tab(exchangeSpecs.getName());
            Pane graphicsPane = new Pane();
            graphicsPane.setPrefWidth(10);
            graphicsPane.setPrefHeight(10);
            graphicsPane.setStyle("-fx-background-color: " + CryptonoseGuiConnectionStatus.CONNECTION_STATUS_DISCONNECTED.getColor());
            tab.setGraphic(graphicsPane);
            tab.setContent(cryptonoseGuiNode);
            mainTabPane.getTabs().add(tab);
            if(activate)
                mainTabPane.getSelectionModel().select(tab);
            CryptonoseGuiExchangeController cryptonoseGuiExchangeController = fxmlLoader.getController();
            cryptonoseGuiExchangeController.init(exchangeSpecs,priceAlertsTabController,graphicsPane);
            tab.setOnCloseRequest((event) -> {
                logger.info("closing tab and disconnecting: " + exchangeSpecs.getName());
                new Thread(()-> cryptonoseGuiExchangeController.close()).start();
                activeExchangesControllersMap.remove(exchangeSpecs);
            });
            cryptonoseGuiExchangeController.soundCheckBox.setSelected(soundCheckBox.isSelected());
            cryptonoseGuiExchangeController.runBrowserCheckBox.setSelected(runBrowserCheckBox.isSelected());
            cryptonoseGuiExchangeController.notificationCheckBox.setSelected(notificationCheckBox.isSelected());
            activeExchangesControllersMap.put(exchangeSpecs, cryptonoseGuiExchangeController);

        } catch (IOException e) {
            logger.log(Level.SEVERE,"when loading exchange",e);
            throw new Error();
        }
    }

    private void saveSceneSizePosition() {
        if(mainScene!=null) {
            Preferences preferences = Preferences.userNodeForPackage(this.getClass());
            preferences.putBoolean("mainIsMaximized",primaryStage.isMaximized());
            preferences.putDouble("mainWidth",primaryStage.getWidth());
            preferences.putDouble("mainHeight",primaryStage.getHeight());
            preferences.putDouble("mainX",primaryStage.getX());
            preferences.putDouble("mainY",primaryStage.getY());
        }
    }

    private void saveExchangesList() {
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        String activeExchangesString= activeExchangesControllersMap.keySet().stream().map(activeExchange -> activeExchange.getName()).collect(Collectors.joining(","));
        preferences.put("activeExchanges",activeExchangesString);
    }

    public void addExchangeClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiAddExchange.fxml"));
            Parent root = fxmlLoader.load();
            Set<ExchangeSpecs> allExchanges = new HashSet<>(Arrays.asList(EXCHANGE_SPECSS));
            allExchanges.removeAll(activeExchangesControllersMap.keySet());
            ((CryptonoseGuiAddExchangeController)fxmlLoader.getController()).init(this,allExchanges.toArray(new ExchangeSpecs[allExchanges.size()]));
            Stage stage = new Stage();
            stage.setTitle("Add exchange");
            stage.setScene(new Scene(root));
            addExchangeButton.setDisable(true);
            stage.showAndWait();
            addExchangeButton.setDisable(false);
        } catch(IOException e) {
            throw new Error(e);
        }
    }

    public void settingsClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("cryptonoseGuiSettings.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Cryptonose2 settings");
            stage.setScene(new Scene(root));
            settingsButton.setDisable(true);
            stage.showAndWait();
            settingsButton.setDisable(false);
        } catch(IOException e) {
            throw new Error(e);
        }
    }


}
