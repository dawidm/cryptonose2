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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.dataobj.PriceAlertThresholds;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import pl.dmotyka.exchangeutils.tools.TimeConverter;

/**
 * Created by dawid on 8/20/17.
 */
public class CryptonoseGuiAlertSettingsController implements Initializable {

    private static class DecimalInputChecker implements UnaryOperator<TextFormatter.Change> {
        @Override
        public TextFormatter.Change apply(TextFormatter.Change change) {
            if (change.getControlNewText().isEmpty())
                return change;
            ParsePosition parsePosition = new ParsePosition( 0 );
            Number n = new DecimalFormat("#.#").parse(change.getControlNewText(), parsePosition);
            if (n == null || parsePosition.getIndex() < change.getControlNewText().length())
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

    private long[] timePeriods;
    private ExchangeSpecs exchangeSpecs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addFormatters();
    }

    private void addFormatters() {
        p1requiredRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p1requiredDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p1requiredRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p1requiredRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p1sufficientRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p1sufficientRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p2requiredRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p2requiredDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p2requiredRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p2requiredRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p2sufficientRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
        p2sufficientRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalInputChecker()));
    }

    private void fillTextFields() {
            long p1timePeriod=timePeriods[0];
            PriceAlertThresholds priceAlertThresholds = CryptonoseSettings.getPriceAlertThresholds(exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(p1timePeriod));
            p1titleLabel.setText(TimeConverter.secondsToFullMinutesHoursDays(p1timePeriod)+" period alerts thresholds");
            String format = "%.2f";
            p1requiredRisingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRisingValue()));
            p1requiredDroppingTextField.setText(String.format(format,priceAlertThresholds.getRequiredFallingValue()));
            p1requiredRelativeRisingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRelativeRisingValue()));
            p1requiredRelativeDroppingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRelativeFallingValue()));
            p1sufficientRelativeRisingTextField.setText(String.format(format,priceAlertThresholds.getSufficientRelativeRisingValue()));
            p1sufficientRelativeDroppingTextField.setText(String.format(format,priceAlertThresholds.getSufficientRelativeFallingValue()));
            long p2timePeriod=timePeriods[1];
            priceAlertThresholds = CryptonoseSettings.getPriceAlertThresholds(exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(p2timePeriod));
            p2titleLabel.setText(TimeConverter.secondsToFullMinutesHoursDays(p2timePeriod)+" period alerts thresholds");
            p2requiredRisingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRisingValue()));
            p2requiredDroppingTextField.setText(String.format(format,priceAlertThresholds.getRequiredFallingValue()));
            p2requiredRelativeRisingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRelativeRisingValue()));
            p2requiredRelativeDroppingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRelativeFallingValue()));
            p2sufficientRelativeRisingTextField.setText(String.format(format,priceAlertThresholds.getSufficientRelativeRisingValue()));
            p2sufficientRelativeDroppingTextField.setText(String.format(format,priceAlertThresholds.getSufficientRelativeFallingValue()));

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

    public void init(ExchangeSpecs exchangeSpecs, long[] timePeriods) {
        saveButton.setText(saveButton.getText() + String.format(" for %s", exchangeSpecs.getName()));
        if(timePeriods.length<2) {
            throw new IllegalArgumentException("timePeriods array should have at least 2 elements");
        }
        this.exchangeSpecs = exchangeSpecs;
        this.timePeriods=timePeriods;
        fillTextFields();
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
            for(ExchangeSpecs currentExchangeSpecs : CryptonoseGuiController.EXCHANGE_SPECSS) {
                CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[0], currentExchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(timePeriods[0]));
                CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[1], currentExchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(timePeriods[1]));
            }
            closeStage();
        }
    }

    public void saveClick() {
        PriceAlertThresholds[] priceAlertThresholds = readTextFields();
        if (priceAlertThresholds != null) {
            CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[0], exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(timePeriods[0]));
            CryptonoseSettings.putPriceAlertThresholds(priceAlertThresholds[1], exchangeSpecs, CryptonoseSettings.TimePeriod.getForPeriodSec(timePeriods[1]));
            closeStage();
        }
    }

    public void saveForAllExchangesClick() {
        saveForAllExchanges(readTextFields());
    }

}
