/*
 * Cryptonose2
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2.settings;

import java.util.prefs.Preferences;

import pl.dmotyka.cryptonose2.controllers.CryptonoseGuiController;
import pl.dmotyka.cryptonose2.dataobj.PriceAlertThresholds;
import pl.dmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

public class CryptonoseSettings {

    public static final int DECIMAL_MAX_DIGITS = 9;
    public static final int FONT_MIN_SIZE = 6;
    public static final int FONT_MAX_SIZE = 24;

    private static final Preferences mainNode = Preferences.userNodeForPackage(CryptonoseGuiController.class);

    public enum TimePeriod {
        ANY(0),
        M5(300),
        M30(1800);

        public final long periodSec;

        TimePeriod(long periodSec) {
            this.periodSec = periodSec;
        }

        public static TimePeriod getForPeriodSec(long periodSec) {
            if (periodSec == TimePeriod.M5.periodSec)
                return TimePeriod.M5;
            if (periodSec == TimePeriod.M30.periodSec)
                return TimePeriod.M30;
            throw new IllegalArgumentException("Wrong time period");
        }
    }

    public enum PreferenceCategory {
        CATEGORY_GENERAL_PREFS("cryptonosePreferences"),
        CATEGORY_ALERTS_PREFS("alertPreferences"),
        CATEGORY_PAIRS_PREFS("pairsPreferences"),
        CATEGORY_GUI_STATE("guiState");

        private final String nodeKey;

        PreferenceCategory(String nodeKey) {
            this.nodeKey = nodeKey;
        }
    }

    private static class PreferenceSpecs<T> {

        private final String key;
        private final T defVal;
        private final PreferenceCategory category;

        private PreferenceSpecs(String key, T defVal, PreferenceCategory category) {
            this.key = key;
            this.defVal = defVal;
            this.category = category;
        }

        public String getKey() {
            return key;
        }

        public T getDefVal() {
            return defVal;
        }

        public PreferenceCategory getCategory() {
            return category;
        }
    }

    private static class GeneralPreferenceSpecs<T> extends PreferenceSpecs<T> {
        private GeneralPreferenceSpecs(String key, T defVal) {
            super(key, defVal, PreferenceCategory.CATEGORY_GENERAL_PREFS);
        }
    }

    private static class PairsPreferenceSpecs<T> extends PreferenceSpecs<T> {
        private PairsPreferenceSpecs(String key, T defVal) {
            super(key, defVal, PreferenceCategory.CATEGORY_PAIRS_PREFS);
        }
    }

    private static class AlertPreferenceSpecs<T> extends PreferenceSpecs<T> {

        private final TimePeriod timePeriod;

        private AlertPreferenceSpecs(String key, T defVal, TimePeriod timePeriod) {
            super(key, defVal, PreferenceCategory.CATEGORY_ALERTS_PREFS);
            this.timePeriod = timePeriod;
        }

        @Override
        public String getKey() {
            if (timePeriod != null)
                return super.getKey()+timePeriod.periodSec;
            return super.getKey();
        }
    }

    private static class GuiStatePreferenceSpecs<T> extends PreferenceSpecs<T> {
        private GuiStatePreferenceSpecs(String key, T defVal) {
            super(key, defVal, PreferenceCategory.CATEGORY_GUI_STATE);
        }
    }

    public static class MarketVolumePreference extends PreferenceSpecs<Double> {
        private static final double DEF_QUOTE_VOLUME = 200.0;
        public MarketVolumePreference(String key) {
            super(key, DEF_QUOTE_VOLUME, PreferenceCategory.CATEGORY_PAIRS_PREFS);
        }
    }

    public static class General {
        public static final GeneralPreferenceSpecs<Boolean> USE_DEF_BROWSER = new GeneralPreferenceSpecs<>("tryUseDefBrowser", true);
        public static final GeneralPreferenceSpecs<String> BROWSER_PATH = new GeneralPreferenceSpecs<>("browserPath", "");
        public static final GeneralPreferenceSpecs<Boolean> USE_DEF_RISING_SOUND = new GeneralPreferenceSpecs<>("defaultRisingSound", true);
        public static final GeneralPreferenceSpecs<Boolean> USE_DEF_DROPPING_SOUND = new GeneralPreferenceSpecs<>("defaultDroppingSound", true);
        public static final GeneralPreferenceSpecs<String> SOUND_RISING_FILE_PATH = new GeneralPreferenceSpecs<>("soundRisingPath", "");
        public static final GeneralPreferenceSpecs<String> SOUND_DROPPING_FILE_PATH = new GeneralPreferenceSpecs<>("soundDroppingPath", "");
        public static final GeneralPreferenceSpecs<Boolean> USE_DEF_FONT_SIZE = new GeneralPreferenceSpecs<>("defaultFontSize", true);
        public static final GeneralPreferenceSpecs<Integer> FONT_SIZE_PX = new GeneralPreferenceSpecs<>("fontSizePt", 12);
        public static final GeneralPreferenceSpecs<Boolean> DARK_MODE = new GeneralPreferenceSpecs<>("darkMode", false);
        public static final GeneralPreferenceSpecs<Boolean> CONNECTION_STATUS_NOTIFICATIONS = new GeneralPreferenceSpecs<>("connectionStatusNotif", true);
    }

    public static class Alert {
        private static final AlertPreferenceSpecs<Double> REQUIRED_RISING_THRESHOLD_M5 = new AlertPreferenceSpecs<>("requiredRisingValue", 3.0, TimePeriod.M5);
        private static final AlertPreferenceSpecs<Double> REQUIRED_FALLING_THRESHOLD_M5 = new AlertPreferenceSpecs<>("requiredFallingValue", 3.0, TimePeriod.M5);
        private static final AlertPreferenceSpecs<Double> REQUIRED_RELATIVE_RISING_THRESHOLD_M5 = new AlertPreferenceSpecs<>("requiredRelativeRisingValue", 4.0, TimePeriod.M5);
        private static final AlertPreferenceSpecs<Double> REQUIRED_RELATIVE_FALLING_THRESHOLD_M5 = new AlertPreferenceSpecs<>("requiredRelativeFallingValue", 4.0, TimePeriod.M5);
        private static final AlertPreferenceSpecs<Double> SUFFICIENT_RELATIVE_RISING_THRESHOLD_M5 = new AlertPreferenceSpecs<>("sufficientRelativeRisingValue", 8.0, TimePeriod.M5);
        private static final AlertPreferenceSpecs<Double> SUFFICIENT_RELATIVE_FALLING_THRESHOLD_M5 = new AlertPreferenceSpecs<>("sufficientRelativeFallingValue", 8.0, TimePeriod.M5);
        private static final AlertPreferenceSpecs<Double> REQUIRED_RISING_THRESHOLD_M30 = new AlertPreferenceSpecs<>("requiredRisingValue", 4.0, TimePeriod.M30);
        private static final AlertPreferenceSpecs<Double> REQUIRED_FALLING_THRESHOLD_M30 = new AlertPreferenceSpecs<>("requiredFallingValue", 4.0, TimePeriod.M30);
        private static final AlertPreferenceSpecs<Double> REQUIRED_RELATIVE_RISING_THRESHOLD_M30 = new AlertPreferenceSpecs<>("requiredRelativeRisingValue", 4.0, TimePeriod.M30);
        private static final AlertPreferenceSpecs<Double> REQUIRED_RELATIVE_FALLING_THRESHOLD_M30 = new AlertPreferenceSpecs<>("requiredRelativeFallingValue", 4.0, TimePeriod.M30);
        private static final AlertPreferenceSpecs<Double> SUFFICIENT_RELATIVE_RISING_THRESHOLD_M30 = new AlertPreferenceSpecs<>("sufficientRelativeRisingValue", 8.0, TimePeriod.M30);
        private static final AlertPreferenceSpecs<Double> SUFFICIENT_RELATIVE_FALLING_THRESHOLD_M30 = new AlertPreferenceSpecs<>("sufficientRelativeFallingValue", 8.0, TimePeriod.M30);
        public static final AlertPreferenceSpecs<Boolean> ENABLE_MIN_CN_LIQUIDITY = new AlertPreferenceSpecs<>("enableMinCnLiquidity", false, TimePeriod.ANY);
        public static final AlertPreferenceSpecs<Double> MIN_CN_LIQUIDITY = new AlertPreferenceSpecs<>("minCnLiquidity", 0.5, TimePeriod.ANY);
    }

    public static class Pairs {
        public static final PairsPreferenceSpecs<String> MARKETS = new PairsPreferenceSpecs<>("markets", "");
        public static final PairsPreferenceSpecs<String> PAIRS_API_SYMBOLS = new PairsPreferenceSpecs<>("pairsApiSymbols", "");
    }

    public static class GuiState {
        public static final GuiStatePreferenceSpecs<String> ACTIVE_EXCHANGES = new GuiStatePreferenceSpecs<>("activeExchanges", "");
        public static final GuiStatePreferenceSpecs<Boolean> POWER_SAVE = new GuiStatePreferenceSpecs<>("powerSave", false);
        public static final GuiStatePreferenceSpecs<Boolean> MAIN_IS_MAXIMIZED = new GuiStatePreferenceSpecs<>("mainIsMaximized", false);
        public static final GuiStatePreferenceSpecs<Double> MAIN_WIDTH = new GuiStatePreferenceSpecs<>("mainWidth", 0.0);
        public static final GuiStatePreferenceSpecs<Double> MAIN_HEIGHT = new GuiStatePreferenceSpecs<>("mainHeight", 0.0);
        public static final GuiStatePreferenceSpecs<Double> MAIN_X = new GuiStatePreferenceSpecs<>("mainX", -1.0);
        public static final GuiStatePreferenceSpecs<Double> MAIN_Y = new GuiStatePreferenceSpecs<>("mainY", -1.0);
        public static final GuiStatePreferenceSpecs<String> HIDDEN_NEW_VERSION = new GuiStatePreferenceSpecs<>("hiddenNewVersion", null);
        public static final GuiStatePreferenceSpecs<Boolean> ALERT_SOUND = new GuiStatePreferenceSpecs<>("enableAlertSound", true);
        public static final GuiStatePreferenceSpecs<Boolean> ALERT_BROWSER = new GuiStatePreferenceSpecs<>("enableAlertBrowser", false);
        public static final GuiStatePreferenceSpecs<Boolean> ALERT_NOTIFICATION = new GuiStatePreferenceSpecs<>("enableAlertNotification", true);
    }

    public static PriceAlertThresholds getPriceAlertThresholds(ExchangeSpecs forExchange, TimePeriod timePeriod) {
        return switch (timePeriod) {
            case M5 -> new PriceAlertThresholds(
                    getDouble(Alert.REQUIRED_RISING_THRESHOLD_M5, forExchange),
                    getDouble(Alert.REQUIRED_FALLING_THRESHOLD_M5, forExchange),
                    getDouble(Alert.REQUIRED_RELATIVE_RISING_THRESHOLD_M5, forExchange),
                    getDouble(Alert.REQUIRED_RELATIVE_FALLING_THRESHOLD_M5, forExchange),
                    getDouble(Alert.SUFFICIENT_RELATIVE_RISING_THRESHOLD_M5, forExchange),
                    getDouble(Alert.SUFFICIENT_RELATIVE_FALLING_THRESHOLD_M5, forExchange)
            );
            case M30 -> new PriceAlertThresholds(
                    getDouble(Alert.REQUIRED_RISING_THRESHOLD_M30, forExchange),
                    getDouble(Alert.REQUIRED_FALLING_THRESHOLD_M30, forExchange),
                    getDouble(Alert.REQUIRED_RELATIVE_RISING_THRESHOLD_M30, forExchange),
                    getDouble(Alert.REQUIRED_RELATIVE_FALLING_THRESHOLD_M30, forExchange),
                    getDouble(Alert.SUFFICIENT_RELATIVE_RISING_THRESHOLD_M30, forExchange),
                    getDouble(Alert.SUFFICIENT_RELATIVE_FALLING_THRESHOLD_M30, forExchange)
            );
            case ANY -> throw new IllegalArgumentException("wrong time period");
        };

    }

    public static void putPriceAlertThresholds(PriceAlertThresholds priceAlertThresholds, ExchangeSpecs forExchange, TimePeriod timePeriod) {
        switch (timePeriod) {
            case M5 -> {
                putDouble(Alert.REQUIRED_RISING_THRESHOLD_M5, priceAlertThresholds.getRequiredRisingValue(), forExchange);
                putDouble(Alert.REQUIRED_FALLING_THRESHOLD_M5, priceAlertThresholds.getRequiredFallingValue(), forExchange);
                putDouble(Alert.REQUIRED_RELATIVE_RISING_THRESHOLD_M5, priceAlertThresholds.getRequiredRelativeRisingValue(), forExchange);
                putDouble(Alert.REQUIRED_RELATIVE_FALLING_THRESHOLD_M5, priceAlertThresholds.getRequiredRelativeFallingValue(), forExchange);
                putDouble(Alert.SUFFICIENT_RELATIVE_RISING_THRESHOLD_M5, priceAlertThresholds.getSufficientRelativeRisingValue(), forExchange);
                putDouble(Alert.SUFFICIENT_RELATIVE_FALLING_THRESHOLD_M5, priceAlertThresholds.getSufficientRelativeFallingValue(), forExchange);
            }
            case M30 -> {
                putDouble(Alert.REQUIRED_RISING_THRESHOLD_M30, priceAlertThresholds.getRequiredRisingValue(), forExchange);
                putDouble(Alert.REQUIRED_FALLING_THRESHOLD_M30, priceAlertThresholds.getRequiredFallingValue(), forExchange);
                putDouble(Alert.REQUIRED_RELATIVE_RISING_THRESHOLD_M30, priceAlertThresholds.getRequiredRelativeRisingValue(), forExchange);
                putDouble(Alert.REQUIRED_RELATIVE_FALLING_THRESHOLD_M30, priceAlertThresholds.getRequiredRelativeFallingValue(), forExchange);
                putDouble(Alert.SUFFICIENT_RELATIVE_RISING_THRESHOLD_M30, priceAlertThresholds.getSufficientRelativeRisingValue(), forExchange);
                putDouble(Alert.SUFFICIENT_RELATIVE_FALLING_THRESHOLD_M30, priceAlertThresholds.getSufficientRelativeFallingValue(), forExchange);
            }
        }
    }

    public static boolean getBool(PreferenceSpecs<Boolean> specs) {
        return getBool(specs, null);
    }
    public static boolean getBool(PreferenceSpecs<Boolean> specs, ExchangeSpecs forExchange) {
        return getPrefsNode(specs.getCategory(), forExchange).getBoolean(specs.getKey(), specs.getDefVal());
    }

    public static String getString(PreferenceSpecs<String> specs) {
        return getString(specs, null);
    }

    public static String getString(PreferenceSpecs<String> specs, ExchangeSpecs forExchange) {
        return getPrefsNode(specs.getCategory(), forExchange).get(specs.getKey(), specs.getDefVal());
    }

    public static int getInt(PreferenceSpecs<Integer> specs) {
        return getInt(specs, null);
    }

    public static int getInt(PreferenceSpecs<Integer> specs, ExchangeSpecs forExchange) {
        return getPrefsNode(specs.getCategory(), forExchange).getInt(specs.getKey(), specs.getDefVal());
    }

    public static double getDouble(PreferenceSpecs<Double> specs) {
        return getDouble(specs, null);
    }

    public static double getDouble(PreferenceSpecs<Double> specs, ExchangeSpecs forExchange) {
        return getPrefsNode(specs.getCategory(), forExchange).getDouble(specs.getKey(), specs.getDefVal());
    }

    public static void putBool(PreferenceSpecs<Boolean> specs, boolean val) {
        putBool(specs, val, null);
    }

    public static void putBool(PreferenceSpecs<Boolean> specs, boolean val, ExchangeSpecs forExchange) {
        getPrefsNode(specs.getCategory(), forExchange).putBoolean(specs.getKey(), val);
    }

    public static void putString(PreferenceSpecs<String> specs, String val) {
        putString(specs, val, null);
    }

    public static void putString(PreferenceSpecs<String> specs, String val, ExchangeSpecs forExchange) {
        getPrefsNode(specs.getCategory(), forExchange).put(specs.getKey(), val);
    }

    public static void putInt(PreferenceSpecs<Integer> specs, int val) {
        putInt(specs, val, null);
    }

    public static void putInt(PreferenceSpecs<Integer> specs, int val, ExchangeSpecs forExchange) {
        getPrefsNode(specs.getCategory(), forExchange).putInt(specs.getKey(), val);
    }

    public static void putDouble(PreferenceSpecs<Double> specs, double val) {
        putDouble(specs, val, null);
    }

    public static void putDouble(PreferenceSpecs<Double> specs, double val, ExchangeSpecs forExchange) {
        getPrefsNode(specs.getCategory(), forExchange).putDouble(specs.getKey(), val);
    }

    public static Preferences getPrefsNode(PreferenceCategory category, ExchangeSpecs exchangeSpecs) {
        if (exchangeSpecs == null)
            return mainNode.node(category.nodeKey);
        else
            return mainNode.node(category.nodeKey).node(exchangeSpecs.getName());
    }

}
