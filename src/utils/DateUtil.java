package utils;

import config.Constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private DateUtil() { }

    public static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern(Constants.DATE_DISPLAY_FORMAT);
    public static final DateTimeFormatter DISPLAY_DATETIME =
            DateTimeFormatter.ofPattern(Constants.DATETIME_DISPLAY_FORMAT);
    public static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE;

    public static String format(LocalDate date) {
        return date == null ? "-" : date.format(DISPLAY_DATE);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DISPLAY_DATETIME);
    }

    public static LocalDate parseIso(String text) {
        return (text == null || text.isBlank()) ? null : LocalDate.parse(text, ISO_DATE);
    }

    public static String fileTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}