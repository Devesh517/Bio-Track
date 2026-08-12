package biotrack.model;
import java.sql.Timestamp;

/**
 * A single health check-in. bloodPressure (legacy single-number field) is
 * kept for old rows created before the systolic/diastolic split; new records
 * populate bpSystolic/bpDiastolic instead and legacy rows may have those as
 * null (see migration.sql for details).
 */
public class HealthRecord {
    private final int recordId;
    private final int userId;
    private final double weight;
    private final double height;
    private final double bmi;
    private final double temperature;
    private final Integer bloodPressure;   // legacy single-value BP, nullable
    private final Integer bpSystolic;
    private final Integer bpDiastolic;
    private final int heartRate;
    private final int sugarLevel;
    private final String sugarContext;     // "FASTING" or "POST_MEAL"
    private final Double spo2;
    private final Integer cholesterolTotal;
    private final String notes;
    private final Timestamp recordedAt;

    public HealthRecord(int recordId, int userId, double weight, double height, double bmi,
                        double temperature, Integer bloodPressure, Integer bpSystolic, Integer bpDiastolic,
                        int heartRate, int sugarLevel, String sugarContext, Double spo2, Integer cholesterolTotal,
                        String notes, Timestamp recordedAt) {
        this.recordId = recordId;
        this.userId = userId;
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
        this.temperature = temperature;
        this.bloodPressure = bloodPressure;
        this.bpSystolic = bpSystolic;
        this.bpDiastolic = bpDiastolic;
        this.heartRate = heartRate;
        this.sugarLevel = sugarLevel;
        this.sugarContext = (sugarContext == null || sugarContext.isBlank()) ? "FASTING" : sugarContext;
        this.spo2 = spo2;
        this.cholesterolTotal = cholesterolTotal;
        this.notes = notes;
        this.recordedAt = recordedAt;
    }

    public int getRecordId() { return recordId; }
    public int getUserId() { return userId; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public double getBmi() { return bmi; }
    public double getTemperature() { return temperature; }
    public Integer getBloodPressure() { return bloodPressure; }
    public Integer getBpSystolic() { return bpSystolic; }
    public Integer getBpDiastolic() { return bpDiastolic; }
    public boolean hasSystolicDiastolic() { return bpSystolic != null && bpDiastolic != null; }
    public int getHeartRate() { return heartRate; }
    public int getSugarLevel() { return sugarLevel; }
    public String getSugarContext() { return sugarContext; }
    public Double getSpo2() { return spo2; }
    public Integer getCholesterolTotal() { return cholesterolTotal; }
    public String getNotes() { return notes; }
    public Timestamp getRecordedAt() { return recordedAt; }
}
