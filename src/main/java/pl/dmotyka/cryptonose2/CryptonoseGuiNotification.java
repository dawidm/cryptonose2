/*
 * Cryptonose2
 *
 * Copyright © 2019 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2;

import java.text.DecimalFormat;

import javafx.util.Duration;

import dorkbox.notify.Notify;
//import org.controlsfx.control.Notifications;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class CryptonoseGuiNotification {

    public static final int HIDE_AFTER=7000;
    public static final DecimalFormat PRICE_DECIMAL_FORMAT=new DecimalFormat("#.########");

    public enum NotificationLibrary {CONTROLSFX,DORKBOX};

    public static void notifyPriceAlert(NotificationLibrary notificationLibrary, PriceAlert priceAlert, Runnable action) {
        String notifyText = String.format("%.2f%% (rel %.2f), %s (%ds), final price: %s",
                priceAlert.getPriceChange(),
                priceAlert.getRelativePriceChange(),
                priceAlert.getFormattedTimePeriod(),
                priceAlert.getChangeTimeSeconds(),
                PRICE_DECIMAL_FORMAT.format(priceAlert.getFinalPrice()));
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
        switch (notificationLibrary) {
            case CONTROLSFX:
                notifyControlsFx(exchangeSpecs.getName(),cryptonoseGuiConnectionStatus.getText(),null);
                break;
            case DORKBOX:
                notifyDorkbox(exchangeSpecs.getName(),cryptonoseGuiConnectionStatus.getText(),null);
                break;
        }
    }

    private static void notifyControlsFx(String title, String text, Runnable action) {
//        javafx.application.Platform.runLater(()-> {
//            Notifications notifications = Notifications.
//                    create().
//                    darkStyle().
//                    title(title).
//                    text(text).
//                    hideAfter(Duration.millis(HIDE_AFTER));
//            if(action!=null)
//                notifications.onAction((actionEvent) -> action.run());
//            notifications.show();
//        });
    }

    private static void notifyDorkbox(String title, String text, Runnable action) {
        Notify notify = Notify.create().darkStyle().hideAfter(HIDE_AFTER).text(text).title(title);
        if(action!=null)
            notify=notify.onAction((notify1 -> action.run()));
        notify.show();
    }
}
