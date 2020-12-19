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
    exports pl.dmotyka.cryptonose2.controllers;
}