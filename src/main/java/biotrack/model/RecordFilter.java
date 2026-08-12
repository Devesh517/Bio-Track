package biotrack.model;

import biotrack.service.HealthAnalyzer;
import java.time.LocalDate;

/**
 * Combinable (AND-ed) search/filter criteria for the health records view.
 * Every field is optional/nullable - null means "don't filter on this".
 */
public class RecordFilter {
    public String keyword;              // matched against notes (LIKE)
    public LocalDate startDate;
    public LocalDate endDate;
    public String valueField;           // one of VALUE_FIELDS, or null
    public Double valueMin;
    public Double valueMax;
    public HealthAnalyzer.Severity status; // computed client-side after fetch, or null

    /** Whitelist of columns allowed in a value-range filter - never build this from raw user input. */
    public static final String[] VALUE_FIELDS = { "bmi", "weight", "heart_rate", "sugar_level", "temperature" };

    public boolean hasKeyword() { return keyword != null && !keyword.isBlank(); }
    public boolean hasDateRange() { return startDate != null && endDate != null; }
    public boolean hasValueRange() {
        return valueField != null && (valueMin != null || valueMax != null);
    }
}
