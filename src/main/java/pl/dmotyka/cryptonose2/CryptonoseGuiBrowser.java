/*
 * Cryptonose
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

package pl.dmotyka.cryptonose2;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class CryptonoseGuiBrowser {
    
    private static final Logger logger = Logger.getLogger(CryptonoseGuiBrowser.class.getName());
    
    public static void runBrowser(String pair, ExchangeSpecs exchangeSpecs){
        pair= exchangeSpecs.getPairSymbolConverter().apiSymbolToChartUrlSymbol(pair);
        String urlToOpen = exchangeSpecs.getMarketUrl() + pair;
        runBrowser(urlToOpen);
    }

    public static void runBrowser(String url) {
        if(isDefaultBrowserSupported() && CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_BROWSER)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    logger.log(Level.WARNING,"when running browser",e);
                }
            }).start();
        } else {
            String browserPath = CryptonoseSettings.getString(CryptonoseSettings.General.BROWSER_PATH);
            if (browserPath.length() > 0) {
                try {
                    Runtime.getRuntime().exec(String.format(browserPath, url));
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
