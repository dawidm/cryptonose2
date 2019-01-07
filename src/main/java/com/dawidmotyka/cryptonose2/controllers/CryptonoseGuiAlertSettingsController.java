package com.dawidmotyka.cryptonose2.controllers;

import com.dawidmotyka.cryptonose2.DecimalFormatterUnaryOperator;
import com.dawidmotyka.cryptonose2.PriceAlertThresholds;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Created by dawid on 8/20/17.
 */
public class CryptonoseGuiAlertSettingsController implements Initializable {

    private Preferences alertPreferences;

    @FXML
    public TextField requiredRisingTextField;
    @FXML
    public TextField requiredDroppingTextField;
    @FXML
    public TextField requiredRelativeRisingTextField;
    @FXML
    public TextField requiredRelativeDroppingTextField;
    @FXML
    public TextField sufficientRelativeRisingTextField;
    @FXML
    public TextField sufficientRelativeDroppingTextField;
    @FXML
    public ListView timePeriodsListView;
    @FXML
    public Label titleLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addFormatters();
    }

    private void addFormatters() {
        requiredRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalFormatterUnaryOperator()));
        requiredDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalFormatterUnaryOperator()));
        requiredRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalFormatterUnaryOperator()));
        requiredRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalFormatterUnaryOperator()));
        sufficientRelativeRisingTextField.setTextFormatter(new TextFormatter<>(new DecimalFormatterUnaryOperator()));
        sufficientRelativeDroppingTextField.setTextFormatter(new TextFormatter<>(new DecimalFormatterUnaryOperator()));
    }

    private void fillTextFields() {
        String selectedTimePeriod = timePeriodsListView.getSelectionModel().getSelectedItem().toString();
        PriceAlertThresholds priceAlertThresholds = PriceAlertThresholds.fromPreferences(alertPreferences,selectedTimePeriod);
        titleLabel.setText(selectedTimePeriod+"s period alerts settings");
        String format = "%.2f";
        requiredRisingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRisingValue()));
        requiredDroppingTextField.setText(String.format(format,priceAlertThresholds.getRequiredFallingValue()));
        requiredRelativeRisingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRelativeRisingValue()));
        requiredRelativeDroppingTextField.setText(String.format(format,priceAlertThresholds.getRequiredRelativeFallingValue()));
        sufficientRelativeRisingTextField.setText(String.format(format,priceAlertThresholds.getSufficientRelativeRisingValue()));
        sufficientRelativeDroppingTextField.setText(String.format(format,priceAlertThresholds.getSufficientRelativeFallingValue()));
    }

    private PriceAlertThresholds readTextFields() throws NumberFormatException {
        return new PriceAlertThresholds(Double.parseDouble(requiredRisingTextField.getText()),
                Double.parseDouble(requiredDroppingTextField.getText()),
                Double.parseDouble(requiredRelativeRisingTextField.getText()),
                Double.parseDouble(requiredRelativeDroppingTextField.getText()),
                Double.parseDouble(sufficientRelativeRisingTextField.getText()),
                Double.parseDouble(sufficientRelativeDroppingTextField.getText()));
    }

    public void init(ExchangeSpecs exchangeSpecs, int[] timePeriods) {
        setExchangeClass(exchangeSpecs);
        setTimePeriods(timePeriods);
        fillTextFields();
    }

    private void setExchangeClass(ExchangeSpecs exchangeSpecs) {
        this.alertPreferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("alertPreferences").node(exchangeSpecs.getName());
    }

    private void setTimePeriods(int[] timePeriods) {
        for(long currentPeriod : timePeriods)
            timePeriodsListView.getItems().add(""+currentPeriod);
        timePeriodsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        timePeriodsListView.getSelectionModel().selectFirst();
        timePeriodsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> fillTextFields());
    }

    public void cancelClick(ActionEvent actionEvent) {
        ((Stage)((Node)actionEvent.getSource()).getScene().getWindow()).close();
    }

    private void saveForAllExchanges(PriceAlertThresholds priceAlertThresholds, String timePeriodSeconds) {
        for(ExchangeSpecs currentExchangeSpecs : CryptonoseGuiController.EXCHANGE_SPECSS) {
            Preferences currentPreferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("alertPreferences").node(currentExchangeSpecs.getName());
            priceAlertThresholds.toPreferences(currentPreferences,timePeriodSeconds);
        }
    }

    public void saveClick() {
        String selectedTimePeriod = timePeriodsListView.getSelectionModel().getSelectedItem().toString();
        readTextFields().toPreferences(alertPreferences,selectedTimePeriod);
    }

    public void saveForAllExchangesClick() {
        String selectedTimePeriod = timePeriodsListView.getSelectionModel().getSelectedItem().toString();
        saveForAllExchanges(readTextFields(),selectedTimePeriod);
    }

}
