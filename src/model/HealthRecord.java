package model;

import enums.HealthStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * One "reading" - mirrors a row of the Dashboard (heart rate, BP, sugar,
 * SpO2, weight, BMI captured together on a given date/time). This is the
 * primary entity persisted by HealthRecordDAO and consumed by
 * AnalysisService / ExportService / ReportService / the chatbot.
 */
public class HealthRecord {

    private int id;
    private String userId;
    private LocalDate recordDate;
    private LocalTime recordTime;

    private Integer heartRate;      // bpm
    private Integer bpSystolic;     // mmHg
    private Integer bpDiastolic;    // mmHg
    private Double bloodSugar;      // mg/dL
    private Double spo2;            // %
    private Double weightKg;
    private Double bmi;
    private String notes;

    private LocalDateTime createdAt;

    public HealthRecord() { }

    public HealthRecord(String userId, LocalDate recordDate) {
        this.userId = userId;
        this.recordDate = recordDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public LocalTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalTime recordTime) { this.recordTime = recordTime; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getBpSystolic() { return bpSystolic; }
    public void setBpSystolic(Integer bpSystolic) { this.bpSystolic = bpSystolic; }

    public Integer getBpDiastolic() { return bpDiastolic; }
    public void setBpDiastolic(Integer bpDiastolic) { this.bpDiastolic = bpDiastolic; }

    public Double getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(Double bloodSugar) { this.bloodSugar = bloodSugar; }

    public Double getSpo2() { return spo2; }
    public void setSpo2(Double spo2) { this.spo2 = spo2; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getBloodPressureDisplay() {
        if (bpSystolic == null || bpDiastolic == null) return "-";
        return bpSystolic + "/" + bpDiastolic;
    }

    // -------- Convenience per-vital status, backed by AnalysisService rules --------
    public HealthStatus heartRateStatus() {
        if (heartRate == null) return HealthStatus.UNKNOWN;
        if (heartRate < 60) return HealthStatus.LOW;
        if (heartRate > 100) return HealthStatus.HIGH;
        return HealthStatus.NORMAL;
    }

    public HealthStatus bloodPressureStatus() {
        if (bpSystolic == null || bpDiastolic == null) return HealthStatus.UNKNOWN;
        if (bpSystolic >= 140 || bpDiastolic >= 90) return HealthStatus.HIGH;
        if (bpSystolic >= 120 || bpDiastolic >= 80) return HealthStatus.ELEVATED;
        if (bpSystolic < 90 || bpDiastolic < 60) return HealthStatus.LOW;
        return HealthStatus.NORMAL;
    }

    public HealthStatus sugarStatus() {
        if (bloodSugar == null) return HealthStatus.UNKNOWN;
        if (bloodSugar < 70) return HealthStatus.LOW;
        if (bloodSugar > 140) return HealthStatus.HIGH;
        return HealthStatus.NORMAL;
    }

    public HealthStatus spo2Status() {
        if (spo2 == null) return HealthStatus.UNKNOWN;
        if (spo2 < 90) return HealthStatus.CRITICAL;
        if (spo2 < 95) return HealthStatus.LOW;
        return HealthStatus.NORMAL;
    }

    public HealthStatus bmiStatus() {
        if (bmi == null) return HealthStatus.UNKNOWN;
        if (bmi < 18.5) return HealthStatus.LOW;
        if (bmi <= 24.9) return HealthStatus.NORMAL;
        if (bmi <= 29.9) return HealthStatus.ELEVATED;
        return HealthStatus.HIGH;
    }
}