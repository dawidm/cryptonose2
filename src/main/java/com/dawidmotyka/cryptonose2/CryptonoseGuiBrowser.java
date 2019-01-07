package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.cryptonose2.controllers.CryptonoseGuiExchangeController;
import com.dawidmotyka.exchangeutils.CurrencyPairConverter;
import com.dawidmotyka.exchangeutils.exchangespecs.BinanceExchangeSpecs;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class CryptonoseGuiBrowser {
    
    private static final Logger logger = Logger.getLogger(CryptonoseGuiBrowser.class.getName());
    
    public static void runBrowser(String pair, ExchangeSpecs exchangeSpecs){
        if(exchangeSpecs instanceof BinanceExchangeSpecs)
            pair=CurrencyPairConverter.binanceApiPairToUrlPair(pair);
        Preferences preferences = Preferences.userNodeForPackage(CryptonoseGuiExchangeController.class).node("cryptonosePreferences");
        String browserPath = preferences.get("browserPath", "");
        if (browserPath.length() > 0) {
            try {
                Runtime.getRuntime().exec(String.format(browserPath, exchangeSpecs.getMarketUrl() + pair));
            } catch (IOException e) {
                logger.log(Level.WARNING,"IOException when opening browser",e);
            }
        }
    }
}
