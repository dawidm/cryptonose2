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

import javafx.scene.Parent;

public class PinnedTicker {

    private int listPosition = Integer.MAX_VALUE;
    private final TablePairPriceChanges tablePairPriceChanges;
    private final CryptonoseGuiPinnedNodeController controller;
    private final Parent root;

    public PinnedTicker(TablePairPriceChanges tablePairPriceChanges, CryptonoseGuiPinnedNodeController controller, Parent root) {
        this.tablePairPriceChanges = tablePairPriceChanges;
        this.controller = controller;
        this.root = root;
    }

    public TablePairPriceChanges getTablePairPriceChanges() {
        return tablePairPriceChanges;
    }

    public CryptonoseGuiPinnedNodeController getController() {
        return controller;
    }

    public Parent getRoot() {
        return root;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }
}
