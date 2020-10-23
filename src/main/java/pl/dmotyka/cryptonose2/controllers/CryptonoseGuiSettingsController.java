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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFileFormat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.CryptonoseGuiBrowser;
import pl.dmotyka.cryptonose2.CryptonoseGuiSoundAlerts;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;

/**
 * Created by dawid on 8/8/17.
 */
public class CryptonoseGuiSettingsController implements Initializable {

    @FXML
    public TextField browserPathEditText;
    @FXML
    public Button selectBrowserButton;
    @FXML
    public TextField priceRisingSoundFileEditText;
    @FXML
    public TextField priceDroppingSoundFileEditText;
    @FXML
    public Button priceRisingSoundFileButton;
    @FXML
    public Button priceDroppingSoundFileButton;
    @FXML
    public HBox browserPathHBox;
    @FXML
    public Label supportedAudioFilesLabel;
    @FXML
    public CheckBox defBrowserCheckbox;
    @FXML
    public CheckBox defaultRisingSoundCheckBox;
    @FXML
    public CheckBox defaultDroppingSoundCheckBox;
    @FXML
    public CheckBox defFontCheckbox;
    @FXML
    public CheckBox darkStyleCheckbox;
    @FXML
    public Spinner<Integer> fontSizeSpinner;
    @FXML
    public CheckBox connStatusCheckbox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        browserPathEditText.setText(CryptonoseSettings.getString(CryptonoseSettings.General.BROWSER_PATH));
        if(CryptonoseGuiBrowser.isDefaultBrowserSupported()) {
            defBrowserCheckbox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_BROWSER));
        } else {
            defBrowserCheckbox.setSelected(true);
            defBrowserCheckbox.setText(defBrowserCheckbox.getText()+" (not supported)");
        }
        supportedAudioFilesLabel.textProperty().setValue(supportedAudioFilesLabel.getText()
                +Arrays.stream(CryptonoseGuiSoundAlerts.getAudioFileTypes()).map(type -> String.format("%s (*.%s)",type.toString(), type.getExtension())).collect(Collectors.joining(", ")));
        priceRisingSoundFileEditText.setText(CryptonoseSettings.getString(CryptonoseSettings.General.SOUND_RISING_FILE_PATH));
        priceDroppingSoundFileEditText.setText(CryptonoseSettings.getString(CryptonoseSettings.General.SOUND_DROPPING_FILE_PATH));
        defaultRisingSoundCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_RISING_SOUND));
        defaultDroppingSoundCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_DROPPING_SOUND));
        defFontCheckbox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_FONT_SIZE));
        darkStyleCheckbox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.General.DARK_MODE));
        connStatusCheckbox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.General.CONNECTION_STATUS_NOTIFICATIONS));
        Integer defFontSize = CryptonoseSettings.getInt(CryptonoseSettings.General.FONT_SIZE_PX);
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(CryptonoseSettings.FONT_MIN_SIZE, CryptonoseSettings.FONT_MAX_SIZE, defFontSize);
        fontSizeSpinner.setValueFactory(valueFactory);
        fontSizeSpinner.disableProperty().bind(defFontCheckbox.selectedProperty());
        browserPathEditText.disableProperty().bind(defBrowserCheckbox.selectedProperty());
        selectBrowserButton.disableProperty().bind(defBrowserCheckbox.selectedProperty());
        priceRisingSoundFileEditText.disableProperty().bind(defaultRisingSoundCheckBox.selectedProperty());
        priceRisingSoundFileButton.disableProperty().bind(defaultRisingSoundCheckBox.selectedProperty());
        priceDroppingSoundFileEditText.disableProperty().bind(defaultDroppingSoundCheckBox.selectedProperty());
        priceDroppingSoundFileButton.disableProperty().bind(defaultDroppingSoundCheckBox.selectedProperty());
    }

    public void selectBrowserClick(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select browser...");
        File file = fileChooser.showOpenDialog(null);
        if (file==null)
            return;
        try {
            String fileName = file.getCanonicalPath();
            if(file.canExecute()) {
                browserPathEditText.setText(fileName + " %s");
            }
            else {
                new CryptonoseAlert(CryptonoseAlert.AlertType.ERROR,"Selected file is not executable").show();
            }
        }
        catch (IOException e) {
            new CryptonoseAlert(CryptonoseAlert.AlertType.ERROR,"Error opening browser file").show();
        }
    }

    public void selectRisingSoundFileClick(ActionEvent actionEvent) {
        selectSoundFile(priceRisingSoundFileEditText);
    }

    public void selectDroppingSoundFileClick(ActionEvent actionEvent) {
        selectSoundFile(priceDroppingSoundFileEditText);
    }

    void selectSoundFile(TextField targetEditText) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select audio file...");
        for (AudioFileFormat.Type type : CryptonoseGuiSoundAlerts.getAudioFileTypes())
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(type.toString(),"*."+type.getExtension()));
        File file = fileChooser.showOpenDialog(null);
        if (file==null)
            return;
        try {
            if (file.canRead()) {
                targetEditText.setText(file.getCanonicalPath());
            }
            else {
                throw new IOException("Cannot read file: " + file.getCanonicalPath());
            }
        }
        catch (IOException e) {
            new CryptonoseAlert(CryptonoseAlert.AlertType.ERROR,"Selected file is not readable").show();
        }
    }

    public void cancelClick(ActionEvent actionEvent) {
        closeStage(actionEvent);
    }

    public void saveClick(ActionEvent actionEvent) {
        CryptonoseSettings.putBool(CryptonoseSettings.General.USE_DEF_BROWSER, defBrowserCheckbox.isSelected());
        CryptonoseSettings.putString(CryptonoseSettings.General.BROWSER_PATH, browserPathEditText.getText());
        CryptonoseSettings.putBool(CryptonoseSettings.General.USE_DEF_RISING_SOUND, defaultRisingSoundCheckBox.isSelected());
        CryptonoseSettings.putBool(CryptonoseSettings.General.USE_DEF_DROPPING_SOUND, defaultDroppingSoundCheckBox.isSelected());
        CryptonoseSettings.putString(CryptonoseSettings.General.SOUND_RISING_FILE_PATH, priceRisingSoundFileEditText.getText());
        CryptonoseSettings.putString(CryptonoseSettings.General.SOUND_DROPPING_FILE_PATH, priceDroppingSoundFileEditText.getText());
        CryptonoseSettings.putBool(CryptonoseSettings.General.USE_DEF_FONT_SIZE, defFontCheckbox.isSelected());
        CryptonoseSettings.putInt(CryptonoseSettings.General.FONT_SIZE_PX, fontSizeSpinner.getValue());
        CryptonoseSettings.putBool(CryptonoseSettings.General.DARK_MODE, darkStyleCheckbox.isSelected());
        CryptonoseSettings.putBool(CryptonoseSettings.General.CONNECTION_STATUS_NOTIFICATIONS, connStatusCheckbox.isSelected());
        closeStage(actionEvent);
    }

    private void closeStage(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

}
