package model;

import java.time.LocalDate;

/** Lightweight (date, weight, bmi) point used for weight/BMI trend series. */
public class BMIRecord {
    private final LocalDate date;
    private final double weightKg;
    private final double bmi;

    public BMIRecord(LocalDate date, double weightKg, double bmi) {
        this.date = date;
        this.weightKg = weightKg;
        this.bmi = bmi;
    }

    public LocalDate getDate() { return date; }
    public double getWeightKg() { return weightKg; }
    public double getBmi() { return bmi; }
}