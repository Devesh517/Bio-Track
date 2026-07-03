package service;

import enums.HealthStatus;
import model.HealthRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Turns a raw list of HealthRecord rows into averages, trends and
 * plain-English insights. Used by the Dashboard/Analytics screens, by
 * ReportService (to build report narratives) and by ChatbotService (as
 * grounding context so the assistant reasons over the user's *actual*
 * numbers instead of hallucinating).
 */
public class AnalysisService {

    public static class Summary {
        public Double avgHeartRate;
        public Double avgSystolic;
        public Double avgDiastolic;
        public Double avgSugar;
        public Double avgSpo2;
        public Double latestWeight;
        public Double latestBmi;
        public HealthStatus overallStatus = HealthStatus.UNKNOWN;
        public List<String> insights = new ArrayList<>();
    }

    public Summary summarize(List<HealthRecord> records) {
        Summary s = new Summary();
        if (records == null || records.isEmpty()) {
            s.insights.add("No health records yet - add a reading to start seeing trends.");
            return s;
        }

        List<HealthRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(HealthRecord::getRecordDate));

        s.avgHeartRate = avg(sorted.stream().map(HealthRecord::getHeartRate));
        s.avgSystolic = avg(sorted.stream().map(HealthRecord::getBpSystolic));
        s.avgDiastolic = avg(sorted.stream().map(HealthRecord::getBpDiastolic));
        s.avgSugar = avg(sorted.stream().map(HealthRecord::getBloodSugar));
        s.avgSpo2 = avg(sorted.stream().map(HealthRecord::getSpo2));

        HealthRecord latest = sorted.get(sorted.size() - 1);
        s.latestWeight = latest.getWeightKg();
        s.latestBmi = latest.getBmi();

        buildInsights(s, sorted);
        s.overallStatus = deriveOverallStatus(s);
        return s;
    }

    private void buildInsights(Summary s, List<HealthRecord> sorted) {
        if (s.avgHeartRate != null) {
            if (s.avgHeartRate > 100) s.insights.add("Average heart rate is elevated (" + round(s.avgHeartRate) + " bpm) - above the normal 60-100 bpm range.");
            else if (s.avgHeartRate < 60) s.insights.add("Average heart rate is low (" + round(s.avgHeartRate) + " bpm) - below the normal 60-100 bpm range.");
            else s.insights.add("Heart rate is averaging " + round(s.avgHeartRate) + " bpm, within the normal range.");
        }

        if (s.avgSystolic != null && s.avgDiastolic != null) {
            if (s.avgSystolic >= 140 || s.avgDiastolic >= 90)
                s.insights.add("Blood pressure is trending high on average (" + round(s.avgSystolic) + "/" + round(s.avgDiastolic) + " mmHg).");
            else if (s.avgSystolic >= 120)
                s.insights.add("Blood pressure is slightly elevated on average (" + round(s.avgSystolic) + "/" + round(s.avgDiastolic) + " mmHg).");
            else
                s.insights.add("Blood pressure is averaging " + round(s.avgSystolic) + "/" + round(s.avgDiastolic) + " mmHg, within the normal range.");
        }

        if (s.avgSugar != null) {
            if (s.avgSugar > 140) s.insights.add("Blood sugar is averaging " + round(s.avgSugar) + " mg/dL, above the typical post-meal threshold.");
            else if (s.avgSugar < 70) s.insights.add("Blood sugar is averaging " + round(s.avgSugar) + " mg/dL, below the normal fasting range.");
            else s.insights.add("Blood sugar is averaging " + round(s.avgSugar) + " mg/dL, within the normal range.");
        }

        if (s.avgSpo2 != null) {
            if (s.avgSpo2 < 95) s.insights.add("SpO2 is averaging " + round(s.avgSpo2) + "%, below the recommended 95% or higher.");
            else s.insights.add("SpO2 is averaging " + round(s.avgSpo2) + "%, a healthy oxygen saturation level.");
        }

        if (s.latestBmi != null) {
            s.insights.add("Latest BMI is " + s.latestBmi + " (" + utils.BMIUtil.category(s.latestBmi) + ").");
        }

        // Simple trend detection: compare first-half vs second-half average heart rate
        if (sorted.size() >= 4) {
            int mid = sorted.size() / 2;
            OptionalDouble firstHalf = sorted.subList(0, mid).stream().map(HealthRecord::getHeartRate).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average();
            OptionalDouble secondHalf = sorted.subList(mid, sorted.size()).stream().map(HealthRecord::getHeartRate).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average();
            if (firstHalf.isPresent() && secondHalf.isPresent()) {
                double delta = secondHalf.getAsDouble() - firstHalf.getAsDouble();
                if (Math.abs(delta) >= 5) {
                    s.insights.add("Heart rate has " + (delta > 0 ? "increased" : "decreased") + " by about "
                            + Math.round(Math.abs(delta)) + " bpm over this period.");
                }
            }
        }
    }

    private HealthStatus deriveOverallStatus(Summary s) {
        boolean anyHigh = false, anyElevated = false;
        HealthRecord dummy = new HealthRecord();
        if (s.avgHeartRate != null) {
            if (s.avgHeartRate > 100 || s.avgHeartRate < 60) anyHigh = true;
        }
        if (s.avgSystolic != null && s.avgDiastolic != null) {
            if (s.avgSystolic >= 140 || s.avgDiastolic >= 90) anyHigh = true;
            else if (s.avgSystolic >= 120) anyElevated = true;
        }
        if (s.avgSugar != null && (s.avgSugar > 140 || s.avgSugar < 70)) anyHigh = true;
        if (s.avgSpo2 != null && s.avgSpo2 < 95) anyHigh = true;

        if (anyHigh) return HealthStatus.HIGH;
        if (anyElevated) return HealthStatus.ELEVATED;
        return HealthStatus.NORMAL;
    }

    private Double avg(java.util.stream.Stream<? extends Number> values) {
        List<Double> nums = new ArrayList<>();
        values.forEach(v -> { if (v != null) nums.add(v.doubleValue()); });
        if (nums.isEmpty()) return null;
        return nums.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}