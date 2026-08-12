package biotrack.service;

/**
 * Classifies vitals against standard medical reference ranges (WHO / AHA / ADA).
 * Each classify* method returns a {@link Classification}: a short status label,
 * a {@link Severity} the UI can map to a colour, and the reference range used,
 * so the UI can show "120/80 mmHg - Normal (ref: <120/<80)" next to the value.
 *
 * IMPORTANT: these are general population reference ranges for informational
 * display only, not a medical diagnosis. They intentionally err toward
 * flagging borderline values rather than staying silent.
 */
public class HealthAnalyzer {

    public enum Severity { NORMAL, WARNING, CRITICAL }

    public static final class Classification {
        public final String label;
        public final Severity severity;
        public final String referenceRange;

        public Classification(String label, Severity severity, String referenceRange) {
            this.label = label;
            this.severity = severity;
            this.referenceRange = referenceRange;
        }

        @Override
        public String toString() { return label; }
    }

    // ------------------------------------------------------------------
    // BMI - WHO standard
    // ------------------------------------------------------------------

    public static double calculateBMI(double weightKg, double heightM) {
        if (heightM <= 0) return 0;
        return weightKg / (heightM * heightM);
    }

    public static Classification classifyBMI(double bmi) {
        String ref = "18.5-24.9";
        if (bmi < 18.5) return new Classification("Underweight", Severity.WARNING, ref);
        if (bmi < 25.0) return new Classification("Normal", Severity.NORMAL, ref);
        if (bmi < 30.0) return new Classification("Overweight", Severity.WARNING, ref);
        return new Classification("Obese", Severity.CRITICAL, ref);
    }

    // ------------------------------------------------------------------
    // Blood pressure - AHA guidelines, combined systolic/diastolic logic.
    // Whichever number lands in the more severe category decides the result.
    // ------------------------------------------------------------------

    public static Classification classifyBloodPressure(int systolic, int diastolic) {
        String ref = "<120 / <80 mmHg";
        if (systolic > 180 || diastolic > 120) {
            return new Classification("Hypertensive Crisis", Severity.CRITICAL, ref);
        }
        if (systolic >= 140 || diastolic >= 90) {
            return new Classification("Hypertension Stage 2", Severity.CRITICAL, ref);
        }
        if (systolic >= 130 || diastolic >= 80) {
            return new Classification("Hypertension Stage 1", Severity.WARNING, ref);
        }
        if (systolic >= 120) {
            return new Classification("Elevated", Severity.WARNING, ref);
        }
        return new Classification("Normal", Severity.NORMAL, ref);
    }

    // ------------------------------------------------------------------
    // Blood sugar / glucose - ADA guidelines. Context matters: a fasting
    // reading and a post-meal reading use different thresholds.
    // ------------------------------------------------------------------

    public static final String SUGAR_CONTEXT_FASTING = "FASTING";
    public static final String SUGAR_CONTEXT_POST_MEAL = "POST_MEAL";

    public static Classification classifyGlucose(int mgPerDl, String context) {
        boolean postMeal = SUGAR_CONTEXT_POST_MEAL.equalsIgnoreCase(context);
        if (postMeal) {
            String ref = "<140 mg/dL (2hr post-meal)";
            if (mgPerDl < 140) return new Classification("Normal", Severity.NORMAL, ref);
            if (mgPerDl < 200) return new Classification("Prediabetes", Severity.WARNING, ref);
            return new Classification("Diabetes range", Severity.CRITICAL, ref);
        } else {
            String ref = "70-99 mg/dL (fasting)";
            if (mgPerDl < 70) return new Classification("Low (hypoglycemia)", Severity.CRITICAL, ref);
            if (mgPerDl < 100) return new Classification("Normal", Severity.NORMAL, ref);
            if (mgPerDl < 126) return new Classification("Prediabetes", Severity.WARNING, ref);
            return new Classification("Diabetes range", Severity.CRITICAL, ref);
        }
    }

    // ------------------------------------------------------------------
    // Heart rate - age-adjusted resting ranges.
    // ------------------------------------------------------------------

    public static Classification classifyHeartRate(int bpm, int ageYears) {
        int low, high;
        if (ageYears < 0) ageYears = 30; // safe fallback
        if (ageYears < 1)        { low = 100; high = 160; }
        else if (ageYears < 3)   { low = 90;  high = 150; }
        else if (ageYears < 6)   { low = 80;  high = 120; }
        else if (ageYears < 12)  { low = 70;  high = 110; }
        else                     { low = 60;  high = 100; } // teen/adult resting HR

        String ref = low + "-" + high + " bpm";
        if (bpm < low) return new Classification("Bradycardia", Severity.WARNING, ref);
        if (bpm > high) return new Classification("Tachycardia", Severity.WARNING, ref);
        return new Classification("Normal", Severity.NORMAL, ref);
    }

    // ------------------------------------------------------------------
    // Body temperature (Celsius)
    // ------------------------------------------------------------------

