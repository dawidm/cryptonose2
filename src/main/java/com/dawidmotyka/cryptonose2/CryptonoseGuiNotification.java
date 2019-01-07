package com.dawidmotyka.cryptonose2;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.event.NotificationEvent;
import ch.swingfx.twinkle.event.NotificationEventAdapter;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
import ch.swingfx.twinkle.window.Positions;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import dorkbox.notify.Notify;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class CryptonoseGuiNotification {

    public static final int HIDE_AFTER=7000;

    public enum NotificationLibrary {CONTROLSFX,DORKBOX,TWINKLE};

    public static void notifyPriceAlert(NotificationLibrary notificationLibrary, PriceAlert priceAlert, Runnable action) {
        String notifyText = String.format("%.2f (rel %.2f), %s (%ds), final price: %.8f",
                priceAlert.getPriceChange(),
                priceAlert.getRelativePriceChange(),
                priceAlert.getFormattedTimePeriod(),
                priceAlert.getChangeTimeSeconds(),
                priceAlert.getFinalPrice());
        String notifyTitle = String.format("%s, %s", priceAlert.getFormattedPair(), priceAlert.getExchangeSpecs().getName());
        switch (notificationLibrary) {
            case CONTROLSFX:
                notifyControlsFx(notifyTitle,notifyText,action);
                break;
            case DORKBOX:
                notifyDorkbox(notifyTitle,notifyText,action);
                break;
            case TWINKLE:
                notifyTwinkle(notifyTitle,notifyText,action);
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
            case TWINKLE:
                notifyTwinkle(exchangeSpecs.getName(),cryptonoseGuiConnectionStatus.getText(),null);
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

    private static void notifyTwinkle(String title, String text, Runnable action) {
        INotificationStyle style = new DarkDefaultNotification()
                .withWidth(400) // Optional
                .withAlpha(0.9f); // Optional
        new NotificationBuilder()
                .withStyle(style) // Required. here we set the previously set style
                .withTitle(title) // Required.
                .withMessage(text) // Optional
                .withDisplayTime(10000) // Optional
                .withPosition(Positions.SOUTH_EAST) // Optional. Show it at the center of the screen
                .withListener(new NotificationEventAdapter() { // Optional
                    public void clicked(NotificationEvent event) {action.run();}
                })
                .showNotification(); // this returns a UUID that you can use to identify events on the listener
    }
}
