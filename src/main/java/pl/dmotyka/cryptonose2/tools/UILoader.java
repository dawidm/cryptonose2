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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;

public class UILoader <T> {

    private static Integer FONT_SIZE = null;

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
        if (FONT_SIZE == null && !CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_FONT_SIZE)) {
            FONT_SIZE = CryptonoseSettings.getInt(CryptonoseSettings.General.FONT_SIZE_PX);
        }
        if (FONT_SIZE != null)
            root.setStyle(String.format("-fx-font-size: %dpx;", FONT_SIZE));
        root.getStylesheets().add(UILoader.class.getClassLoader().getResource("style.css").toExternalForm());
        if (CryptonoseSettings.getBool(CryptonoseSettings.General.DARK_MODE)) {
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource("style-dark.css").toExternalForm());
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource("style-colors-dark.css").toExternalForm());
        } else {
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource("style-light.css").toExternalForm());
            root.getStylesheets().add(UILoader.class.getClassLoader().getResource("style-colors-light.css").toExternalForm());
        }
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
        stage.setScene(new Scene(fxmlLoader.getRoot()));
        return stage;
    }

}
