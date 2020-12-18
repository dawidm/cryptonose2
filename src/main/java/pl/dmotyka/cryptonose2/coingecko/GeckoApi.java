/*
 * Cryptonose2
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeckoApi {

    private static final String INFO_URL = "https://api.coingecko.com/api/v3/coins/%s?localization=false";
    private static final String LIST_URL = "https://api.coingecko.com/api/v3/coins/list";

    private static final String ERR = ".err";

    public static GeckoCurrencyInfo getInfo(String symbol) throws IOException, GeckoMultipleSymbolsException {
        symbol = symbol.toLowerCase();
        ObjectMapper objectMapper = new ObjectMapper();
        var symbolIdMap = getSymbolToIdMap();
        try {
            String id = symbolIdMap.get(symbol);
            if (id.equals(ERR))
                throw new GeckoMultipleSymbolsException("coingecko has multiple assets with specified symbol");
            JsonNode infoJsonNode = objectMapper.readValue(new URL(getInfoUrl(id)), JsonNode.class);
            try {
                double capUsd = infoJsonNode.get("market_data").get("market_cap").get("usd").asDouble();
                int rank = infoJsonNode.get("market_cap_rank").asInt();
                double dailyChange = infoJsonNode.get("market_data").get("price_change_percentage_24h").doubleValue();
                double weeklyChange = infoJsonNode.get("market_data").get("price_change_percentage_7d").doubleValue();
                return new GeckoCurrencyInfo(dailyChange, weeklyChange, capUsd, rank);
            } catch (NullPointerException e) {
                throw new IOException("unexpected data format");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getSymbolToIdMap() throws IOException {
        Map<String,String> map = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode listJsonNode = objectMapper.readValue(new URL(LIST_URL), JsonNode.class);
        try {
            for (JsonNode jsonNode : listJsonNode) {
                String symbol = jsonNode.get("symbol").asText();
                Objects.requireNonNull(symbol);
                if (map.containsKey(symbol)) {
                    map.put(symbol, ERR);
                } else {
                    map.put(symbol, Objects.requireNonNull(jsonNode.get("id").asText()));
                }
            }
        } catch (NullPointerException e) {
            throw new IOException("unexpected data format");
        }
        return map;
    }

    private static String getInfoUrl(String symbol) {
        return String.format(INFO_URL, symbol);
    }
}