    public static Classification classifyTemperature(double celsius) {
        String ref = "36.1-37.2 C";
        if (celsius < 35.0) return new Classification("Hypothermia", Severity.CRITICAL, ref);
        if (celsius < 36.1) return new Classification("Below normal", Severity.WARNING, ref);
        if (celsius <= 37.2) return new Classification("Normal", Severity.NORMAL, ref);
        if (celsius <= 38.0) return new Classification("Low-grade fever", Severity.WARNING, ref);
        if (celsius <= 39.4) return new Classification("Fever", Severity.CRITICAL, ref);
        return new Classification("High fever", Severity.CRITICAL, ref);
    }

    // ------------------------------------------------------------------
    // SpO2 (blood oxygen saturation, %)
    // ------------------------------------------------------------------

    public static Classification classifySpo2(double spo2Percent) {
        String ref = "95-100%";
        if (spo2Percent >= 95) return new Classification("Normal", Severity.NORMAL, ref);
        if (spo2Percent >= 90) return new Classification("Mild hypoxemia", Severity.WARNING, ref);
        return new Classification("Severe hypoxemia", Severity.CRITICAL, ref);
    }

    // ------------------------------------------------------------------
    // Total cholesterol (mg/dL)
    // ------------------------------------------------------------------

    public static Classification classifyCholesterol(int totalMgPerDl) {
        String ref = "<200 mg/dL";
        if (totalMgPerDl < 200) return new Classification("Desirable", Severity.NORMAL, ref);
        if (totalMgPerDl < 240) return new Classification("Borderline high", Severity.WARNING, ref);
        return new Classification("High", Severity.CRITICAL, ref);
    }

    // ------------------------------------------------------------------
    // Convenience: worst severity across a set of classifications, used to
    // pick an overall "status" badge for a record row.
    // ------------------------------------------------------------------

    public static Severity worstOf(Severity... severities) {
        Severity worst = Severity.NORMAL;
        for (Severity s : severities) {
            if (s == Severity.CRITICAL) return Severity.CRITICAL;
            if (s == Severity.WARNING) worst = Severity.WARNING;
        }
        return worst;
    }

    /**
     * Overall severity for a record, combining every vital that's present
     * (fields that weren't recorded, e.g. legacy rows without spo2, are
     * simply skipped). Used by search/filter ("Status: Normal/Warning/Critical")
     * and by the dashboard's recent-records list.
     */
    public static Severity overallSeverity(double bmi, Integer systolic, Integer diastolic,
                                            double temperature, int heartRate, int ageYears,
                                            int sugarLevel, String sugarContext,
                                            Double spo2, Integer cholesterolTotal) {
        java.util.List<Severity> all = new java.util.ArrayList<>();
        all.add(classifyBMI(bmi).severity);
        if (systolic != null && diastolic != null) all.add(classifyBloodPressure(systolic, diastolic).severity);
        all.add(classifyTemperature(temperature).severity);
        all.add(classifyHeartRate(heartRate, ageYears).severity);
        all.add(classifyGlucose(sugarLevel, sugarContext).severity);
        if (spo2 != null) all.add(classifySpo2(spo2).severity);
        if (cholesterolTotal != null) all.add(classifyCholesterol(cholesterolTotal).severity);
        return worstOf(all.toArray(new Severity[0]));
    }

    /**
     * Legacy-compatible text summary (kept for anything that still wants a
     * plain-text block, e.g. the PDF report's "Health Flags" section).
     */
    public static String analyze(double bmi, Integer systolic, Integer diastolic, double temperature,
                                  int heartRate, int ageYears, int sugarLevel, String sugarContext,
                                  Double spo2, Integer cholesterolTotal) {
        StringBuilder sb = new StringBuilder();
        appendIfNotNormal(sb, "BMI", classifyBMI(bmi));
        if (systolic != null && diastolic != null) {
            appendIfNotNormal(sb, "Blood pressure", classifyBloodPressure(systolic, diastolic));
        }
        appendIfNotNormal(sb, "Temperature", classifyTemperature(temperature));
        appendIfNotNormal(sb, "Heart rate", classifyHeartRate(heartRate, ageYears));
        appendIfNotNormal(sb, "Blood sugar", classifyGlucose(sugarLevel, sugarContext));
        if (spo2 != null) appendIfNotNormal(sb, "SpO2", classifySpo2(spo2));
        if (cholesterolTotal != null) appendIfNotNormal(sb, "Cholesterol", classifyCholesterol(cholesterolTotal));
        return sb.length() == 0 ? "All parameters normal." : sb.toString();
    }

    private static void appendIfNotNormal(StringBuilder sb, String vital, Classification c) {
        if (c.severity != Severity.NORMAL) {
            sb.append("- ").append(vital).append(": ").append(c.label)
              .append(" (ref: ").append(c.referenceRange).append(")\n");
        }
    }
}
