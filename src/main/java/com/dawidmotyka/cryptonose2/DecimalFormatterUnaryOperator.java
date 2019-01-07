package com.dawidmotyka.cryptonose2;

import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

/**
 * Created by dawid on 8/20/17.
 */
public class DecimalFormatterUnaryOperator implements UnaryOperator<TextFormatter.Change> {
    @Override
    public TextFormatter.Change apply(TextFormatter.Change change) {
        if (change.getControlNewText().isEmpty()) {
            return change;
        }
        ParsePosition parsePosition = new ParsePosition( 0 );
        Object object = new DecimalFormat("#.#").parse(change.getControlNewText(), parsePosition);
        if (object == null || parsePosition.getIndex() < change.getControlNewText().length()) {
            return null;
        }
        else {
            return change;
        }
    }
}
