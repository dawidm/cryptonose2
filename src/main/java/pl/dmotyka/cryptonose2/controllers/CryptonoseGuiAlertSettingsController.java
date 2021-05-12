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
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.dataobj.PriceAlertThresholds;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.cryptonose2.settings.ExampleAlertThresholds;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.tools.TimeConverter;

/**
 * Created by dawid on 8/20/17.
 */
public class CryptonoseGuiAlertSettingsController implements Initializable {

    private static final double MIN_ALERT_THRESHOLD = 0;
    private static final double MAX_ALERT_THRESHOLD = 100;

    private static class DecimalInputChecker implements UnaryOperator<TextFormatter.Change> {

        private final double minValue;
        private final double maxValue;

        public DecimalInputChecker(double minValue, double maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public TextFormatter.Change apply(TextFormatter.Change change) {
            if (change.getControlNewText().isEmpty())
                return change;
            ParsePosition parsePosition = new ParsePosition( 0 );
            Number n = new DecimalFormat("#.#").parse(change.getControlNewText(), parsePosition);
            if (n == null || parsePosition.getIndex() < change.getControlNewText().length())
                return null;
            if (n.doubleValue() < minValue || n.doubleValue() > maxValue)
                return null;
            return change;
        }
    }

    @FXML
    public HBox mainHBox;
    @FXML
    public TextField p1requiredRisingTextField;
    @FXML
    public TextField p1requiredDroppingTextField;
    @FXML
    public TextField p1requiredRelativeRisingTextField;
    @FXML
    public TextField p1requiredRelativeDroppingTextField;
    @FXML
    public TextField p1sufficientRelativeRisingTextField;
    @FXML
    public TextField p1sufficientRelativeDroppingTextField;
    @FXML
    public Label p1titleLabel;
    @FXML
    public TextField p2requiredRisingTextField;
    @FXML
    public TextField p2requiredDroppingTextField;
    @FXML
    public TextField p2requiredRelativeRisingTextField;
    @FXML
    public TextField p2requiredRelativeDroppingTextField;
    @FXML
    public TextField p2sufficientRelativeRisingTextField;
    @FXML
    public TextField p2sufficientRelativeDroppingTextField;
    @FXML
    public Label p2titleLabel;
    @FXML
    public TitledPane p2periodSettingsTitledPane;
    @FXML
    public Button saveButton;
    @FXML
    public Slider cnLiquiditySlider;
    @FXML
    public CheckBox cnLiquidityCheckBox;
    @FXML
    public CheckBox blockSubsequentCheckBox;
    @FXML
    public CheckBox allowSubsequentCheckBox;
    @FXML
    public Button p1ExampleButtonLow;
    @FXML
    public Button p1ExampleButtonMed;
    @FXML
    public Button p1ExampleButtonHigh;
    @FXML
    public Button p2ExampleButtonLow;
    @FXML
    public Button p2ExampleButtonMed;
    @FXML
    public Button p2ExampleButtonHigh;
    @FXML
    public CheckBox p1EnabledCheckBox;
    @FXML
    public CheckBox p2EnabledCheckBox;
    @FXML
    public VBox p1SettingsVBox;
    @FXML
    public VBox p2SettingsVBox;
    @FXML
    public HBox p1TitleHBox;
    @FXML
    public HBox p2TitleHBox;

