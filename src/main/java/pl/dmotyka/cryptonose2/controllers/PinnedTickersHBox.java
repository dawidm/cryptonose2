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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

import pl.dmotyka.cryptonose2.UILoader;
import pl.dmotyka.cryptonose2.tools.ObservableListAggregate;

public class PinnedTickersHBox {

    private final HBox pinnedHBox;
    private final ObservableListAggregate<TablePairPriceChanges> items;

    private final List<PinnedTicker> pinnedTickers = new LinkedList<>();

    public PinnedTickersHBox(HBox pinnedHBox, ObservableListAggregate<TablePairPriceChanges> items) {
        this.pinnedHBox = pinnedHBox;
        this.items = items;
        items.setListener(new ObservableListAggregate.AggregateChangeListener<>() {
            @Override
            public void onChange(ObservableList<? extends TablePairPriceChanges> changedList) {}

            @Override
            public void added(List<? extends TablePairPriceChanges> added) {
                for (var tablePriceChanges : added) {
                    tablePriceChanges.pinnedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            addPinnedTickers(List.of(tablePriceChanges));
                        } else {
                            removePinnedTickers(List.of(tablePriceChanges));
                        }
                    });
                }
                addPinnedTickers(added.stream().filter(tablePairPriceChanges -> tablePairPriceChanges.pinnedProperty().get()).collect(Collectors.toList()));
            }

            @Override
            public void removed(List<? extends TablePairPriceChanges> removed) {
                removePinnedTickers(removed);
            }
        });
    }

    private void addPinnedTickers(List<? extends TablePairPriceChanges> newItems) {
        for (var tablePriceChanges : newItems) {
            UILoader<CryptonoseGuiPinnedNodeController> pinnedLoader = new UILoader<>("cryptonoseGuiPinnedNode.fxml");
            CryptonoseGuiPinnedNodeController pnCtrl = pinnedLoader.getController();
            pnCtrl.init(tablePriceChanges.getExchangeSpecs(), tablePriceChanges.getPairName(), tablePriceChanges.lastPriceProperty(), tablePriceChanges.chartCandlesProperty());
            PinnedTicker newPt = new PinnedTicker(tablePriceChanges, pnCtrl, pinnedLoader.getRoot());
            pinnedTickers.add(newPt);
            pinnedHBox.getChildren().add(pinnedHBox.getChildren().size(), newPt.getRoot());
            newPt.setListPosition(pinnedHBox.getChildren().size());
        }
    }

    private void removePinnedTickers(List<? extends TablePairPriceChanges> removed) {
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
