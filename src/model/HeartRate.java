package model;

import java.time.LocalDate;

/** Lightweight (date, value) point used when building heart-rate trend series. */
public class HeartRate {
    private final LocalDate date;
    private final int bpm;

    public HeartRate(LocalDate date, int bpm) {
        this.date = date;
        this.bpm = bpm;
    }

    public LocalDate getDate() { return date; }
    public int getBpm() { return bpm; }
}