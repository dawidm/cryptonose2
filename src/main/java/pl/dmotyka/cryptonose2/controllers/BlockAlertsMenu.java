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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import pl.dmotyka.cryptonose2.dataobj.AlertBlockTime;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class BlockAlertsMenu extends ContextMenu {

    public BlockAlertsMenu(ExchangeSpecs exchangeSpecs, String pairApiSymbol, AlertBlockListener blockListener, BlocksSettingsListener blocksSettingsListener) {
        super();
        for (AlertBlockTime blockTime : AlertBlockTime.values()) {
            if (blockTime == AlertBlockTime.BLOCK_PERMANENTLY || blockTime == AlertBlockTime.UNBLOCK)
                continue;
            MenuItem currentItem = new MenuItem("Block %s %s alerts for %s".formatted(exchangeSpecs.getName(), exchangeSpecs.getPairSymbolConverter().toFormattedString(pairApiSymbol), blockTime.getLabel()));
            currentItem.setOnAction(event -> blockListener.block(new AlertBlock(exchangeSpecs, pairApiSymbol, blockTime)));
            this.getItems().add(currentItem);
        }
        this.getItems().add(new SeparatorMenuItem());
        MenuItem permBlockItem = new MenuItem("Block %s %s alerts until unblocked".formatted(exchangeSpecs.getName(), exchangeSpecs.getPairSymbolConverter().toFormattedString(pairApiSymbol)));
        permBlockItem.setOnAction(event -> blockListener.block(new AlertBlock(exchangeSpecs, pairApiSymbol, AlertBlockTime.BLOCK_PERMANENTLY)));
        this.getItems().add(permBlockItem);
        this.getItems().add(new SeparatorMenuItem());
        MenuItem hintItem = new MenuItem("Manage %s blocks".formatted(exchangeSpecs.getName()));
        hintItem.setOnAction(event -> blocksSettingsListener.blockSettings(exchangeSpecs));
        this.getItems().add(hintItem);
    }
}
