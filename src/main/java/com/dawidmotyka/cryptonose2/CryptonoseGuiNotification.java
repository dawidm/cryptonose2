package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import dorkbox.notify.Notify;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.text.DecimalFormat;

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
        javafx.application.Platform.runLater(()-> {
            Notifications notifications = Notifications.
                    create().
                    darkStyle().
                    title(title).
                    text(text).
                    hideAfter(Duration.millis(HIDE_AFTER));
            if(action!=null)
                notifications.onAction((actionEvent) -> action.run());
            notifications.show();
        });
    }

    private static void notifyDorkbox(String title, String text, Runnable action) {
        Notify notify = Notify.create().darkStyle().hideAfter(HIDE_AFTER).text(text).title(title);
        if(action!=null)
            notify=notify.onAction((notify1 -> action.run()));
        notify.show();
    }
}