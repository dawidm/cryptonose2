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

package com.dawidmotyka.cryptonose2;

import com.dawidmotyka.cryptonose2.controllers.CryptonoseGuiExchangeController;
import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;
import com.dawidmotyka.exchangeutils.pairsymbolconverter.PairSymbolConverter;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class CryptonoseGuiBrowser {
    
    private static final Logger logger = Logger.getLogger(CryptonoseGuiBrowser.class.getName());
    
    public static void runBrowser(String pair, ExchangeSpecs exchangeSpecs){
        pair= PairSymbolConverter.apiSymbolToChartUrlSymbol(exchangeSpecs, pair);
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
