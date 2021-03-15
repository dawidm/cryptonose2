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
import java.util.Comparator;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.kordamp.ikonli.javafx.FontIcon;
import pl.dmotyka.cryptonose2.dataobj.AlertBlockTime;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class CryptonoseGuiAlertBlocksController implements Initializable {

    @FXML
    public FlowPane blocksFlowPane;
    @FXML
    public Label emptyBlocksLabel;
    @FXML
    public Button doneButton;
    @FXML
    public ScrollPane scrollPane;

    private ObservableList<AlertBlock> alertBlocks;
    private AlertBlockListener alertBlockListener;
    private ExchangeSpecs exchangeSpecs;

    private void refreshBlocks() {
        Objects.requireNonNull(alertBlocks);
        blocksFlowPane.getChildren().clear();
        if (alertBlocks.size() == 0) {
            blocksFlowPane.getChildren().add(new Label("No active blocks"));
        } else {
            ObservableList<AlertBlock> sortedBlocks = alertBlocks.sorted(Comparator.comparing(alertBlock -> exchangeSpecs.getPairSymbolConverter().toFormattedString(alertBlock.getPairApiSymbol())));
            for (AlertBlock block : sortedBlocks) {
                Label blockLabel = new Label("%s, %s".formatted(exchangeSpecs.getPairSymbolConverter().toFormattedString(block.getPairApiSymbol()), block.getBlockTime().getLabel()));
                blockLabel.setAlignment(Pos.CENTER);
                VBox labelVBox = new VBox(blockLabel);
                labelVBox.setAlignment(Pos.CENTER);
                Button blockButton = new Button();
                FontIcon fontIcon = new FontIcon("fa-close");
                fontIcon.iconColorProperty().bind(blockLabel.textFillProperty());
                blockButton.setGraphic(fontIcon);
                blockButton.getStyleClass().add("button-small");
                blockButton.setOnMouseClicked(e -> {
                    alertBlockListener.block(block.withDifferentBlockTime(AlertBlockTime.UNBLOCK));
                    Platform.runLater(this::refreshBlocks);
                });
                HBox blockHBox = new HBox(5, labelVBox, blockButton);
                blocksFlowPane.getChildren().add(blockHBox);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        doneButton.setOnAction(e -> ((Stage)doneButton.getScene().getWindow()).close());
        scrollPane.setFitToWidth(true);
        blocksFlowPane.prefWrapLengthProperty().bind(blocksFlowPane.widthProperty());
    }

    public void init(ExchangeSpecs exchangeSpecs, ObservableList<AlertBlock> alertBlocks, AlertBlockListener alertBlockListener) {
        this.exchangeSpecs = exchangeSpecs;
        this.alertBlocks = alertBlocks;
        this.alertBlockListener = alertBlockListener;
        alertBlocks.addListener((ListChangeListener<? super AlertBlock>) c -> {
            boolean addedRemoved = false;
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    addedRemoved = true;
                    break;
                }
            }
            if (addedRemoved) {
                Platform.runLater(this::refreshBlocks);
            }
        });
        refreshBlocks();
    }


}
