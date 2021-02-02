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

package pl.dmotyka.cryptonose2.cryptopanic;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CryptoPanicApi {

    private static final Logger logger = Logger.getLogger(CryptoPanicApi.class.getName());

    private static final String NEWS_URL = "https://cryptopanic.com/api/v1/posts/?auth_token=66134c141b19b7299101d4569bb2a1691929e2f7&public=true&currencies=%s";

    // limit - maximum number of news to return
    public static CryptoPanicNews[] getNewsForSymbol(String symbol, int limit) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resultNode = objectMapper.readTree(new URL(formatNewsUrl(symbol)));
            JsonNode resultsListNode = resultNode.get("results");
            if (resultsListNode == null)
                throw new IOException("unexpected JSON format");
            List<CryptoPanicNews> newsList = new ArrayList<>(limit);
            for (JsonNode newsNode : resultsListNode) {
                CryptoPanicNews currentNews;
                try {
                    String title = newsNode.get("title").asText().replace("\\r?\\n", " ");
                    String url = newsNode.get("url").asText();
                    String dateString = newsNode.get("published_at").asText();
                    Instant published = Instant.parse(dateString);
                    JsonNode reactions = newsNode.get("votes");
                    int numReactions = 0;
                    for (JsonNode reactionType : reactions) {
                        numReactions += reactionType.intValue();
                    }
                    currentNews = new CryptoPanicNews(title, numReactions, published, url);
                } catch (NullPointerException | DateTimeParseException e) {
                    throw new IOException("unexpected JSON format");
                }
                newsList.add(currentNews);
                if (newsList.size() >= limit)
                    return newsList.toArray(CryptoPanicNews[]::new);
            }
            return newsList.toArray(CryptoPanicNews[]::new);
        } catch (IOException e) {
            logger.warning("cannot get CryptoPanic data for: " + symbol + " " + e.getMessage());
            throw new IOException("cannot get data: " + e.getMessage());
        }
    }

    private static String formatNewsUrl(String symbol) {
        return String.format(NEWS_URL, symbol);
    }

}
