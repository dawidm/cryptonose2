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

package pl.dmotyka.cryptonose2.coingecko;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeckoApi {

    private static final String INFO_URL = "https://api.coingecko.com/api/v3/coins/%s?localization=false";
    private static final String LIST_URL = "https://api.coingecko.com/api/v3/coins/list";
    private static final String MARKET_PAGE_URL = "https://www.coingecko.com/en/coins/%s";

    private static final long SYMBOL_TO_ID_UPDATE_INTERVAL_MS = 60 * 60 * 1000;

    // symbols are lower-case
    private static Map<String,Set<String>> symbolToIdMap;
    private static long symbolToIdLastUpdateMs = 0;

    // get info by currency symbol (code)
    // if in coingecko there are multiple currencies with this symbol, the one with highest market cap is chosen
    public static GeckoCurrencyInfo getInfoBySymbol(String symbol) throws IOException, GeckoNoSuchSymbolException {
        symbol = symbol.toLowerCase();
        var symbolIdMap = updateAndGetSymbolToIdMap();
        try {
            Set<String> ids = symbolIdMap.get(symbol);
            if (ids == null)
                throw new GeckoNoSuchSymbolException("no such symbol " + symbol);
            LinkedList<GeckoCurrencyInfo> infoList = new LinkedList<>();
            for (String id : ids) {
                infoList.add(getInfoById(id));
            }
            infoList.sort(Comparator.comparingDouble(GeckoCurrencyInfo::getDayVolume));
            return infoList.getLast();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static GeckoCurrencyInfo getInfoById(String id) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoJsonNode = objectMapper.readValue(new URL(getInfoUrl(id)), JsonNode.class);
        try {
            String name = infoJsonNode.get("name").asText();
            String symbol = infoJsonNode.get("symbol").asText();
            double capUsd = infoJsonNode.get("market_data").get("market_cap").get("usd").asDouble();
            int rank = infoJsonNode.get("market_cap_rank").asInt();
            double dailyChange = infoJsonNode.get("market_data").get("price_change_percentage_24h").doubleValue();
            double weeklyChange = infoJsonNode.get("market_data").get("price_change_percentage_7d").doubleValue();
            double dayVolume = infoJsonNode.get("market_data").get("total_volume").get("usd").doubleValue();
            String description = infoJsonNode.get("description").get("en").textValue();
            String homepage = infoJsonNode.get("links").get("homepage").get(0).textValue();
            return new GeckoCurrencyInfo(name, symbol, id, dailyChange, weeklyChange, capUsd, rank, dayVolume, description, homepage);
        } catch (NullPointerException e) {
            throw new IOException("unexpected data format");
        }
    }

    public static Map<String, Set<String>> updateAndGetSymbolToIdMap() throws IOException{
        long msTime = System.nanoTime() / (1000 * 1000);
        if (symbolToIdMap == null || symbolToIdLastUpdateMs == 0 || msTime - symbolToIdLastUpdateMs > SYMBOL_TO_ID_UPDATE_INTERVAL_MS) {
            symbolToIdMap = getSymbolToIdMap();
            symbolToIdLastUpdateMs = msTime;
        }
        return symbolToIdMap;
    }

    public static Map<String, Set<String>> getSymbolToIdMap() throws IOException {
        Map<String,Set<String>> map = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode listJsonNode = objectMapper.readValue(new URL(LIST_URL), JsonNode.class);
        try {
            for (JsonNode jsonNode : listJsonNode) {
                String symbol = Objects.requireNonNull(jsonNode.get("symbol").asText()).toLowerCase();
                String id = Objects.requireNonNull(jsonNode.get("id").asText());
                if (map.containsKey(symbol)) {
                    map.get(symbol).add(id);
                } else {
                    Set<String> newSet = new HashSet<>();
                    newSet.add(id);
                    map.put(symbol, newSet);
                }
            }
        } catch (NullPointerException e) {
            throw new IOException("unexpected data format");
        }
        return map;
    }

    // id - gecko market id
    public static String getMarketUrl(String id) {
        return String.format(MARKET_PAGE_URL,id);
    }

    private static String getInfoUrl(String symbol) {
        return String.format(INFO_URL, symbol);
    }
}
