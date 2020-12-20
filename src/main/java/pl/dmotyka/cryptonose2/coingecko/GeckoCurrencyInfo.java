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

package pl.dmotyka.cryptonose2.coingecko;

public class GeckoCurrencyInfo {

    private final String name;
    private final String symbol;
    private final String id;
    private final double dayChange;
    private final double weekChange;
    private final double marketCapUSD;
    private final int geckoMarketCapRank;
    private final double dayVolume;
    private final String description;
    private final String homepage;

    public GeckoCurrencyInfo(String name, String symbol, String id, double dayChange, double weekChange, double marketCapUSD, int geckoMarketCapRank, double dayVolume, String description, String homepage) {
        this.name = name;
        this.symbol = symbol;
        this.id = id;
        this.dayChange = dayChange;
        this.weekChange = weekChange;
        this.marketCapUSD = marketCapUSD;
        this.geckoMarketCapRank = geckoMarketCapRank;
        this.dayVolume = dayVolume;
        this.description = description;
        this.homepage = homepage;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getId() {
        return id;
    }

    public double getDayChange() {
        return dayChange;
    }

    public double getWeekChange() {
        return weekChange;
    }

    public double getMarketCapUSD() {
        return marketCapUSD;
    }

    public int getGeckoMarketCapRank() {
        return geckoMarketCapRank;
    }

    public double getDayVolume() {
        return dayVolume;
    }

    public String getDescription() {
        return description;
    }

    public String getHomepage() {
        return homepage;
    }
}
