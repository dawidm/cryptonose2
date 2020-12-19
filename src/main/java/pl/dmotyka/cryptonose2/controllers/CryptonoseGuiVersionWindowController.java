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
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import pl.dmotyka.cryptonose2.CryptonoseGuiBrowser;
import pl.dmotyka.cryptonose2.updatechecker.VersionInfo;

public class CryptonoseGuiVersionWindowController implements Initializable {

    @FXML
    public Hyperlink downloadsLinkLabel;
    @FXML
    public Hyperlink releaseLinkLabel;
    @FXML
    public Label titleLabel;
    @FXML
    public Button closeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        downloadsLinkLabel.setOnMouseClicked(e -> CryptonoseGuiBrowser.runBrowser(downloadsLinkLabel.getText()));
        releaseLinkLabel.setOnMouseClicked(e -> CryptonoseGuiBrowser.runBrowser(releaseLinkLabel.getText()));
        closeButton.setOnMouseClicked(e -> ((Stage)((Button)e.getSource()).getScene().getWindow()).close());
    }

    public void init(VersionInfo versionInfo) {
        titleLabel.setText(titleLabel.getText()+versionInfo.getVersionString());
        releaseLinkLabel.setText(versionInfo.getDownloadUrl());
    }

}
