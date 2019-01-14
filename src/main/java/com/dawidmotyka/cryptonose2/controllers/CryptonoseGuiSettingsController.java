package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.cryptonose2.CryptonoseGuiSoundAlerts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Created by dawid on 8/8/17.
 */
public class CryptonoseGuiSettingsController implements Initializable{

    @FXML
    public TextField browserPathEditText;
    @FXML
    public TextField priceRisingSoundFileEditText;
    @FXML
    public TextField priceFallingSoundFileEditText;
    @FXML
    public HBox browserPathHBox;
    @FXML
    public Text supportedAudioFilesText;
    @FXML
    public CheckBox defBrowserCheckbox;

    private Preferences preferences;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        preferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("cryptonosePreferences");
        browserPathEditText.setText(preferences.get("browserPath", ""));
        priceRisingSoundFileEditText.setText(preferences.get("soundRisingPath", ""));
        priceFallingSoundFileEditText.setText(preferences.get("soundDroppingPath", ""));
        defBrowserCheckbox.setSelected(preferences.getBoolean("tryUseDefBrowser",true));
        browserPathHBox.setDisable(defBrowserCheckbox.isSelected());
        supportedAudioFilesText.textProperty().setValue(supportedAudioFilesText.getText()
                +Arrays.stream(CryptonoseGuiSoundAlerts.getAudioFileTypes()).map(type -> String.format("%s (*.%s)",type.toString(), type.getExtension())).collect(Collectors.joining(", ")));
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
                new Alert(Alert.AlertType.ERROR,"Selected file is not executable").show();
            }
        }
        catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,"Error opening browser file").show();
        }
    }

    public void selectRisingSoundFileClick(ActionEvent actionEvent) {
        selectSoundFile(priceRisingSoundFileEditText);
    }

    public void selectFallingSoundFileClick(ActionEvent actionEvent) {
        selectSoundFile(priceFallingSoundFileEditText);
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
                throw new IOException();
            }
        }
        catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,"Selected file is not readable").show();
        }
    }

    public void cancelClick(ActionEvent actionEvent) {
        closeStage(actionEvent);
    }

    public void saveClick(ActionEvent actionEvent) {
        preferences.put("browserPath", browserPathEditText.getText());
        preferences.put("soundRisingPath", priceRisingSoundFileEditText.getText());
        preferences.put("soundDroppingPath", priceFallingSoundFileEditText.getText());
        closeStage(actionEvent);
    }

    private void closeStage(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    public void defBrowserCheckboxOnAction(ActionEvent actionEvent) {
        if(((CheckBox)actionEvent.getSource()).isSelected())
            browserPathHBox.setDisable(true);
        else
            browserPathHBox.setDisable(false);
    }
}
