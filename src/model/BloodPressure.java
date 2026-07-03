package model;

import java.time.LocalDate;

/** Lightweight (date, systolic, diastolic) point used for BP trend series. */
public class BloodPressure {
    private final LocalDate date;
    private final int systolic;
    private final int diastolic;

    public BloodPressure(LocalDate date, int systolic, int diastolic) {
        this.date = date;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }

    public LocalDate getDate() { return date; }
    public int getSystolic() { return systolic; }
    public int getDiastolic() { return diastolic; }

    public String display() { return systolic + "/" + diastolic; }
}