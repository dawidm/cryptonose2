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

package pl.dmotyka.cryptonose2.tools;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;

public class UILoader <T> {

    private static final String STYLE_CSS_FILE = "style.css";
    private static final String STYLE_DARK_CSS_FILE = "style-dark.css";
    private static final String STYLE_LIGHT_CSS_FILE = "style-light.css";
    private static final String STYLE_COLORS_DARK_CSS_FILE = "style-colors-dark.css";
    private static final String STYLE_COLORS_LIGHT_CSS_FILE = "style-colors-light.css";

    private final FXMLLoader fxmlLoader;

    public UILoader(String resourcesFXMLPath) {
        this(resourcesFXMLPath, null);
    }

    public UILoader(String resourcesFXMLPath, T controller) {
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(resourcesFXMLPath));
        if (controller != null)
            fxmlLoader.setController(controller);
        try {
            fxmlLoader.load();
            applyStyles(fxmlLoader.getRoot());
        } catch (IOException e) {
            throw new RuntimeException("cannot load FXML");
        }
    }

    public static void applyStyles(Parent root) {
        Runnable setFontRunnable = () -> {
            if (!CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_FONT_SIZE)) {
                int fontSize = CryptonoseSettings.getInt(CryptonoseSettings.General.FONT_SIZE_PX);
                Platform.runLater(() -> {
                    root.setStyle(String.format("-fx-font-size: %dpx;", fontSize));
                });
            } else {
                Platform.runLater(() -> {
                    root.setStyle("");
                });
            }
        };
        setFontRunnable.run();
        CryptonoseSettings.runOnPreferenceChange(null, CryptonoseSettings.General.USE_DEF_FONT_SIZE, setFontRunnable);
        root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_CSS_FILE).toExternalForm());
        if (CryptonoseSettings.getBool(CryptonoseSettings.General.DARK_MODE)) {
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_DARK_CSS_FILE).toExternalForm());
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_COLORS_DARK_CSS_FILE).toExternalForm());
        } else {
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_LIGHT_CSS_FILE).toExternalForm());
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_COLORS_LIGHT_CSS_FILE).toExternalForm());
        }
        CryptonoseSettings.runOnPreferenceChange(null, CryptonoseSettings.General.DARK_MODE, () -> {
            Platform.runLater(() -> {
                if (CryptonoseSettings.getBool(CryptonoseSettings.General.DARK_MODE)) {
                    root.getStylesheets().removeIf(s -> s.endsWith(STYLE_LIGHT_CSS_FILE));
                    root.getStylesheets().removeIf(s -> s.endsWith(STYLE_COLORS_LIGHT_CSS_FILE));
                    root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_DARK_CSS_FILE).toExternalForm());
                    root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_COLORS_DARK_CSS_FILE).toExternalForm());
                } else {
                    root.getStylesheets().removeIf(s -> s.endsWith(STYLE_DARK_CSS_FILE));
                    root.getStylesheets().removeIf(s -> s.endsWith(STYLE_COLORS_DARK_CSS_FILE));
                    root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_LIGHT_CSS_FILE).toExternalForm());
                    root.getStylesheets().add(UILoader.class.getClassLoader().getResource(STYLE_COLORS_LIGHT_CSS_FILE).toExternalForm());
                }
            });
        });
    }

    public Parent getRoot() {
       return fxmlLoader.getRoot();
    }

    public T getController() {
        return fxmlLoader.getController();
    }

    public void setController(T controller) {
        fxmlLoader.setController(controller);
    }

    public void stageShow(String windowTitle) {
        createStage(windowTitle).show();
    }

    public void stageShowAndWait(String windowTitle) {
        createStage(windowTitle).showAndWait();
    }

    private Stage createStage(String windowTitle) {
        Stage stage = new Stage();
        stage.setTitle(windowTitle);
        Scene newScene = createScene(fxmlLoader.getRoot());
        stage.setScene(newScene);
        newScene.heightProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            stage.sizeToScene();
            stage.centerOnScreen();
        }));
        return stage;
    }

    public static Scene createScene(Parent root) {
        Scene newScene = new Scene(root);
        newScene.getStylesheets().add(UILoader.class.getClassLoader().getResource("controlsfx-notifications.css").toExternalForm());
        return newScene;
    }

}
