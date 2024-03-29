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

package pl.dmotyka.cryptonose2.dataobj;

/**
 * Created by dawid on 8/2/17.
 */
public enum CryptonoseGuiConnectionStatus {
    CONNECTION_STATUS_CONNECTED("Connected", "status-connected"),
    CONNECTION_STATUS_CONNECTING("Connecting", "status-connecting"),
    CONNECTION_STATUS_DISCONNECTED("Disconnected", "status-disconnected"),
    CONNECTION_STATUS_NO_UPDATES("No updates", "status-connecting"),
    CONNECTION_STATUS_NO_UPDATED_RECONNECT("No updates", "status-connecting"),
    CONNECTION_STATUS_NO_PAIRS("No pairs", "status-disconnected");

    private final String text;
    private final String cssClass;

    CryptonoseGuiConnectionStatus(String text, String cssClass) {
        this.text = text;
        this.cssClass = cssClass;
    }

    public String getText() {
        return text;
    }

    public String getCssClass() {
        return cssClass;
    }
}
