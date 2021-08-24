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

import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

import dorkbox.notify.Notify;
import org.controlsfx.control.Notifications;
import pl.dmotyka.cryptonose2.dataobj.CryptonoseGuiConnectionStatus;
import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class CryptonoseGuiNotification {

    public static final int HIDE_AFTER=7000;
    public static final int DORKBOX_MAX_FONT_SIZE = 19;

    public enum NotificationLibrary { CONTROLSFX,DORKBOX };

    private final NotificationLibrary notificationLibrary;
    private Window anchorHelperWindow = null;
    private Popup anchorHelperPopup = null;

    public CryptonoseGuiNotification(NotificationLibrary notificationLibrary) {
        this.notificationLibrary = notificationLibrary;
    }

    public void notifyPriceAlert(PriceAlert priceAlert, Runnable action) {
        String notifyText = String.format("Change: %.2f%% (rel %.2f)\n" +
                        "Time: %s (%ds)\n" +
                        "Final price: %s",
                priceAlert.getPriceChange(),
                priceAlert.getRelativePriceChange(),
                priceAlert.getFormattedTimePeriod(),
                priceAlert.getChangeTimeSeconds(),
                DecimalFormatter.formatDecimalPrice(priceAlert.getFinalPrice()));
        String notifyTitle = String.format("%s, %s", priceAlert.getFormattedPair(), priceAlert.getExchangeSpecs().getName());
        switch (notificationLibrary) {
            case CONTROLSFX:
                notifyControlsFx(notifyTitle,notifyText,action);
                break;
            case DORKBOX:
                notifyDorkbox(notifyTitle,notifyText,action);
                break;
        }
    }

    public void notifyConnectionState(ExchangeSpecs exchangeSpecs, CryptonoseGuiConnectionStatus cryptonoseGuiConnectionStatus) {
        String statusText;
        if (cryptonoseGuiConnectionStatus.equals(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_UPDATED_RECONNECT))
            statusText = String.format("No price updates for %d seconds, reconnecting", CryptonoseSettings.NO_UPDATES_RECONNECT_SECONDS);
        else
            statusText = cryptonoseGuiConnectionStatus.getText();
        switch (notificationLibrary) {
            case CONTROLSFX:
                notifyControlsFx(exchangeSpecs.getName(),statusText,null);
                break;
            case DORKBOX:
                notifyDorkbox(exchangeSpecs.getName(),statusText,null);
                break;
        }
    }

    public void notifyText(String title, String text, Runnable action) {
        switch (notificationLibrary) {
            case CONTROLSFX:
                notifyControlsFx(title, text, action);
                break;
            case DORKBOX:
                notifyDorkbox(title, text, action);
                break;
        }
    }

    // Set to window that is always visible (doesn't have to be active) to avoid problems with notifications.
    // This is a workaround for notifications disappearing too early when no window is active
    //  (because if there's existing notification it may be used used as an owner for the next one).
    //  Also a workaround notifications disappearing after closing active window (Controlsfx Notifications
    //  use active widow as an owner for notification and when window is closed, notification disappears)
    public void setAnchorHelperWindow(Window anchorHelperWindow) {
        this.anchorHelperWindow = anchorHelperWindow;
        // An invisible popup is created as an owner for notifications popups
        Platform.runLater(() -> {
            anchorHelperPopup = new Popup();
            anchorHelperPopup.setHeight(0);
            anchorHelperPopup.setWidth(0);
            anchorHelperPopup.hide();
            anchorHelperPopup.show(anchorHelperWindow);
        });
    }

    public void anchorHelperUpdate() {
        anchorHelperPopup.setX(Screen.getPrimary().getVisualBounds().getMaxX());
        anchorHelperPopup.setY(Screen.getPrimary().getVisualBounds().getMaxY());
    }

    private void notifyControlsFx(String title, String text, Runnable action) {
        Platform.runLater(() -> {
            if (anchorHelperPopup != null) {
                anchorHelperUpdate();
            }
            Notifications notifications = Notifications.
                create().
                owner(anchorHelperPopup==null?null:anchorHelperPopup).
                darkStyle().
                title(title).
                text(text).
                hideAfter(Duration.millis(HIDE_AFTER));
            if(action!=null) {
                notifications.onAction((actionEvent) -> action.run());
            }
            notifications.show();
        });
    }

    private void notifyDorkbox(String title, String text, Runnable action) {
        Screen screen = Screen.getPrimary();
        double scaleX = screen.getOutputScaleX();
        double fontSize;
        if (CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_FONT_SIZE)) {
            Font defaultFont = Font.getDefault();
            fontSize = defaultFont.getSize();
        } else {
            fontSize = CryptonoseSettings.getInt(CryptonoseSettings.General.FONT_SIZE_PX);
        }
        SwingUtilities.invokeLater(() -> {
            // the text is scaled manually because hidpi scaling is disabled for swing
            double newFontSize = (int)(fontSize*scaleX);
            // max size is used because notifications have fixed size, thus limited space
            newFontSize=(newFontSize>DORKBOX_MAX_FONT_SIZE)?DORKBOX_MAX_FONT_SIZE:fontSize;
            Notify.TITLE_TEXT_FONT = String.format("Sans Serif BOLD %d", (int)(newFontSize*1.1));
            Notify.MAIN_TEXT_FONT = String.format("Sans Serif %d", (int)(newFontSize));
            Notify notify = Notify.create().darkStyle().hideAfter(HIDE_AFTER).text(text).title(title);
            if(action!=null)
                notify=notify.onAction((notify1 -> action.run()));
            notify.show();
        });
    }

}
