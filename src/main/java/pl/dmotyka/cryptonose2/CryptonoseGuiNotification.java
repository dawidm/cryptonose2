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

package pl.dmotyka.cryptonose2;

import javax.swing.SwingUtilities;

import javafx.scene.text.Font;
import javafx.stage.Screen;

import dorkbox.notify.Notify;
import pl.dmotyka.cryptonose2.controllers.DecimalFormatter;
import pl.dmotyka.cryptonose2.dataobj.CryptonoseGuiConnectionStatus;
import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class CryptonoseGuiNotification {

    public static final int HIDE_AFTER=7000;
    public static final int DORKBOX_MAX_FONT_SIZE = 19;

    public enum NotificationLibrary {CONTROLSFX,DORKBOX};

    public static void notifyPriceAlert(NotificationLibrary notificationLibrary, PriceAlert priceAlert, Runnable action) {
        String notifyText = String.format("%.2f%% (rel %.2f), %s (%ds), final price: %s",
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

    public static void notifyConnectionState(NotificationLibrary notificationLibrary,ExchangeSpecs exchangeSpecs, CryptonoseGuiConnectionStatus cryptonoseGuiConnectionStatus) {
        String statusText;
        if (cryptonoseGuiConnectionStatus.equals(CryptonoseGuiConnectionStatus.CONNECTION_STATUS_NO_TRADES_RECONNECT))
            statusText = String.format("No price updates for %d seconds, reconnecting", CryptonoseSettings.NO_TRADES_RECONNECT_SECONDS);
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

    private static void notifyControlsFx(String title, String text, Runnable action) {
        throw new RuntimeException("Not implemented.");
//        Removed because of bugs (cannot show notification when no active window)
//        Platform.runLater(() -> {
//            Notifications notifications = Notifications.
//                create().
//                darkStyle().
//                title(title).
//                text(text).
//                hideAfter(Duration.millis(HIDE_AFTER));
//            if(action!=null)
//                notifications.onAction((actionEvent) -> action.run());
//            notifications.show();
//        });
    }

    private static void notifyDorkbox(String title, String text, Runnable action) {
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
