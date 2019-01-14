package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.cryptonose2.controllers.CryptonoseGuiExchangeController;
import com.dawidmotyka.exchangeutils.CurrencyPairConverter;
import com.dawidmotyka.exchangeutils.exchangespecs.BinanceExchangeSpecs;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class CryptonoseGuiBrowser {
    
    private static final Logger logger = Logger.getLogger(CryptonoseGuiBrowser.class.getName());
    
    public static void runBrowser(String pair, ExchangeSpecs exchangeSpecs){
        if(exchangeSpecs instanceof BinanceExchangeSpecs)
            pair=CurrencyPairConverter.binanceApiPairToUrlPair(pair);
        String urlToOpen = exchangeSpecs.getMarketUrl() + pair;
        Preferences preferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("cryptonosePreferences");
        if(isDefaultBrowserSupported() && preferences.getBoolean("tryUseDefBrowser",true)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(urlToOpen));
                } catch (Exception e) {
                    logger.log(Level.WARNING,"when running browser",e);
                }
            }).start();
        } else {
            String browserPath = preferences.get("browserPath", "");
            if (browserPath.length() > 0) {
                try {
                    Runtime.getRuntime().exec(String.format(browserPath, urlToOpen));
                } catch (IOException e) {
                    logger.log(Level.WARNING, "IOException when opening browser", e);
                }
            }
        }
    }

    public static boolean isDefaultBrowserSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }
}
