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

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

import pl.dmotyka.cryptonose2.tools.UILoader;
import pl.dmotyka.cryptonose2.dataobj.CryptonosePairData;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;
import pl.dmotyka.cryptonose2.tools.ObservableListAggregate;

public class PinnedTickersHBox {

    private final HBox pinnedHBox;
    private final ObservableListAggregate<CryptonosePairData> items;

    private final TreeSet<PinnedTicker> pinnedTickers = new TreeSet<>();

    public PinnedTickersHBox(HBox pinnedHBox, ObservableListAggregate<CryptonosePairData> items) {
        this.pinnedHBox = pinnedHBox;
        this.items = items;
        items.setListener(new ObservableListAggregate.AggregateChangeListener<>() {
            @Override
            public void onChange(ObservableList<? extends CryptonosePairData> changedList) {}

            @Override
            public void added(List<? extends CryptonosePairData> added) {
                handleAdded(added);
            }

            @Override
            public void removed(List<? extends CryptonosePairData> removed) {
                removePinnedTickers(removed);
            }
        });
    }

    private void handleAdded(List<? extends CryptonosePairData> added) {
        for (var cnPairData : added) {
            cnPairData.pinnedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    long timestampMs = CryptonoseSettings.pinTicker(cnPairData.getExchangeSpecs(), cnPairData.getPairName());
                    cnPairData.setPinnedTimestampMs(timestampMs);
                    addPinnedTickers(List.of(cnPairData));
                } else {
                    CryptonoseSettings.unpinTicker(cnPairData.getExchangeSpecs(), cnPairData.getPairName());
                    removePinnedTickers(List.of(cnPairData));
                }
            });
        }
        addPinnedTickers(added.stream().filter(tablePairPriceChanges -> tablePairPriceChanges.pinnedProperty().get()).collect(Collectors.toList()));
    }

    private void addPinnedTickers(List<? extends CryptonosePairData> newItems) {
        for (var tablePriceChanges : newItems) {
            UILoader<CryptonoseGuiPinnedNodeController> pinnedLoader = new UILoader<>("cryptonoseGuiPinnedNode.fxml");
            CryptonoseGuiPinnedNodeController pnCtrl = pinnedLoader.getController();
            pnCtrl.init(tablePriceChanges.getExchangeSpecs(), tablePriceChanges.getPairName(), tablePriceChanges.lastPriceProperty(), tablePriceChanges.chartCandlesProperty());
            PinnedTicker newPt = new PinnedTicker(tablePriceChanges, pnCtrl, pinnedLoader.getRoot());
            if (pinnedTickers.add(newPt)) {
                PinnedTicker lowerPt = pinnedTickers.lower(newPt);
                if (lowerPt != null) {
                    pinnedHBox.getChildren().add(pinnedHBox.getChildren().indexOf(lowerPt.getRoot())+1, newPt.getRoot());
                } else {
                    pinnedHBox.getChildren().add(0,newPt.getRoot());
                }
            }
        }
    }

    private void removePinnedTickers(List<? extends CryptonosePairData> removed) {
        for (var tablePairPriceChanges : removed) {
            var pinnedIt = pinnedTickers.iterator();
            while (pinnedIt.hasNext()) {
                PinnedTicker pinned = pinnedIt.next();
                if (pinned.getTablePairPriceChanges() == tablePairPriceChanges) {
                    pinnedHBox.getChildren().removeIf(node -> node == pinned.getRoot());
                    pinnedIt.remove();
                }
            }
        }
    }


}
