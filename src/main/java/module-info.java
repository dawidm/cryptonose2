module cryptonose {
    uses pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecsProvider;
    requires javafx.controls;
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires exchangeutils;
    requires cryptonoseengine;
    requires minimalfxcharts;
    requires javafx.fxml;
    requires xchange.core;
    requires Notify;
    requires java.sql;
    requires jdk.crypto.ec;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.text;
    exports pl.dmotyka.cryptonose2.controllers;
    exports pl.dmotyka.cryptonose2.cryptopanic;
}