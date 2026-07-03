package model;

import java.time.LocalDate;

/** Lightweight (date, value) point used for blood-sugar trend series. */
public class SugarRecord {
    private final LocalDate date;
    private final double mgPerDl;

    public SugarRecord(LocalDate date, double mgPerDl) {
        this.date = date;
        this.mgPerDl = mgPerDl;
    }

    public LocalDate getDate() { return date; }
    public double getMgPerDl() { return mgPerDl; }
}