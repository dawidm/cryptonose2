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

package pl.dmotyka.cryptonose2;

import javafx.scene.Node;

public abstract class PriceAlertPlugin {

    private final String name;
    private final String buttonTitle;
    private final String buttonCssClass;
    protected final String currencySymbol;
    protected Node anchor;

    public PriceAlertPlugin(String name, String buttonTitle, String buttonCssClass, String currencySymbol) {
        this.name = name;
        this.buttonTitle = buttonTitle;
        this.buttonCssClass = buttonCssClass;
        this.currencySymbol = currencySymbol;
    }

    public abstract void show();
    public abstract boolean isShowing();

    public void setAnchor(Node anchor) {
        this.anchor = anchor;
    }

    public String getName() {
        return name;
    }

    public String getButtonTitle() {
        return buttonTitle;
    }

    public String getButtonCssClass() {
        return buttonCssClass;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }
}