    private long p1TimePeriod;
    private long p2TimePeriod;
    private ExchangeSpecs exchangeSpecs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        p1ExampleButtonLow.setOnAction(e -> {
            fillP1ThresholdFields(ExampleAlertThresholds.getThresholds(ExampleAlertThresholds.ThresholdValuesType.LOW, CryptonoseSettings.TimePeriod.M5));
        });
        p1ExampleButtonMed.setOnAction(e -> {
            fillP1ThresholdFields(ExampleAlertThresholds.getThresholds(ExampleAlertThresholds.ThresholdValuesType.MEDIUM, CryptonoseSettings.TimePeriod.M5));
        });
        p1ExampleButtonHigh.setOnAction(e -> {
            fillP1ThresholdFields(ExampleAlertThresholds.getThresholds(ExampleAlertThresholds.ThresholdValuesType.HIGH, CryptonoseSettings.TimePeriod.M5));
        });
        p2ExampleButtonLow.setOnAction(e -> {
            fillP2ThresholdFields(ExampleAlertThresholds.getThresholds(ExampleAlertThresholds.ThresholdValuesType.LOW, CryptonoseSettings.TimePeriod.M30));
        });
        p2ExampleButtonMed.setOnAction(e -> {
            fillP2ThresholdFields(ExampleAlertThresholds.getThresholds(ExampleAlertThresholds.ThresholdValuesType.MEDIUM, CryptonoseSettings.TimePeriod.M30));
        });
        p2ExampleButtonHigh.setOnAction(e -> {
            fillP2ThresholdFields(ExampleAlertThresholds.getThresholds(ExampleAlertThresholds.ThresholdValuesType.HIGH, CryptonoseSettings.TimePeriod.M30));
        });
        p1SettingsVBox.disableProperty().bind(p1EnabledCheckBox.selectedProperty().not());
        p2SettingsVBox.disableProperty().bind(p2EnabledCheckBox.selectedProperty().not());
        p1EnabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fillP1ThresholdFields(p1TimePeriod));
        p2EnabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fillP2ThresholdFields(p2TimePeriod));
        addFormatters();
    }

    private void addFormatters() {
        p1requiredRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p1requiredDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p1requiredRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p1requiredRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p1sufficientRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p1sufficientRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p2requiredRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p2requiredDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p2requiredRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p2requiredRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p2sufficientRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
        p2sufficientRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker(MIN_ALERT_THRESHOLD, MAX_ALERT_THRESHOLD)));
    }

    private void fillFields() {
            fillP1ThresholdFields(p1TimePeriod, null);
            p1titleLabel.setText(TimeConverter.secondsToFullMinutesHoursDays(p1TimePeriod)+" period alerts thresholds");
            fillP2ThresholdFields(p2TimePeriod, null);
            p2titleLabel.setText(TimeConverter.secondsToFullMinutesHoursDays(p2TimePeriod)+" period alerts thresholds");
            p1EnabledCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.Alert.M5_ALERTS_ENABLED, exchangeSpecs));
            p2EnabledCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.Alert.M30_ALERTS_ENABLED, exchangeSpecs));
            cnLiquidityCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_MIN_CN_LIQUIDITY, exchangeSpecs));
            cnLiquiditySlider.setValue(CryptonoseSettings.getDouble(CryptonoseSettings.Alert.MIN_CN_LIQUIDITY, exchangeSpecs));
            blockSubsequentCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_BLOCK_SUBSEQUENT_ALERTS, exchangeSpecs));
            allowSubsequentCheckBox.setSelected(CryptonoseSettings.getBool(CryptonoseSettings.Alert.ENABLE_ALLOW_SUBSEQUENT_2X_ALERTS, exchangeSpecs));
    }

    private void fillP1ThresholdFields(long timePeriod, PriceAlertThresholds thresholds) {
        if (thresholds == null) {
            thresholds = CryptonoseSettings.getPriceAlertThresholds(exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(timePeriod));
        }
        String format = "%.2f";
        p1requiredRisingTextField.setText(String.format(format,thresholds.getRequiredRisingValue()));
        p1requiredDroppingTextField.setText(String.format(format,thresholds.getRequiredFallingValue()));
        p1requiredRelativeRisingTextField.setText(String.format(format,thresholds.getRequiredRelativeRisingValue()));
        p1requiredRelativeDroppingTextField.setText(String.format(format,thresholds.getRequiredRelativeFallingValue()));
        p1sufficientRelativeRisingTextField.setText(String.format(format,thresholds.getSufficientRelativeRisingValue()));
        p1sufficientRelativeDroppingTextField.setText(String.format(format,thresholds.getSufficientRelativeFallingValue()));
    }

    private void fillP1ThresholdFields(PriceAlertThresholds thresholds) {
        if (thresholds == null) {
            throw new IllegalArgumentException("thresholds can't be null");
        }
        fillP1ThresholdFields(0, thresholds);
    }

    private void fillP1ThresholdFields(long timePeriod) {
        fillP1ThresholdFields(timePeriod, null);
    }

    private void fillP2ThresholdFields(long timePeriod, PriceAlertThresholds thresholds) {
        if (thresholds == null) {
            thresholds = CryptonoseSettings.getPriceAlertThresholds(exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(timePeriod));
        }
        String format = "%.2f";
        p2requiredRisingTextField.setText(String.format(format,thresholds.getRequiredRisingValue()));
        p2requiredDroppingTextField.setText(String.format(format,thresholds.getRequiredFallingValue()));
        p2requiredRelativeRisingTextField.setText(String.format(format,thresholds.getRequiredRelativeRisingValue()));
        p2requiredRelativeDroppingTextField.setText(String.format(format,thresholds.getRequiredRelativeFallingValue()));
        p2sufficientRelativeRisingTextField.setText(String.format(format,thresholds.getSufficientRelativeRisingValue()));
        p2sufficientRelativeDroppingTextField.setText(String.format(format,thresholds.getSufficientRelativeFallingValue()));
    }

    private void fillP2ThresholdFields(PriceAlertThresholds thresholds) {
        if (thresholds == null) {
            throw new IllegalArgumentException("thresholds can't be null");
        }
        fillP2ThresholdFields(0, thresholds);
    }

    private void fillP2ThresholdFields(long timePeriod) {
        fillP2ThresholdFields(timePeriod, null);
    }

    private PriceAlertThresholds[] readTextFields() throws NumberFormatException {
        try {
            return new PriceAlertThresholds[]{
                    new PriceAlertThresholds(
                            Double.parseDouble(p1requiredRisingTextField.getText()),
                            Double.parseDouble(p1requiredDroppingTextField.getText()),
                            Double.parseDouble(p1requiredRelativeRisingTextField.getText()),
                            Double.parseDouble(p1requiredRelativeDroppingTextField.getText()),
                            Double.parseDouble(p1sufficientRelativeRisingTextField.getText()),
                            Double.parseDouble(p1sufficientRelativeDroppingTextField.getText())),
                    new PriceAlertThresholds(
                            Double.parseDouble(p2requiredRisingTextField.getText()),
                            Double.parseDouble(p2requiredDroppingTextField.getText()),
                            Double.parseDouble(p2requiredRelativeRisingTextField.getText()),
                            Double.parseDouble(p2requiredRelativeDroppingTextField.getText()),
                            Double.parseDouble(p2sufficientRelativeRisingTextField.getText()),
                            Double.parseDouble(p2sufficientRelativeDroppingTextField.getText()))
            };
        } catch (NumberFormatException e) {
            CryptonoseAlert alert = new CryptonoseAlert(Alert.AlertType.INFORMATION, "Error, please check if provided values are correct", ButtonType.OK);
            alert.setTitle("Error");
            alert.showAndWait();
            return null;
        }
    }

    public void init(ExchangeSpecs exchangeSpecs, long p1TimePeriod, long p2TimePeriod) {
        this.exchangeSpecs = exchangeSpecs;
        this.p1TimePeriod = p1TimePeriod;
        this.p2TimePeriod = p2TimePeriod;
        saveButton.setText(saveButton.getText() + String.format(" for %s", exchangeSpecs.getName()));
        fillFields();
        cnLiquiditySlider.disableProperty().bind(cnLiquidityCheckBox.selectedProperty().not());
    }

    public void closeStage() {
        ((Stage)mainHBox.getScene().getWindow()).close();
    }

    public void cancelClick(ActionEvent actionEvent) {
        closeStage();
    }

    private void saveForAllExchanges(PriceAlertThresholds[] priceAlertThresholds) {
        CryptonoseAlert alert = new CryptonoseAlert(Alert.AlertType.CONFIRMATION, "This will replace settings for all available exchanges", ButtonType.OK, ButtonType.CANCEL);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            for(ExchangeSpecs currentExchangeSpecs : CryptonoseGuiController.exchangeSpecss) {
                CryptonoseSettings.putBool(CryptonoseSettings.Alert.M5_ALERTS_ENABLED, p1EnabledCheckBox.isSelected(), currentExchangeSpecs);
                CryptonoseSettings.putBool(CryptonoseSettings.Alert.M30_ALERTS_ENABLED, p2EnabledCheckBox.isSelected(), currentExchangeSpecs);
                CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[0], currentExchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(p1TimePeriod));
                CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[1], currentExchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(p2TimePeriod));
                CryptonoseSettings.putBool(CryptonoseSettings.Alert.ENABLE_MIN_CN_LIQUIDITY, cnLiquidityCheckBox.isSelected(), currentExchangeSpecs);
                CryptonoseSettings.putDouble(CryptonoseSettings.Alert.MIN_CN_LIQUIDITY, cnLiquiditySlider.getValue(), currentExchangeSpecs);
                CryptonoseSettings.putBool(CryptonoseSettings.Alert.ENABLE_BLOCK_SUBSEQUENT_ALERTS, blockSubsequentCheckBox.isSelected(), currentExchangeSpecs);
                CryptonoseSettings.putBool(CryptonoseSettings.Alert.ENABLE_ALLOW_SUBSEQUENT_2X_ALERTS, allowSubsequentCheckBox.isSelected(), currentExchangeSpecs);
            }
            closeStage();
        }
    }

    public void saveClick() {
        PriceAlertThresholds[] priceAlertThresholds = readTextFields();
        if (priceAlertThresholds != null) {
            CryptonoseSettings.putBool(CryptonoseSettings.Alert.M5_ALERTS_ENABLED, p1EnabledCheckBox.isSelected(), exchangeSpecs);
            CryptonoseSettings.putBool(CryptonoseSettings.Alert.M30_ALERTS_ENABLED, p2EnabledCheckBox.isSelected(), exchangeSpecs);
            CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[0], exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(p1TimePeriod));
            CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[1], exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(p2TimePeriod));
            CryptonoseSettings.putBool(CryptonoseSettings.Alert.ENABLE_MIN_CN_LIQUIDITY, cnLiquidityCheckBox.isSelected(), exchangeSpecs);
            CryptonoseSettings.putDouble(CryptonoseSettings.Alert.MIN_CN_LIQUIDITY, cnLiquiditySlider.getValue(), exchangeSpecs);
            CryptonoseSettings.putBool(CryptonoseSettings.Alert.ENABLE_BLOCK_SUBSEQUENT_ALERTS, blockSubsequentCheckBox.isSelected(), exchangeSpecs);
            CryptonoseSettings.putBool(CryptonoseSettings.Alert.ENABLE_ALLOW_SUBSEQUENT_2X_ALERTS, allowSubsequentCheckBox.isSelected(), exchangeSpecs);
            closeStage();
        }
    }

    public void saveForAllExchangesClick() {
        saveForAllExchanges(readTextFields());
    }

}
