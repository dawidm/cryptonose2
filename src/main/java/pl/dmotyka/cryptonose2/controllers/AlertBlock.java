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

package pl.dmotyka.cryptonose2.controllers;

import java.util.Objects;

import pl.dmotyka.cryptonose2.dataobj.AlertBlockTime;
import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class AlertBlock {

    private final ExchangeSpecs exchangeSpecs;
    private final String pairApiSymbol;
    private final AlertBlockTime blockTime;

    public AlertBlock(ExchangeSpecs exchangeSpecs, String pairApiSymbol, AlertBlockTime blockTime) {
        this.exchangeSpecs = exchangeSpecs;
        this.pairApiSymbol = pairApiSymbol;
        this.blockTime = blockTime;
    }

    public ExchangeSpecs getExchangeSpecs() {
        return exchangeSpecs;
    }

    public String getPairApiSymbol() {
        return pairApiSymbol;
    }

    public AlertBlockTime getBlockTime() {
        return blockTime;
    }

    public boolean isSamePair(AlertBlock other) {
        return this.getPairApiSymbol().equals(other.getPairApiSymbol()) && this.getExchangeSpecs().equals(other.getExchangeSpecs());
    }

    public boolean isForAlert(PriceAlert priceAlert) {
        return this.getPairApiSymbol().equals(priceAlert.getPair()) && this.exchangeSpecs.equals(priceAlert.getExchangeSpecs());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AlertBlock that = (AlertBlock) o;
        return exchangeSpecs.equals(that.exchangeSpecs) &&
                pairApiSymbol.equals(that.pairApiSymbol) &&
                blockTime == that.blockTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeSpecs, pairApiSymbol, blockTime);
    }
}
