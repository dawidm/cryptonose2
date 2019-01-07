package com.dawidmotyka.cryptonose2;

/**
 * Created by dawid on 8/2/17.
 */
public class CryptonoseGuiConnectionStatus {

    private final String text;
    private final String color;

    public static final CryptonoseGuiConnectionStatus CONNECTION_STATUS_CONNECTED = new CryptonoseGuiConnectionStatus("connected", "limegreen");
    public static final CryptonoseGuiConnectionStatus CONNECTION_STATUS_CONNECTING = new CryptonoseGuiConnectionStatus("connecting", "yellow");
    public static final CryptonoseGuiConnectionStatus CONNECTION_STATUS_DISCONNECTED = new CryptonoseGuiConnectionStatus("disconnected", "red");

    public CryptonoseGuiConnectionStatus(String text, String color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CryptonoseGuiConnectionStatus && (text.equals(((CryptonoseGuiConnectionStatus)obj).text)))
            return true;
        return false;
    }
}
