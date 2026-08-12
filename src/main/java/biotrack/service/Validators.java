package biotrack.service;

import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;

import java.util.function.UnaryOperator;

/**
 * Central place for numeric health-field validation, used at two layers:
 *   1. UI: {@link #nonNegativeDecimalFormatter()} / {@link #nonNegativeIntegerFormatter()}
 *      block keystrokes that would make a field negative or non-numeric.
 *   2. Service/DAO: {@link #requireRange(String, double, double, double)} etc.
 *      are called again right before insert/update, so a record can never
 *      reach the database with an out-of-bounds value even if some future
 *      caller skips the UI (or the UI has a bug).
 *
 * Bounds below are sanity ceilings to catch typos (e.g. "999" for a heart
 * rate), not clinical limits - they're intentionally generous.
 */
public class Validators {

    public static class ValidationException extends Exception {
        public ValidationException(String message) { super(message); }
    }

    // Reasonable upper bounds to catch fat-finger typos.
    public static final double WEIGHT_MAX_KG = 500;
    public static final double HEIGHT_MAX_M = 2.75;
    public static final double TEMP_MIN_C = 25.0;
    public static final double TEMP_MAX_C = 45.0;
    public static final int BP_MIN = 40;
    public static final int BP_MAX = 300;
    public static final int HR_MIN = 20;
    public static final int HR_MAX = 300;
    public static final int SUGAR_MIN = 20;
    public static final int SUGAR_MAX = 900;
    public static final double SPO2_MIN = 0;
    public static final double SPO2_MAX = 100;
    public static final int CHOLESTEROL_MIN = 50;
    public static final int CHOLESTEROL_MAX = 1000;

    /** Blocks any keystroke that would leave the field negative, non-numeric, or with >1 decimal point. */
    public static TextFormatter<Double> nonNegativeDecimalFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (newText.matches("-?\\d*\\.?\\d*") && !newText.startsWith("-")) return change;
            return null;
        };
        return new TextFormatter<>(new DoubleStringConverter(), null, filter);
    }

    /** Blocks any keystroke that would leave the field negative or non-numeric. */
    public static TextFormatter<Integer> nonNegativeIntegerFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (newText.matches("\\d*")) return change;
            return null;
        };
        return new TextFormatter<>(new javafx.util.converter.IntegerStringConverter(), null, filter);
    }

    public static double requireRange(String fieldName, double value, double min, double max) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + trim(min) + " and " + trim(max) + ".");
        }
        return value;
    }

    public static int requireRange(String fieldName, int value, int min, int max) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    public static double requirePositive(String fieldName, double value) throws ValidationException {
        if (value <= 0) throw new ValidationException(fieldName + " must be greater than zero.");
        return value;
    }

    private static String trim(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
